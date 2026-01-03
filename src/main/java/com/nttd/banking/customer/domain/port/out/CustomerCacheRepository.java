package com.nttd.banking.customer.domain.port.out;

import com.nttd.banking.customer.domain.model.Customer;
import java.time.Duration;
import reactor.core.publisher.Mono;

/**
 * Output port (cache repository) for customer cache operations.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface CustomerCacheRepository {

  /**
   * Saves a customer in cache.
   *
   * @param customer the customer to cache
   * @param ttl time to live for the cache entry
   * @return Mono that completes when customer is cached
   */
  Mono<Void> save(Customer customer, Duration ttl);

  /**
   * Finds a customer in cache by ID.
   *
   * @param id the customer ID
   * @return Mono with the cached customer, or empty if not in cache
   */
  Mono<Customer> findById(String id);

  /**
   * Finds a customer in cache by email.
   *
   * @param email the customer email
   * @return Mono with the cached customer, or empty if not in cache
   */
  Mono<Customer> findByEmail(String email);

  /**
   * Finds a customer in cache by document number.
   *
   * @param documentNumber the customer document number
   * @return Mono with the cached customer, or empty if not in cache
   */
  Mono<Customer> findByDocumentNumber(String documentNumber);

  /**
   * Evicts a customer from cache by ID.
   *
   * @param id the customer ID to evict
   * @return Mono that completes when customer is evicted
   */
  Mono<Void> evict(String id);

  /**
   * Evicts a customer from cache by email.
   *
   * @param email the customer email to evict
   * @return Mono that completes when customer is evicted
   */
  Mono<Void> evictByEmail(String email);

  /**
   * Evicts a customer from cache by document number.
   *
   * @param documentNumber the customer document number to evict
   * @return Mono that completes when customer is evicted
   */
  Mono<Void> evictByDocumentNumber(String documentNumber);

  /**
   * Evicts all customers from cache.
   *
   * @return Mono that completes when cache is cleared
   */
  Mono<Void> evictAll();
}
