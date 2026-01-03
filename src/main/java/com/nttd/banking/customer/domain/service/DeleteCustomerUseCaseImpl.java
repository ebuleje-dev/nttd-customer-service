package com.nttd.banking.customer.domain.service;

import com.nttd.banking.customer.application.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.port.in.DeleteCustomerUseCase;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the delete customer use case (soft delete).
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCustomerUseCaseImpl implements DeleteCustomerUseCase {

  private final CustomerRepository customerRepository;
  private final CustomerCacheRepository cacheRepository;

  @Override
  public Mono<Void> delete(String id) {
    log.info("Deleting customer (logical) with id: {}", id);

    return customerRepository
        .findById(id)
        .switchIfEmpty(Mono.error(CustomerNotFoundException.byId(id)))
        .flatMap(
            customer -> {
              customer.setStatus(CustomerStatus.INACTIVE);
              customer.setUpdatedAt(Instant.now());
              return customerRepository.save(customer).then(evictCache(customer));
            })
        .then()
        .doOnSuccess(unused -> log.info("Customer deleted successfully: {}", id))
        .doOnError(error -> log.error("Error deleting customer {}: {}", id, error.getMessage()));
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
