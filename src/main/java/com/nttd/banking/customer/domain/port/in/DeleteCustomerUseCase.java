package com.nttd.banking.customer.domain.port.in;

import reactor.core.publisher.Mono;

/**
 * Input port (use case) for deleting a customer (soft delete).
 *
 * @author NTT Data
 * @version 1.0
 */
public interface DeleteCustomerUseCase {

  /**
   * Soft deletes a customer by setting status to INACTIVE.
   *
   * @param id the customer ID to delete
   * @return Mono that completes when customer is deleted
   * @throws RuntimeException if customer does not exist
   */
  Mono<Void> delete(String id);
}
