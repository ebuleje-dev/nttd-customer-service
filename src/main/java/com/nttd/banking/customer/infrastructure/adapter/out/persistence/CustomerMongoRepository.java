package com.nttd.banking.customer.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Spring Data MongoDB repository for customer entities.
 *
 * @author NTT Data
 * @version 1.0
 */
@Repository
public interface CustomerMongoRepository extends ReactiveMongoRepository<CustomerEntity, String> {

  /**
   * Finds a customer by email.
   *
   * @param email customer email
   * @return Mono with the found customer or empty if not exists
   */
  Mono<CustomerEntity> findByEmail(String email);

  /**
   * Finds a customer by document number.
   *
   * @param documentNumber customer document number
   * @return Mono with the found customer or empty if not exists
   */
  Mono<CustomerEntity> findByDocumentNumber(String documentNumber);

  /**
   * Checks if a customer with the given email exists.
   *
   * @param email email to check
   * @return Mono with true if exists, false otherwise
   */
  Mono<Boolean> existsByEmail(String email);

  /**
   * Checks if a customer with the given document number exists.
   *
   * @param documentNumber document number to check
   * @return Mono with true if exists, false otherwise
   */
  Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
