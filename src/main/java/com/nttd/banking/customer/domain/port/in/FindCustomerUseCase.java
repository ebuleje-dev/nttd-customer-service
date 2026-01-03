package com.nttd.banking.customer.domain.port.in;

import com.nttd.banking.customer.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Input port (use case) for finding customers.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface FindCustomerUseCase {

  /**
   * Finds a customer by ID.
   *
   * @param id the customer ID
   * @return Mono with the found customer, or error if not exists
   */
  Mono<Customer> findById(String id);

  /**
   * Finds a customer by email.
   *
   * @param email the customer email
   * @return Mono with the found customer, or error if not exists
   */
  Mono<Customer> findByEmail(String email);

  /**
   * Finds a customer by document number.
   *
   * @param documentNumber the customer document number
   * @return Mono with the found customer, or error if not exists
   */
  Mono<Customer> findByDocumentNumber(String documentNumber);

  /**
   * Gets all customers.
   *
   * @return Flux with all customers
   */
  Flux<Customer> findAll();

  /**
   * Gets all customers with pagination.
   *
   * @param page page number (starts at 0)
   * @param size page size
   * @return Flux with customers for the requested page
   */
  Flux<Customer> findAll(int page, int size);
}
