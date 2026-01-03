package com.nttd.banking.customer.domain.service;

import com.nttd.banking.customer.application.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.port.in.FindCustomerUseCase;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of the find customer use case with cache-aside pattern.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FindCustomerUseCaseImpl implements FindCustomerUseCase {

  private final CustomerRepository customerRepository;
  private final CustomerCacheRepository cacheRepository;

  private static final Duration CACHE_TTL = Duration.ofHours(1);

  @Override
  public Mono<Customer> findById(String id) {
    log.info("Finding customer by id: {}", id);

    return cacheRepository
        .findById(id)
        .switchIfEmpty(
            customerRepository
                .findById(id)
                .flatMap(customer -> cacheRepository.save(customer, CACHE_TTL).thenReturn(customer)))
        .switchIfEmpty(Mono.error(CustomerNotFoundException.byId(id)))
        .doOnSuccess(customer -> log.info("Customer found with id: {}", id))
        .doOnError(error -> log.warn("Customer not found with id: {}", id));
  }

  @Override
  public Mono<Customer> findByEmail(String email) {
    log.info("Finding customer by email: {}", email);

    return cacheRepository
        .findByEmail(email)
        .switchIfEmpty(
            customerRepository
                .findByEmail(email)
                .flatMap(customer -> cacheRepository.save(customer, CACHE_TTL).thenReturn(customer)))
        .switchIfEmpty(Mono.error(CustomerNotFoundException.byEmail(email)))
        .doOnSuccess(customer -> log.info("Customer found with email: {}", email))
        .doOnError(error -> log.warn("Customer not found with email: {}", email));
  }

  @Override
  public Mono<Customer> findByDocumentNumber(String documentNumber) {
    log.info("Finding customer by document: {}", documentNumber);

    return cacheRepository
        .findByDocumentNumber(documentNumber)
        .switchIfEmpty(
            customerRepository
                .findByDocumentNumber(documentNumber)
                .flatMap(customer -> cacheRepository.save(customer, CACHE_TTL).thenReturn(customer)))
        .switchIfEmpty(Mono.error(CustomerNotFoundException.byDocument(documentNumber)))
        .doOnSuccess(customer -> log.info("Customer found with document: {}", documentNumber))
        .doOnError(error -> log.warn("Customer not found with document: {}", documentNumber));
  }

  @Override
  public Flux<Customer> findAll() {
    log.info("Finding all customers");

    return customerRepository
        .findAll()
        .doOnComplete(() -> log.info("Finished retrieving all customers"));
  }

  @Override
  public Flux<Customer> findAll(int page, int size) {
    log.info("Finding all customers with pagination: page={}, size={}", page, size);

    return customerRepository
        .findAll(page, size)
        .doOnComplete(() -> log.info("Finished retrieving customers page={}", page));
  }
}
