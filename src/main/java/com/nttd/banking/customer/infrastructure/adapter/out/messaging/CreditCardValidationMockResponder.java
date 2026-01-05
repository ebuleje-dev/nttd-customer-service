package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.CreditCardValidationRequest;
import com.nttd.banking.customer.domain.event.CreditCardValidationResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Mock responder for credit card validation requests.
 * This component simulates the credit-card-service for development and testing purposes.
 *
 * <p>This mock component should be disabled when the real credit-card-service
 * is deployed and available in the environment.</p>
 *
 * <p>Configuration properties:</p>
 * <ul>
 *   <li>{@code credit-card.validation.mock.enabled} - Enable/disable this mock (default: true)</li>
 *   <li>{@code credit-card.validation.mock.always-approve} - Always approve requests
 *       (default: true)</li>
 * </ul>
 *
 * @author NTT Data
 * @version 1.0
 * @see CreditCardValidationPort
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "credit-card.validation.mock.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class CreditCardValidationMockResponder {

  private final KafkaSender<String, Object> kafkaSender;
  private final String bootstrapServers;
  private final boolean alwaysApprove;
  private final Random random = new Random();

  private KafkaReceiver<String, CreditCardValidationRequest> requestReceiver;
  private Disposable subscription;

  /**
   * Constructor for the mock responder.
   *
   * @param kafkaSender the Kafka sender
   * @param bootstrapServers the Kafka bootstrap servers
   * @param alwaysApprove whether to always approve validation requests
   */
  public CreditCardValidationMockResponder(
      KafkaSender<String, Object> kafkaSender,
      @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
      @Value("${credit-card.validation.mock.always-approve:true}") boolean alwaysApprove) {
    this.kafkaSender = kafkaSender;
    this.bootstrapServers = bootstrapServers;
    this.alwaysApprove = alwaysApprove;
  }

  /**
   * Starts listening for validation requests when the component is initialized.
   */
  @PostConstruct
  public void start() {
    log.info("Credit card validation mock responder starting - alwaysApprove={}", alwaysApprove);

    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "mock-credit-card-service");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CreditCardValidationRequest.class);
    consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    ReceiverOptions<String, CreditCardValidationRequest> receiverOptions =
        ReceiverOptions.<String, CreditCardValidationRequest>create(consumerProps)
            .subscription(Collections.singletonList(
                KafkaTopics.CREDIT_CARD_VALIDATION_REQUEST));

    requestReceiver = KafkaReceiver.create(receiverOptions);

    subscription = requestReceiver
        .receive()
        .flatMap(record -> {
          CreditCardValidationRequest request = record.value();
          log.debug("Mock received validation request: customerId={}, correlationId={}",
              request.getCustomerId(), request.getCorrelationId());

          boolean hasActiveCreditCard = determineValidationResult(request);

          CreditCardValidationResponse response = CreditCardValidationResponse.success(
              request.getCorrelationId(),
              request.getCustomerId(),
              hasActiveCreditCard);

          log.debug("Mock sending validation response: customerId={}, hasActiveCreditCard={}",
              request.getCustomerId(), hasActiveCreditCard);

          return sendResponse(response)
              .doOnSuccess(v -> record.receiverOffset().acknowledge());
        })
        .doOnError(error -> log.error("Error processing validation request in mock", error))
        .subscribe();

    log.info("Credit card validation mock responder started successfully");
  }

  /**
   * Stops the mock responder when the component is destroyed.
   */
  @PreDestroy
  public void stop() {
    log.info("Credit card validation mock responder stopping");
    if (subscription != null && !subscription.isDisposed()) {
      subscription.dispose();
    }
  }

  /**
   * Determines the validation result based on configuration.
   *
   * @param request the validation request
   * @return true if validation should pass, false otherwise
   */
  private boolean determineValidationResult(CreditCardValidationRequest request) {
    if (alwaysApprove) {
      return true;
    }
    // In random mode, 80% chance of having a credit card
    return random.nextDouble() < 0.8;
  }

  /**
   * Sends a validation response to Kafka.
   *
   * @param response the validation response
   * @return Mono that completes when the response is sent
   */
  private Mono<Void> sendResponse(CreditCardValidationResponse response) {
    ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
        KafkaTopics.CREDIT_CARD_VALIDATION_RESPONSE,
        response.getCustomerId(),
        response);

    SenderRecord<String, Object, String> senderRecord =
        SenderRecord.create(producerRecord, response.getCorrelationId());

    return kafkaSender.send(Mono.just(senderRecord))
        .doOnNext(result -> log.debug("Mock response sent: correlationId={}",
            response.getCorrelationId()))
        .then();
  }
}
