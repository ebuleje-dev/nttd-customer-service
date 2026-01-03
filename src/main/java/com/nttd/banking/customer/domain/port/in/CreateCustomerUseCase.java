package com.nttd.banking.customer.domain.port.in;

import com.nttd.banking.customer.domain.model.Customer;
import reactor.core.publisher.Mono;

/**
 * Input port (use case) for creating a new customer.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface CreateCustomerUseCase {

  /**
   * Creates a new customer.
   *
   * @param customer the customer to create (PersonalCustomer or BusinessCustomer)
   * @return Mono with the created customer including generated ID
   * @throws IllegalArgumentException if customer data is invalid
   * @throws RuntimeException if a customer with the same email or document already exists
   */
  Mono<Customer> execute(Customer customer);
}
