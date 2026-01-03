package com.nttd.banking.customer.domain.port.out;

import com.nttd.banking.customer.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output port (repository) for customer persistence operations.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface CustomerRepository {

  /**
   * Saves a new customer or updates an existing one.
   *
   * @param customer the customer to save
   * @return Mono with the saved customer including generated ID if new
   */
  Mono<Customer> save(Customer customer);

  /**
   * Finds a customer by ID.
   *
   * @param id the customer ID
   * @return Mono with the found customer, or empty if not exists
   */
  Mono<Customer> findById(String id);

  /**
   * Finds a customer by email.
   *
   * @param email the customer email
   * @return Mono with the found customer, or empty if not exists
   */
  Mono<Customer> findByEmail(String email);

  /**
   * Finds a customer by document number.
   *
   * @param documentNumber the customer document number
   * @return Mono with the found customer, or empty if not exists
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

  /**
   * Deletes a customer by ID.
   *
   * @param id the customer ID to delete
   * @return Mono that completes when customer is deleted
   */
  Mono<Void> deleteById(String id);

  /**
   * Checks if a customer with the given email exists.
   *
   * @param email the email to check
   * @return Mono with true if exists, false otherwise
   */
  Mono<Boolean> existsByEmail(String email);

  /**
   * Checks if a customer with the given document number exists.
   *
   * @param documentNumber the document number to check
   * @return Mono with true if exists, false otherwise
   */
  Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
