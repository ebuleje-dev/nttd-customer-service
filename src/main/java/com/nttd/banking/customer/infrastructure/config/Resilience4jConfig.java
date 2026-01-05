package com.nttd.banking.customer.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Resilience4j circuit breakers and time limiters.
 *
 * <p>This configuration uses the auto-configured registries from Spring Boot
 * based on the application.yml properties under resilience4j.circuitbreaker.instances
 * and resilience4j.timelimiter.instances.</p>
 *
 * <p>Circuit Breaker instances:</p>
 * <ul>
 *   <li><b>creditCardValidation</b>: For credit card validation via Kafka</li>
 *   <li><b>customerProducts</b>: For customer products query via Kafka</li>
 * </ul>
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Configuration
public class Resilience4jConfig {

  public static final String CREDIT_CARD_VALIDATION = "creditCardValidation";
  public static final String CUSTOMER_PRODUCTS = "customerProducts";

  /**
   * Configures event listeners for the credit card validation circuit breaker.
   *
   * @param registry the auto-configured circuit breaker registry
   * @return the circuit breaker instance with event listeners configured
   */
  @Bean
  public CircuitBreaker creditCardValidationCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreaker circuitBreaker = registry.circuitBreaker(CREDIT_CARD_VALIDATION);

    circuitBreaker.getEventPublisher()
        .onStateTransition(event ->
            log.info("Circuit Breaker [{}] state changed from {} to {}",
                event.getCircuitBreakerName(),
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState()))
        .onSlowCallRateExceeded(event ->
            log.warn("Circuit Breaker [{}] slow call rate exceeded: {}%",
                event.getCircuitBreakerName(),
                event.getSlowCallRate()))
        .onFailureRateExceeded(event ->
            log.warn("Circuit Breaker [{}] failure rate exceeded: {}%",
                event.getCircuitBreakerName(),
                event.getFailureRate()))
        .onCallNotPermitted(event ->
            log.warn("Circuit Breaker [{}] call not permitted - circuit is OPEN",
                event.getCircuitBreakerName()))
        .onError(event ->
            log.debug("Circuit Breaker [{}] recorded error: {}",
                event.getCircuitBreakerName(),
                event.getThrowable().getMessage()))
        .onSuccess(event ->
            log.debug("Circuit Breaker [{}] recorded success, duration: {}ms",
                event.getCircuitBreakerName(),
                event.getElapsedDuration().toMillis()));

    log.info("Circuit Breaker [{}] configured and ready", CREDIT_CARD_VALIDATION);
    return circuitBreaker;
  }

  /**
   * Configures event listeners for the customer products circuit breaker.
   *
   * @param registry the auto-configured circuit breaker registry
   * @return the circuit breaker instance with event listeners configured
   */
  @Bean
  public CircuitBreaker customerProductsCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreaker circuitBreaker = registry.circuitBreaker(CUSTOMER_PRODUCTS);

    circuitBreaker.getEventPublisher()
        .onStateTransition(event ->
            log.info("Circuit Breaker [{}] state changed from {} to {}",
                event.getCircuitBreakerName(),
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState()))
        .onSlowCallRateExceeded(event ->
            log.warn("Circuit Breaker [{}] slow call rate exceeded: {}%",
                event.getCircuitBreakerName(),
                event.getSlowCallRate()))
        .onFailureRateExceeded(event ->
            log.warn("Circuit Breaker [{}] failure rate exceeded: {}%",
                event.getCircuitBreakerName(),
                event.getFailureRate()))
        .onCallNotPermitted(event ->
            log.warn("Circuit Breaker [{}] call not permitted - circuit is OPEN",
                event.getCircuitBreakerName()))
        .onError(event ->
            log.debug("Circuit Breaker [{}] recorded error: {}",
                event.getCircuitBreakerName(),
                event.getThrowable().getMessage()))
        .onSuccess(event ->
            log.debug("Circuit Breaker [{}] recorded success, duration: {}ms",
                event.getCircuitBreakerName(),
                event.getElapsedDuration().toMillis()));

    log.info("Circuit Breaker [{}] configured and ready", CUSTOMER_PRODUCTS);
    return circuitBreaker;
  }

  /**
   * Gets the credit card validation time limiter.
   *
   * @param registry the auto-configured time limiter registry
   * @return the time limiter instance
   */
  @Bean
  public TimeLimiter creditCardValidationTimeLimiter(TimeLimiterRegistry registry) {
    return registry.timeLimiter(CREDIT_CARD_VALIDATION);
  }

  /**
   * Gets the customer products time limiter.
   *
   * @param registry the auto-configured time limiter registry
   * @return the time limiter instance
   */
  @Bean
  public TimeLimiter customerProductsTimeLimiter(TimeLimiterRegistry registry) {
    return registry.timeLimiter(CUSTOMER_PRODUCTS);
  }
}
