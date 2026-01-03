package com.nttd.banking.customer.domain.service;

import com.nttd.banking.customer.application.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.port.in.UpdateCustomerUseCase;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the update customer use case.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCustomerUseCaseImpl implements UpdateCustomerUseCase {

  private final CustomerRepository customerRepository;
  private final CustomerCacheRepository cacheRepository;

  private static final Duration CACHE_TTL = Duration.ofHours(1);

  @Override
  public Mono<Customer> update(String id, Customer updates) {
    log.info("Updating customer with id: {}", id);

    return customerRepository
        .findById(id)
        .switchIfEmpty(Mono.error(CustomerNotFoundException.byId(id)))
        .flatMap(existing -> applyUpdates(existing, updates))
        .flatMap(customerRepository::save)
        .flatMap(
            updated ->
                evictCache(updated)
                    .then(cacheRepository.save(updated, CACHE_TTL))
                    .thenReturn(updated))
        .doOnSuccess(
            updated -> log.info("Customer updated successfully with id: {}", updated.getId()))
        .doOnError(error -> log.error("Error updating customer {}: {}", id, error.getMessage()));
  }

  /**
   * Applies allowed updates to existing customer.
   *
   * @param existing existing customer
   * @param updates fields to update
   * @return Mono with updated customer
   */
  private Mono<Customer> applyUpdates(Customer existing, Customer updates) {
    if (updates.getEmail() != null) {
      existing.setEmail(updates.getEmail());
    }

    if (updates.getPhoneNumber() != null) {
      existing.setPhoneNumber(updates.getPhoneNumber());
    }

    if (updates.getAddress() != null) {
      existing.setAddress(updates.getAddress());
    }

    existing.setUpdatedAt(Instant.now());

    log.debug("Applied updates to customer: {}", existing.getId());
    return Mono.just(existing);
  }

  /**
   * Evicts all cache entries for a customer.
   *
   * @param customer customer to evict from cache
   * @return Mono that completes when cache is evicted
   */
  private Mono<Void> evictCache(Customer customer) {
    return Mono.when(
        cacheRepository.evict(customer.getId()),
        cacheRepository.evictByEmail(customer.getEmail()),
        cacheRepository.evictByDocumentNumber(customer.getDocumentNumber()));
  }
}
