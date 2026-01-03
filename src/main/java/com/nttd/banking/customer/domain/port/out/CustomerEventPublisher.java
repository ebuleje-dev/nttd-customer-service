package com.nttd.banking.customer.domain.port.out;

import com.nttd.banking.customer.domain.model.Customer;
import reactor.core.publisher.Mono;

/**
 * Output port (event publisher) for publishing customer domain events.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface CustomerEventPublisher {

  /**
   * Publishes a customer created event.
   *
   * @param customer the created customer
   * @return Mono that completes when event is published
   */
  Mono<Void> publishCustomerCreated(Customer customer);

  /**
   * Publishes a customer updated event.
   *
   * @param customer the updated customer
   * @return Mono that completes when event is published
   */
  Mono<Void> publishCustomerUpdated(Customer customer);

  /**
   * Publishes a customer deleted event.
   *
   * @param customerId the deleted customer ID
   * @return Mono that completes when event is published
   */
  Mono<Void> publishCustomerDeleted(String customerId);

  /**
   * Publishes a profile updated event.
   *
   * @param customer the customer whose profile was updated
   * @param oldProfile the previous profile
   * @param newProfile the new profile
   * @return Mono that completes when event is published
   */
  Mono<Void> publishProfileUpdated(Customer customer, String oldProfile, String newProfile);
}
