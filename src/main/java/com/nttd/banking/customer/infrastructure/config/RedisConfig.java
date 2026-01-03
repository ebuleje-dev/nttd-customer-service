package com.nttd.banking.customer.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for reactive cache.
 *
 * @author NTT Data
 * @version 1.0
 */
@Configuration
public class RedisConfig {

  /**
   * Configures ReactiveRedisTemplate for generic objects.
   *
   * @param connectionFactory Redis connection factory
   * @return configured ReactiveRedisTemplate
   */
  @Bean
  public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory connectionFactory) {

    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType(Object.class)
        .build();

    objectMapper.activateDefaultTyping(
        typeValidator,
        ObjectMapper.DefaultTyping.NON_FINAL,
        com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
    );

    GenericJackson2JsonRedisSerializer serializer =
        new GenericJackson2JsonRedisSerializer(objectMapper);

    StringRedisSerializer stringSerializer = new StringRedisSerializer();

    RedisSerializationContext<String, Object> serializationContext =
        RedisSerializationContext.<String, Object>newSerializationContext(stringSerializer)
            .value(serializer)
            .hashKey(stringSerializer)
            .hashValue(serializer)
            .build();

    return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
  }
}
