package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.CustomerValidationRequest;
import com.nttd.banking.customer.domain.event.CustomerValidationResponse;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Kafka responder for customer validation requests.
 * Listens to customer.validation.request and responds with customer.validation.response.
 *
 * <p>This component validates customer existence and status by querying the database,
 * then publishes a response to allow other services (e.g., account-service) to validate
 * customers before creating products.</p>
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Component
public class KafkaCustomerValidationResponder {

  private final KafkaSender<String, Object> kafkaSender;
  private final CustomerRepository customerRepository;
  private final String bootstrapServers;

  private KafkaReceiver<String, CustomerValidationRequest> requestReceiver;
  private Disposable subscription;

  /**
   * Constructor for the customer validation responder.
   *
   * @param kafkaSender the Kafka sender
   * @param customerRepository the customer repository
   * @param bootstrapServers the Kafka bootstrap servers
   */
  public KafkaCustomerValidationResponder(
      KafkaSender<String, Object> kafkaSender,
      CustomerRepository customerRepository,
      @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
    this.kafkaSender = kafkaSender;
    this.customerRepository = customerRepository;
    this.bootstrapServers = bootstrapServers;
  }

  /**
   * Starts listening for validation requests when the component is initialized.
   */
  @PostConstruct
  public void start() {
    log.info("Customer validation responder starting");

    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "customer-service-validation-responder");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CustomerValidationRequest.class);
    consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    ReceiverOptions<String, CustomerValidationRequest> receiverOptions =
        ReceiverOptions.<String, CustomerValidationRequest>create(consumerProps)
            .subscription(Collections.singletonList(KafkaTopics.CUSTOMER_VALIDATION_REQUEST));

    requestReceiver = KafkaReceiver.create(receiverOptions);

    subscription = requestReceiver
        .receive()
        .flatMap(record -> {
          CustomerValidationRequest request = record.value();
          log.debug("Received customer validation request: customerId={}, correlationId={}",
              request.getCustomerId(), request.getCorrelationId());

          return validateCustomer(request)
              .flatMap(response -> sendResponse(response)
                  .doOnSuccess(v -> {
                    record.receiverOffset().acknowledge();
                    log.debug("Customer validation response sent: customerId={}, exists={}",
                        response.getCustomerId(), response.isExists());
                  }))
              .onErrorResume(error -> {
                log.error("Error validating customer: customerId={}, correlationId={}, error={}",
                    request.getCustomerId(), request.getCorrelationId(), error.getMessage());
                // Send "not exists" response on error
                CustomerValidationResponse errorResponse =
                    CustomerValidationResponse.notExists(
                        request.getCorrelationId(),
                        request.getCustomerId());
                return sendResponse(errorResponse)
                    .doOnSuccess(v -> record.receiverOffset().acknowledge());
              });
        })
        .doOnError(error -> log.error("Error processing validation request", error))
        .subscribe();

    log.info("Customer validation responder started successfully");
  }

  /**
   * Stops the validation responder when the component is destroyed.
   */
  @PreDestroy
  public void stop() {
    log.info("Customer validation responder stopping");
    if (subscription != null && !subscription.isDisposed()) {
      subscription.dispose();
    }
  }

  /**
   * Validates a customer by querying the database.
   *
   * @param request the validation request
   * @return Mono with CustomerValidationResponse
   */
  private Mono<CustomerValidationResponse> validateCustomer(CustomerValidationRequest request) {
    return customerRepository.findById(request.getCustomerId())
        .map(customer -> {
          String customerType = customer.getCustomerType().name();
          String customerProfile = extractProfile(customer);
          boolean isActive = customer.getStatus() == CustomerStatus.ACTIVE;

          return CustomerValidationResponse.exists(
              request.getCorrelationId(),
              request.getCustomerId(),
              customerType,
              customerProfile,
              isActive);
        })
        .defaultIfEmpty(CustomerValidationResponse.notExists(
            request.getCorrelationId(),
            request.getCustomerId()));
  }

  /**
   * Extracts the profile string from a customer based on type.
   *
   * @param customer the customer
   * @return the profile as string (STANDARD, VIP, or PYME)
   */
  private String extractProfile(Customer customer) {
    if (customer instanceof PersonalCustomer personalCustomer) {
      return personalCustomer.getPersonalProfile() != null
          ? personalCustomer.getPersonalProfile().name()
          : "STANDARD";
    } else if (customer instanceof BusinessCustomer businessCustomer) {
      return businessCustomer.getBusinessProfile() != null
          ? businessCustomer.getBusinessProfile().name()
          : "STANDARD";
    }
    return "STANDARD";
  }

  /**
   * Sends a validation response to Kafka.
   *
   * @param response the validation response
   * @return Mono that completes when the response is sent
   */
  private Mono<Void> sendResponse(CustomerValidationResponse response) {
    ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
        KafkaTopics.CUSTOMER_VALIDATION_RESPONSE,
        response.getCustomerId(),
        response);

    SenderRecord<String, Object, String> senderRecord =
        SenderRecord.create(producerRecord, response.getCorrelationId());

    return kafkaSender.send(Mono.just(senderRecord))
        .doOnNext(result -> log.debug("Validation response sent: correlationId={}",
            response.getCorrelationId()))
        .then();
  }
}
