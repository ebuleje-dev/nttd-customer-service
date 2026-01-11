package com.nttd.banking.customer.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nttd.banking.customer.domain.model.AuthorizedSigner;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.infrastructure.config.jackson.AuthorizedSignerMixIn;
import com.nttd.banking.customer.infrastructure.config.jackson.BusinessCustomerMixIn;
import com.nttd.banking.customer.infrastructure.config.jackson.CustomerMixIn;
import com.nttd.banking.customer.infrastructure.config.jackson.PersonalCustomerMixIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for reactive cache.
 * Uses Jackson MixIns to configure serialization without polluting domain models.
 *
 * @author NTT Data
 * @version 1.0
 */
@Configuration
public class RedisConfig {

  /**
   * Configures ReactiveRedisTemplate for generic objects.
   * Registers Jackson MixIns to handle computed properties in domain models.
   *
   * @param connectionFactory Redis connection factory
   * @return configured ReactiveRedisTemplate
   */
  @Bean
  public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory connectionFactory) {

    ObjectMapper objectMapper = createObjectMapper();

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

  /**
   * Creates and configures ObjectMapper with MixIns for domain models.
   * This keeps Jackson configuration in infrastructure layer,
   * maintaining domain model purity.
   *
   * @return configured ObjectMapper
   */
  private ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    // Register Java 8 time module
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Register MixIns to handle computed properties without polluting domain models
    objectMapper.addMixIn(Customer.class, CustomerMixIn.class);
    objectMapper.addMixIn(PersonalCustomer.class, PersonalCustomerMixIn.class);
    objectMapper.addMixIn(BusinessCustomer.class, BusinessCustomerMixIn.class);
    objectMapper.addMixIn(AuthorizedSigner.class, AuthorizedSignerMixIn.class);

    // Configure polymorphic type handling using @class property
    // This is consistent with the @JsonTypeInfo in CustomerMixIn
    PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType(Object.class)
        .build();

    objectMapper.activateDefaultTyping(
        typeValidator,
        ObjectMapper.DefaultTyping.NON_FINAL,
        com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
    );

    return objectMapper;
  }
}
