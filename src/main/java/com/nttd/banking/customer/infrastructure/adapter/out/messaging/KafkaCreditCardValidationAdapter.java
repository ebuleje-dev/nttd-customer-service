package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.CreditCardValidationRequest;
import com.nttd.banking.customer.domain.event.CreditCardValidationResponse;
import com.nttd.banking.customer.domain.port.out.CreditCardValidationPort;
import com.nttd.banking.customer.infrastructure.config.Resilience4jConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Kafka adapter for credit card validation communication.
 * Implements request-reply pattern using correlation IDs.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Component
public class KafkaCreditCardValidationAdapter implements CreditCardValidationPort {

  private final KafkaSender<String, Object> kafkaSender;
  private final KafkaReceiver<String, CreditCardValidationResponse> validationResponseReceiver;
  private final CircuitBreaker circuitBreaker;

  private static final Duration VALIDATION_TIMEOUT = Duration.ofSeconds(30);

  /**
   * Constructor with dependencies.
   *
   * @param kafkaSender the Kafka sender
   * @param validationResponseReceiver the Kafka receiver for validation responses
   * @param circuitBreaker the circuit breaker for credit card validation
   */
  public KafkaCreditCardValidationAdapter(
      KafkaSender<String, Object> kafkaSender,
      KafkaReceiver<String, CreditCardValidationResponse> validationResponseReceiver,
      @Qualifier("creditCardValidationCircuitBreaker") CircuitBreaker circuitBreaker) {
    this.kafkaSender = kafkaSender;
    this.validationResponseReceiver = validationResponseReceiver;
    this.circuitBreaker = circuitBreaker;
  }

  /**
   * Map to hold pending validation requests, keyed by correlation ID.
   */
  private final Map<String, Sinks.One<CreditCardValidationResponse>> pendingRequests =
      new ConcurrentHashMap<>();

  private Disposable responseSubscription;

  /**
   * Starts listening for validation responses when the component is initialized.
   */
  @PostConstruct
  public void startListening() {
    log.info("Starting credit card validation response listener");
    responseSubscription = validationResponseReceiver
        .receive()
        .doOnNext(record -> {
          CreditCardValidationResponse response = record.value();
          log.debug("Received validation response: correlationId={}, hasActiveCreditCard={}",
              response.getCorrelationId(), response.isHasActiveCreditCard());

          Sinks.One<CreditCardValidationResponse> sink =
              pendingRequests.remove(response.getCorrelationId());

          if (sink != null) {
            sink.tryEmitValue(response);
          } else {
            log.warn("No pending request found for correlationId: {}",
                response.getCorrelationId());
          }

          record.receiverOffset().acknowledge();
        })
        .doOnError(error -> log.error("Error receiving validation response", error))
        .subscribe();
  }

  /**
   * Stops the response listener when the component is destroyed.
   */
  @PreDestroy
  public void stopListening() {
    log.info("Stopping credit card validation response listener");
    if (responseSubscription != null && !responseSubscription.isDisposed()) {
      responseSubscription.dispose();
    }
  }

  @Override
  public Mono<Boolean> validateHasActiveCreditCard(
      String customerId,
      String customerType,
      String requestedProfile) {

    log.info("Validating credit card: customerId={}, circuitBreaker={}",
        customerId, circuitBreaker.getState());

    // Wrap the ENTIRE flow with Circuit Breaker using Mono.defer
    // This ensures CB captures ALL errors: connection, send, timeout, etc.
    return Mono.defer(() -> {
      log.debug("Creating validation request for customerId={}", customerId);

      CreditCardValidationRequest request = CreditCardValidationRequest.create(
          customerId, customerType, requestedProfile);

      log.debug("Sending credit card validation request: correlationId={}",
          request.getCorrelationId());

      Sinks.One<CreditCardValidationResponse> responseSink = Sinks.one();
      pendingRequests.put(request.getCorrelationId(), responseSink);

      return sendValidationRequest(request)
          .doOnSuccess(v -> log.debug("Request sent successfully, waiting for response: "
              + "correlationId={}", request.getCorrelationId()))
          .doOnError(sendError -> log.error("Failed to send request: correlationId={}, "
              + "error={}", request.getCorrelationId(), sendError.getMessage()))
          .then(responseSink.asMono())
          .timeout(VALIDATION_TIMEOUT)
          .map(CreditCardValidationResponse::isHasActiveCreditCard)
          .doFinally(signal -> {
            log.debug("Cleaning up pending request: correlationId={}, signal={}",
                request.getCorrelationId(), signal);
            pendingRequests.remove(request.getCorrelationId());
          });
    })
        .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
        .doOnSuccess(result -> log.info(
            "Credit card validation completed: customerId={}, hasActiveCreditCard={}",
            customerId, result))
        .doOnError(error -> log.error(
            "Credit card validation failed for customerId={}: {} [{}]",
            customerId, error.getMessage(), error.getClass().getSimpleName()))
        .onErrorResume(error -> {
          log.warn("Handling error for customerId={}, returning fallback. Error: {} [{}]",
              customerId, error.getMessage(), error.getClass().getSimpleName());
          return handleCircuitBreakerError(error);
        });
  }

  /**
   * Handles circuit breaker and other errors with appropriate fallback.
   *
   * @param error the error to handle
   * @return fallback value (false) with appropriate logging
   */
  private Mono<Boolean> handleCircuitBreakerError(Throwable error) {
    if (error instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
      log.warn("Circuit breaker is OPEN for credit card validation. "
          + "Returning fallback value: false");
    } else if (error instanceof java.util.concurrent.TimeoutException) {
      log.warn("Credit card validation timeout. Returning fallback value: false");
    } else {
      log.error("Credit card validation error: {}. Returning fallback value: false",
          error.getMessage());
    }
    return Mono.just(false);
  }

  /**
   * Sends a validation request to Kafka.
   *
   * @param request the validation request
   * @return Mono that completes when the message is sent
   */
  private Mono<Void> sendValidationRequest(CreditCardValidationRequest request) {
    ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
        KafkaTopics.CREDIT_CARD_VALIDATION_REQUEST,
        request.getCustomerId(),
        request);

    SenderRecord<String, Object, String> senderRecord =
        SenderRecord.create(producerRecord, request.getCorrelationId());

    return kafkaSender.send(Mono.just(senderRecord))
        .doOnNext(result -> log.debug("Validation request sent: topic={}, key={}, correlationId={}",
            KafkaTopics.CREDIT_CARD_VALIDATION_REQUEST,
            request.getCustomerId(),
            request.getCorrelationId()))
        .doOnError(error -> log.error("Failed to send validation request: {}",
            error.getMessage()))
        .then();
  }
}
