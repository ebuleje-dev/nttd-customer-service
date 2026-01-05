package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.CreditCardValidationRequest;
import com.nttd.banking.customer.domain.event.CreditCardValidationResponse;
import com.nttd.banking.customer.domain.port.out.CreditCardValidationPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
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
@RequiredArgsConstructor
public class KafkaCreditCardValidationAdapter implements CreditCardValidationPort {

  private final KafkaSender<String, Object> kafkaSender;
  private final KafkaReceiver<String, CreditCardValidationResponse> validationResponseReceiver;

  private static final Duration VALIDATION_TIMEOUT = Duration.ofSeconds(30);

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

    CreditCardValidationRequest request = CreditCardValidationRequest.create(
        customerId, customerType, requestedProfile);

    log.info("Sending credit card validation request: customerId={}, correlationId={}",
        customerId, request.getCorrelationId());

    Sinks.One<CreditCardValidationResponse> responseSink = Sinks.one();
    pendingRequests.put(request.getCorrelationId(), responseSink);

    return sendValidationRequest(request)
        .then(responseSink.asMono())
        .timeout(VALIDATION_TIMEOUT)
        .map(CreditCardValidationResponse::isHasActiveCreditCard)
        .doOnSuccess(result -> log.info(
            "Credit card validation completed: customerId={}, hasActiveCreditCard={}",
            customerId, result))
        .doOnError(error -> {
          pendingRequests.remove(request.getCorrelationId());
          log.error("Credit card validation failed for customerId={}: {}",
              customerId, error.getMessage());
        })
        .onErrorResume(error -> {
          if (error instanceof java.util.concurrent.TimeoutException) {
            log.warn("Credit card validation timeout for customerId={}", customerId);
          }
          return Mono.just(false);
        });
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
