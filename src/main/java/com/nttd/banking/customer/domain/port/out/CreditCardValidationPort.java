package com.nttd.banking.customer.domain.port.out;

import reactor.core.publisher.Mono;

/**
 * Output port for validating if a customer has an active credit card.
 * This validation is required for VIP/PYME profile upgrades.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface CreditCardValidationPort {

  /**
   * Validates if a customer has an active credit card.
   * This validation is performed asynchronously via Kafka communication
   * with the credit-card-service.
   *
   * @param customerId the ID of the customer to validate
   * @param customerType the type of customer (PERSONAL or BUSINESS)
   * @param requestedProfile the requested profile type (VIP or PYME)
   * @return Mono emitting true if the customer has an active credit card, false otherwise
   */
  Mono<Boolean> validateHasActiveCreditCard(
      String customerId,
      String customerType,
      String requestedProfile);
}
