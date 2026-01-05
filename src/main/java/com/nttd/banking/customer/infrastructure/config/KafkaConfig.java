package com.nttd.banking.customer.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nttd.banking.customer.domain.event.CreditCardValidationResponse;
import com.nttd.banking.customer.infrastructure.adapter.out.messaging.KafkaTopics;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

/**
 * Kafka configuration for reactive event publishing.
 *
 * @author NTT Data
 * @version 1.0
 */
@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id:customer-service-group}")
  private String consumerGroupId;

  /**
   * Configures the reactive Kafka sender for publishing events.
   *
   * @return configured KafkaSender instance
   */
  @Bean
  public KafkaSender<String, Object> kafkaSender() {
    Map<String, Object> producerProps = new HashMap<>();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
    producerProps.put(ProducerConfig.RETRIES_CONFIG, 3);
    producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

    SenderOptions<String, Object> senderOptions = SenderOptions.create(producerProps);

    return KafkaSender.create(senderOptions);
  }

  /**
   * Configures ObjectMapper for JSON serialization with Java 8 time support.
   *
   * @return configured ObjectMapper instance
   */
  @Bean
  public ObjectMapper kafkaObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  /**
   * Configures the Kafka receiver for credit card validation responses.
   *
   * @return configured KafkaReceiver instance
   */
  @Bean
  public KafkaReceiver<String, CreditCardValidationResponse> validationResponseReceiver() {
    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-validation");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CreditCardValidationResponse.class);
    consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    ReceiverOptions<String, CreditCardValidationResponse> receiverOptions =
        ReceiverOptions.<String, CreditCardValidationResponse>create(consumerProps)
            .subscription(Collections.singletonList(
                KafkaTopics.CREDIT_CARD_VALIDATION_RESPONSE));

    return KafkaReceiver.create(receiverOptions);
  }
}
