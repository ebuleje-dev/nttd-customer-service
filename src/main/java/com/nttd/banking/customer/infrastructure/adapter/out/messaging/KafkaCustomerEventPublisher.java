package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.CustomerEvent;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.port.out.CustomerEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Kafka implementation of CustomerEventPublisher port.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCustomerEventPublisher implements CustomerEventPublisher {

  private final KafkaSender<String, Object> kafkaSender;
  private final CustomerEventMapper eventMapper;

  @Override
  public Mono<Void> publishCustomerCreated(Customer customer) {
    var event = eventMapper.toCreatedEvent(customer);
    log.info("Publishing CustomerCreatedEvent for customerId: {}", customer.getId());
    return sendEvent(KafkaTopics.CUSTOMER_EVENTS, customer.getId(), event);
  }

  @Override
  public Mono<Void> publishCustomerUpdated(Customer customer) {
    var event = eventMapper.toUpdatedEvent(customer);
    log.info("Publishing CustomerUpdatedEvent for customerId: {}", customer.getId());
    return sendEvent(KafkaTopics.CUSTOMER_EVENTS, customer.getId(), event);
  }

  @Override
  public Mono<Void> publishCustomerDeleted(String customerId) {
    var event = eventMapper.toDeletedEvent(customerId);
    log.info("Publishing CustomerDeletedEvent for customerId: {}", customerId);
    return sendEvent(KafkaTopics.CUSTOMER_EVENTS, customerId, event);
  }

  @Override
  public Mono<Void> publishProfileUpdated(Customer customer, String oldProfile, String newProfile) {
    var event = eventMapper.toProfileUpdatedEvent(customer, oldProfile, newProfile);
    log.info("Publishing ProfileUpdatedEvent for customerId: {} ({} -> {})",
        customer.getId(), oldProfile, newProfile);
    return sendEvent(KafkaTopics.PROFILE_EVENTS, customer.getId(), event);
  }

  /**
   * Sends an event to Kafka topic.
   *
   * @param topic the Kafka topic
   * @param key the message key
   * @param event the event to send
   * @return Mono that completes when event is sent
   */
  private Mono<Void> sendEvent(String topic, String key, CustomerEvent event) {
    ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topic, key, event);
    SenderRecord<String, Object, String> senderRecord =
        SenderRecord.create(producerRecord, event.getEventId());

    return kafkaSender.send(Mono.just(senderRecord))
        .doOnNext(result -> log.debug("Event sent successfully: topic={}, key={}, eventId={}",
            topic, key, event.getEventId()))
        .doOnError(error -> log.error("Failed to send event: topic={}, key={}, error={}",
            topic, key, error.getMessage()))
        .then();
  }
}
