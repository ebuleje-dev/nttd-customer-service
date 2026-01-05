package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.ProductsQueryRequest;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import com.nttd.banking.customer.domain.port.out.CustomerProductsPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Kafka adapter implementing CustomerProductsPort for querying customer products.
 * Uses request-reply pattern with correlation IDs for asynchronous communication.
 *
 * <p>This adapter sends requests to the products query request topic and listens
 * for responses on the products query response topic, matching them by correlation ID.</p>
 *
 * @author NTT Data
 * @version 1.0
 * @see CustomerProductsPort
 */
@Slf4j
@Component
public class KafkaCustomerProductsAdapter implements CustomerProductsPort {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  private final KafkaSender<String, Object> kafkaSender;
  private final String bootstrapServers;
  private final String consumerGroupId;

  private final Map<String, Sinks.One<ProductsQueryResponse>> pendingRequests =
      new ConcurrentHashMap<>();

  private KafkaReceiver<String, ProductsQueryResponse> responseReceiver;
  private Disposable subscription;

  /**
   * Constructor for the Kafka products adapter.
   *
   * @param kafkaSender     the Kafka sender
   * @param bootstrapServers the Kafka bootstrap servers
   * @param consumerGroupId  the consumer group ID
   */
  public KafkaCustomerProductsAdapter(
      KafkaSender<String, Object> kafkaSender,
      @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
      @Value("${spring.kafka.consumer.group-id:customer-service-group}") String consumerGroupId) {
    this.kafkaSender = kafkaSender;
    this.bootstrapServers = bootstrapServers;
    this.consumerGroupId = consumerGroupId;
  }

  /**
   * Initializes the Kafka receiver for products query responses.
   */
  @PostConstruct
  public void init() {
    log.info("Initializing Kafka customer products adapter");

    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-products");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductsQueryResponse.class);
    consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    ReceiverOptions<String, ProductsQueryResponse> receiverOptions =
        ReceiverOptions.<String, ProductsQueryResponse>create(consumerProps)
            .subscription(Collections.singletonList(KafkaTopics.CUSTOMER_PRODUCTS_RESPONSE));

    responseReceiver = KafkaReceiver.create(receiverOptions);
    startListening();

    log.info("Kafka customer products adapter initialized successfully");
  }

  /**
   * Starts listening for products query responses.
   */
  private void startListening() {
    subscription = responseReceiver.receive()
        .doOnNext(record -> {
          ProductsQueryResponse response = record.value();
          String correlationId = response.getCorrelationId();

          log.debug("Received products query response: correlationId={}, customerId={}, "
                  + "totalProducts={}",
              correlationId, response.getCustomerId(), response.getTotalProducts());

          Sinks.One<ProductsQueryResponse> sink = pendingRequests.remove(correlationId);
          if (sink != null) {
            sink.tryEmitValue(response);
          } else {
            log.warn("No pending request found for correlation ID: {}", correlationId);
          }

          record.receiverOffset().acknowledge();
        })
        .doOnError(error -> log.error("Error receiving products query response", error))
        .subscribe();
  }

  /**
   * Cleanup resources when the component is destroyed.
   */
  @PreDestroy
  public void destroy() {
    log.info("Shutting down Kafka customer products adapter");
    if (subscription != null && !subscription.isDisposed()) {
      subscription.dispose();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Mono<ProductsQueryResponse> queryCustomerProducts(
      String customerId,
      String customerType) {
    return queryCustomerProducts(customerId, customerType, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Mono<ProductsQueryResponse> queryCustomerProducts(
      String customerId,
      String customerType,
      boolean activeOnly) {

    ProductsQueryRequest request = ProductsQueryRequest.create(
        customerId, customerType, activeOnly);

    log.debug("Sending products query request: customerId={}, correlationId={}",
        customerId, request.getCorrelationId());

    Sinks.One<ProductsQueryResponse> sink = Sinks.one();
    pendingRequests.put(request.getCorrelationId(), sink);

    return sendRequest(request)
        .then(sink.asMono()
            .timeout(TIMEOUT)
            .doOnError(error -> {
              pendingRequests.remove(request.getCorrelationId());
              log.error("Error or timeout waiting for products query response: "
                  + "customerId={}, correlationId={}", customerId, request.getCorrelationId());
            })
            .onErrorResume(error -> {
              log.warn("Returning empty products due to error: {}", error.getMessage());
              return Mono.just(ProductsQueryResponse.empty(
                  request.getCorrelationId(), customerId));
            }));
  }

  /**
   * Sends a products query request to Kafka.
   *
   * @param request the products query request
   * @return a Mono that completes when the request is sent
   */
  private Mono<Void> sendRequest(ProductsQueryRequest request) {
    ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
        KafkaTopics.CUSTOMER_PRODUCTS_REQUEST,
        request.getCustomerId(),
        request);

    SenderRecord<String, Object, String> senderRecord =
        SenderRecord.create(producerRecord, request.getCorrelationId());

    return kafkaSender.send(Mono.just(senderRecord))
        .doOnNext(result ->
            log.debug("Products query request sent: correlationId={}",
                request.getCorrelationId()))
        .doOnError(error ->
            log.error("Failed to send products query request: correlationId={}",
                request.getCorrelationId(), error))
        .then();
  }
}
