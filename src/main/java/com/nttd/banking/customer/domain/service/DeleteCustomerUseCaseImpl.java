package com.nttd.banking.customer.domain.service;

import com.nttd.banking.customer.application.exception.CustomerHasActiveProductsException;
import com.nttd.banking.customer.application.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.port.in.DeleteCustomerUseCase;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerEventPublisher;
import com.nttd.banking.customer.domain.port.out.CustomerProductsPort;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the delete customer use case (soft delete).
 * Validates that customer has no active products before deletion.
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
  private final CustomerEventPublisher eventPublisher;
  private final CustomerProductsPort customerProductsPort;

  @Override
  public Mono<Void> delete(String id) {
    log.info("Deleting customer (logical) with id: {}", id);

    return customerRepository
        .findById(id)
        .switchIfEmpty(Mono.error(CustomerNotFoundException.byId(id)))
        .flatMap(this::validateNoActiveProducts)
        .flatMap(this::performSoftDelete)
        .then()
        .doOnSuccess(unused -> log.info("Customer deleted successfully: {}", id))
        .doOnError(error -> log.error("Error deleting customer {}: {}", id, error.getMessage()));
  }

  /**
   * Validates that the customer has no active products.
   *
   * @param customer the customer to validate
   * @return Mono emitting the customer if validation passes
   * @throws CustomerHasActiveProductsException if customer has active products
   */
  private Mono<Customer> validateNoActiveProducts(Customer customer) {
    log.debug("Validating no active products for customer: {}", customer.getId());

    return customerProductsPort
        .queryCustomerProducts(customer.getId(), customer.getCustomerType().name())
        .flatMap(response -> {
          if (response.hasActiveProducts()) {
            log.warn("Customer {} has {} active products, cannot delete",
                customer.getId(), response.getTotalProducts());
            return Mono.error(new CustomerHasActiveProductsException(
                customer.getId(), response.getTotalProducts()));
          }
          log.debug("Customer {} has no active products, proceeding with deletion",
              customer.getId());
          return Mono.just(customer);
        });
  }

  /**
   * Performs the soft delete operation.
   *
   * @param customer the customer to delete
   * @return Mono that completes when deletion is done
   */
  private Mono<Void> performSoftDelete(Customer customer) {
    customer.setStatus(CustomerStatus.INACTIVE);
    customer.setUpdatedAt(Instant.now());

    return customerRepository.save(customer)
        .then(evictCache(customer))
        .then(eventPublisher.publishCustomerDeleted(customer.getId()));
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
