package com.nttd.banking.customer.domain.port.in;

import com.nttd.banking.customer.domain.model.Customer;
import reactor.core.publisher.Mono;

/**
 * Input port (use case) for updating customer profile.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface UpdateProfileUseCase {

  /**
   * Updates a customer's profile.
   *
   * @param id the customer ID
   * @param profileType the new profile type ("VIP" or "PYME")
   * @return Mono with the updated customer
   * @throws IllegalArgumentException if profile type is invalid for customer type
   * @throws RuntimeException if customer does not exist
   */
  Mono<Customer> updateProfile(String id, String profileType);
}
