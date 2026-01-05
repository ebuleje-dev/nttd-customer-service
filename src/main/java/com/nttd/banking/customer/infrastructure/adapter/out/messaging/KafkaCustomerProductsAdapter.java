package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.ProductsQueryRequest;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import com.nttd.banking.customer.domain.port.out.CustomerProductsPort;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
  private final CircuitBreaker circuitBreaker;
  private final String bootstrapServers;
  private final String consumerGroupId;

  private final Map<String, Sinks.One<ProductsQueryResponse>> pendingRequests =
      new ConcurrentHashMap<>();

  private KafkaReceiver<String, ProductsQueryResponse> responseReceiver;
  private Disposable subscription;

  /**
   * Constructor for the Kafka products adapter.
   *
   * @param kafkaSender      the Kafka sender
   * @param circuitBreaker   the circuit breaker for customer products queries
   * @param bootstrapServers the Kafka bootstrap servers
   * @param consumerGroupId  the consumer group ID
   */
  public KafkaCustomerProductsAdapter(
      KafkaSender<String, Object> kafkaSender,
      @Qualifier("customerProductsCircuitBreaker") CircuitBreaker circuitBreaker,
      @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
      @Value("${spring.kafka.consumer.group-id:customer-service-group}") String consumerGroupId) {
    this.kafkaSender = kafkaSender;
    this.circuitBreaker = circuitBreaker;
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

    log.debug("Querying customer products: customerId={}, circuitBreaker={}",
        customerId, circuitBreaker.getState());

    // Wrap the ENTIRE flow with Circuit Breaker using Mono.defer
    // This ensures CB captures ALL errors: connection, send, timeout, etc.
    return Mono.defer(() -> {
      ProductsQueryRequest request = ProductsQueryRequest.create(
          customerId, customerType, activeOnly);

      log.debug("Sending products query request: correlationId={}",
          request.getCorrelationId());

      Sinks.One<ProductsQueryResponse> sink = Sinks.one();
      pendingRequests.put(request.getCorrelationId(), sink);

      return sendRequest(request)
          .then(sink.asMono())
          .timeout(TIMEOUT)
          .doFinally(signal -> pendingRequests.remove(request.getCorrelationId()));
    })
        .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
        .doOnSuccess(result -> log.info(
            "Customer products query completed: customerId={}, totalProducts={}",
            customerId, result.getTotalProducts()))
        .doOnError(error -> log.error(
            "Customer products query failed for customerId={}: {}",
            customerId, error.getMessage()))
        .onErrorResume(error -> handleCircuitBreakerError(error, customerId));
  }

  /**
   * Handles circuit breaker and other errors with appropriate fallback.
   *
   * @param error      the error to handle
   * @param customerId the customer ID
   * @return fallback response with empty products
   */
  private Mono<ProductsQueryResponse> handleCircuitBreakerError(
      Throwable error,
      String customerId) {

    if (error instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
      log.warn("Circuit breaker is OPEN for customer products query. "
          + "Returning empty products for customerId={}", customerId);
    } else if (error instanceof java.util.concurrent.TimeoutException) {
      log.warn("Customer products query timeout for customerId={}. "
          + "Returning empty products", customerId);
    } else {
      log.error("Customer products query error for customerId={}: {}. "
          + "Returning empty products", customerId, error.getMessage());
    }
    return Mono.just(ProductsQueryResponse.empty("fallback", customerId));
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
