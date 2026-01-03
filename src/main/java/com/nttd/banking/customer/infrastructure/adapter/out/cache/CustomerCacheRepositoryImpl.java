package com.nttd.banking.customer.infrastructure.adapter.out.cache;

import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Redis implementation of the customer cache repository.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomerCacheRepositoryImpl implements CustomerCacheRepository {

  private final ReactiveRedisTemplate<String, Object> redisTemplate;

  private static final String KEY_PREFIX_ID = "customer:id:";
  private static final String KEY_PREFIX_EMAIL = "customer:email:";
  private static final String KEY_PREFIX_DOCUMENT = "customer:document:";

  @Override
  public Mono<Void> save(Customer customer, Duration ttl) {
    if (customer == null || customer.getId() == null) {
      return Mono.empty();
    }

    log.debug("Saving customer to cache: id={}, ttl={}", customer.getId(), ttl);

    return Mono.zip(
            saveByKey(KEY_PREFIX_ID + customer.getId(), customer, ttl),
            saveByKey(KEY_PREFIX_EMAIL + customer.getEmail(), customer, ttl),
            saveByKey(KEY_PREFIX_DOCUMENT + customer.getDocumentNumber(), customer, ttl))
        .then()
        .doOnSuccess(
            unused ->
                log.debug(
                    "Customer cached successfully: id={}, email={}, document={}",
                    customer.getId(),
                    customer.getEmail(),
                    customer.getDocumentNumber()))
        .doOnError(
            error ->
                log.error("Error caching customer {}: {}", customer.getId(), error.getMessage()));
  }

  @Override
  public Mono<Customer> findById(String id) {
    if (id == null) {
      return Mono.empty();
    }

    log.debug("Looking for customer in cache by id: {}", id);

    return redisTemplate
        .opsForValue()
        .get(KEY_PREFIX_ID + id)
        .cast(Customer.class)
        .doOnSuccess(
            customer -> {
              if (customer != null) {
                log.debug("Cache HIT for customer id: {}", id);
              } else {
                log.debug("Cache MISS for customer id: {}", id);
              }
            })
        .doOnError(error -> log.error("Error reading cache for id {}: {}", id, error.getMessage()))
        .onErrorResume(error -> Mono.empty());
  }

  @Override
  public Mono<Customer> findByEmail(String email) {
    if (email == null) {
      return Mono.empty();
    }

    log.debug("Looking for customer in cache by email: {}", email);

    return redisTemplate
        .opsForValue()
        .get(KEY_PREFIX_EMAIL + email)
        .cast(Customer.class)
        .doOnSuccess(
            customer -> {
              if (customer != null) {
                log.debug("Cache HIT for customer email: {}", email);
              } else {
                log.debug("Cache MISS for customer email: {}", email);
              }
            })
        .doOnError(
            error -> log.error("Error reading cache for email {}: {}", email, error.getMessage()))
        .onErrorResume(error -> Mono.empty());
  }

  @Override
  public Mono<Customer> findByDocumentNumber(String documentNumber) {
    if (documentNumber == null) {
      return Mono.empty();
    }

    log.debug("Looking for customer in cache by document: {}", documentNumber);

    return redisTemplate
        .opsForValue()
        .get(KEY_PREFIX_DOCUMENT + documentNumber)
        .cast(Customer.class)
        .doOnSuccess(
            customer -> {
              if (customer != null) {
                log.debug("Cache HIT for customer document: {}", documentNumber);
              } else {
                log.debug("Cache MISS for customer document: {}", documentNumber);
              }
            })
        .doOnError(
            error ->
                log.error(
                    "Error reading cache for document {}: {}", documentNumber, error.getMessage()))
        .onErrorResume(error -> Mono.empty());
  }

  @Override
  public Mono<Void> evict(String id) {
    if (id == null) {
      return Mono.empty();
    }

    log.debug("Evicting customer from cache by id: {}", id);

    return redisTemplate
        .delete(KEY_PREFIX_ID + id)
        .then()
        .doOnSuccess(unused -> log.debug("Customer evicted from cache: id={}", id))
        .doOnError(
            error -> log.error("Error evicting customer {}: {}", id, error.getMessage()));
  }

  @Override
  public Mono<Void> evictByEmail(String email) {
    if (email == null) {
      return Mono.empty();
    }

    log.debug("Evicting customer from cache by email: {}", email);

    return redisTemplate
        .delete(KEY_PREFIX_EMAIL + email)
        .then()
        .doOnSuccess(unused -> log.debug("Customer evicted from cache: email={}", email))
        .doOnError(
            error -> log.error("Error evicting customer by email {}: {}", email, error.getMessage()));
  }

  @Override
  public Mono<Void> evictByDocumentNumber(String documentNumber) {
    if (documentNumber == null) {
      return Mono.empty();
    }

    log.debug("Evicting customer from cache by document: {}", documentNumber);

    return redisTemplate
        .delete(KEY_PREFIX_DOCUMENT + documentNumber)
        .then()
        .doOnSuccess(
            unused -> log.debug("Customer evicted from cache: document={}", documentNumber))
        .doOnError(
            error ->
                log.error(
                    "Error evicting customer by document {}: {}",
                    documentNumber,
                    error.getMessage()));
  }

  @Override
  public Mono<Void> evictAll() {
    log.warn("Evicting ALL customers from cache");

    return redisTemplate
        .keys(KEY_PREFIX_ID + "*")
        .flatMap(redisTemplate::delete)
        .then(redisTemplate.keys(KEY_PREFIX_EMAIL + "*").flatMap(redisTemplate::delete).then())
        .then(redisTemplate.keys(KEY_PREFIX_DOCUMENT + "*").flatMap(redisTemplate::delete).then())
        .doOnSuccess(unused -> log.warn("All customers evicted from cache"))
        .doOnError(error -> log.error("Error evicting all customers: {}", error.getMessage()));
  }

  private Mono<Boolean> saveByKey(String key, Object value, Duration ttl) {
    return redisTemplate.opsForValue().set(key, value, ttl);
  }
}
