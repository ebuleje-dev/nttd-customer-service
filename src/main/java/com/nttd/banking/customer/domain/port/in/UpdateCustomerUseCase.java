package com.nttd.banking.customer.domain.port.in;

import com.nttd.banking.customer.domain.model.Customer;
import reactor.core.publisher.Mono;

/**
 * Input port (use case) for updating an existing customer.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface UpdateCustomerUseCase {

  /**
   * Updates an existing customer.
   *
   * @param id the customer ID to update
   * @param customer the object with data to update
   * @return Mono with the updated customer
   * @throws IllegalArgumentException if data is invalid
   * @throws RuntimeException if customer does not exist
   */
  Mono<Customer> update(String id, Customer customer);
}
