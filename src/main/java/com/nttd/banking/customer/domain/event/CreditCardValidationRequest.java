package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing a request to validate if a customer has an active credit card.
 * This event is sent to credit-card-service via Kafka for VIP/PYME profile validation.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardValidationRequest {

  /**
   * Unique correlation ID for matching request with response.
   */
  private String correlationId;

  /**
   * ID of the customer to validate.
   */
  private String customerId;

  /**
   * Type of customer (PERSONAL or BUSINESS).
   */
  private String customerType;

  /**
   * Requested profile type (VIP or PYME).
   */
  private String requestedProfile;

  /**
   * Timestamp when the request was created.
   */
  private Instant timestamp;

  /**
   * Creates a new validation request for a customer.
   *
   * @param customerId the customer ID to validate
   * @param customerType the type of customer
   * @param requestedProfile the requested profile type
   * @return a new CreditCardValidationRequest
   */
  public static CreditCardValidationRequest create(
      String customerId,
      String customerType,
      String requestedProfile) {
    return CreditCardValidationRequest.builder()
        .correlationId(UUID.randomUUID().toString())
        .customerId(customerId)
        .customerType(customerType)
        .requestedProfile(requestedProfile)
        .timestamp(Instant.now())
        .build();
  }
}
