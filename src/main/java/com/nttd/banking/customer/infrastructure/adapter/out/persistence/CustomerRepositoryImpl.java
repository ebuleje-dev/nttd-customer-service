package com.nttd.banking.customer.infrastructure.adapter.out.persistence;

import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * MongoDB implementation of the customer repository.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

  private final CustomerMongoRepository mongoRepository;
  private final CustomerEntityMapper mapper;

  @Override
  public Mono<Customer> save(Customer customer) {
    log.debug("Saving customer to MongoDB: {}", customer.getId());

    return Mono.just(customer)
        .map(mapper::toPersistence)
        .flatMap(mongoRepository::save)
        .map(mapper::toDomain)
        .doOnSuccess(saved -> log.debug("Customer saved successfully: {}", saved.getId()))
        .doOnError(error -> log.error("Error saving customer: {}", error.getMessage()));
  }

  @Override
  public Mono<Customer> findById(String id) {
    log.debug("Finding customer by id: {}", id);

    return mongoRepository
        .findById(id)
        .map(mapper::toDomain)
        .doOnSuccess(
            customer -> {
              if (customer != null) {
                log.debug("Customer found: {}", id);
              } else {
                log.debug("Customer not found: {}", id);
              }
            });
  }

  @Override
  public Mono<Customer> findByEmail(String email) {
    log.debug("Finding customer by email: {}", email);

    return mongoRepository
        .findByEmail(email)
        .map(mapper::toDomain)
        .doOnSuccess(
            customer -> {
              if (customer != null) {
                log.debug("Customer found by email: {}", email);
              } else {
                log.debug("Customer not found by email: {}", email);
              }
            });
  }

  @Override
  public Mono<Customer> findByDocumentNumber(String documentNumber) {
    log.debug("Finding customer by document number: {}", documentNumber);

    return mongoRepository
        .findByDocumentNumber(documentNumber)
        .map(mapper::toDomain)
        .doOnSuccess(
            customer -> {
              if (customer != null) {
                log.debug("Customer found by document: {}", documentNumber);
              } else {
                log.debug("Customer not found by document: {}", documentNumber);
              }
            });
  }

  @Override
  public Flux<Customer> findAll() {
    log.debug("Finding all customers");

    return mongoRepository
        .findAll()
        .map(mapper::toDomain)
        .doOnComplete(() -> log.debug("Finished retrieving all customers"));
  }

  @Override
  public Flux<Customer> findAll(int page, int size) {
    log.debug("Finding all customers with pagination: page={}, size={}", page, size);

    int skip = page * size;

    return mongoRepository
        .findAll()
        .skip(skip)
        .take(size)
        .map(mapper::toDomain)
        .doOnComplete(() -> log.debug("Finished retrieving customers page={}, size={}", page, size));
  }

  @Override
  public Mono<Void> deleteById(String id) {
    log.debug("Deleting customer by id: {}", id);

    return mongoRepository
        .deleteById(id)
        .doOnSuccess(unused -> log.debug("Customer deleted: {}", id))
        .doOnError(error -> log.error("Error deleting customer {}: {}", id, error.getMessage()));
  }

  @Override
  public Mono<Boolean> existsByEmail(String email) {
    log.debug("Checking if customer exists by email: {}", email);

    return mongoRepository
        .existsByEmail(email)
        .doOnSuccess(exists -> log.debug("Customer exists by email {}: {}", email, exists));
  }

  @Override
  public Mono<Boolean> existsByDocumentNumber(String documentNumber) {
    log.debug("Checking if customer exists by document: {}", documentNumber);

    return mongoRepository
        .existsByDocumentNumber(documentNumber)
        .doOnSuccess(
            exists -> log.debug("Customer exists by document {}: {}", documentNumber, exists));
  }
}
