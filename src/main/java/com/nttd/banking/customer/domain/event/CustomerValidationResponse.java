package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing the response to a customer validation request.
 * Sent back to the requesting service with customer existence and details.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerValidationResponse {

  /**
   * Correlation ID matching the original request.
   */
  private String correlationId;

  /**
   * ID of the customer that was validated.
   */
  private String customerId;

  /**
   * Indicates whether the customer exists.
   */
  private boolean exists;

  /**
   * Type of customer (PERSONAL or BUSINESS) if exists=true.
   */
  private String customerType;

  /**
   * Customer profile (STANDARD, VIP, or PYME) if exists=true.
   */
  private String customerProfile;

  /**
   * Indicates whether the customer is active.
   */
  private boolean isActive;

  /**
   * Timestamp when the response was created.
   */
  private Instant respondedAt;

  /**
   * Creates a response for an existing customer.
   *
   * @param correlationId the correlation ID from the request
   * @param customerId the customer ID
   * @param customerType the customer type
   * @param customerProfile the customer profile
   * @param isActive whether the customer is active
   * @return a new CustomerValidationResponse
   */
  public static CustomerValidationResponse exists(
      String correlationId,
      String customerId,
      String customerType,
      String customerProfile,
      boolean isActive) {
    return CustomerValidationResponse.builder()
        .correlationId(correlationId)
        .customerId(customerId)
        .exists(true)
        .customerType(customerType)
        .customerProfile(customerProfile)
        .isActive(isActive)
        .respondedAt(Instant.now())
        .build();
  }

  /**
   * Creates a response for a non-existing customer.
   *
   * @param correlationId the correlation ID from the request
   * @param customerId the customer ID
   * @return a new CustomerValidationResponse indicating customer does not exist
   */
  public static CustomerValidationResponse notExists(
      String correlationId,
      String customerId) {
    return CustomerValidationResponse.builder()
        .correlationId(correlationId)
        .customerId(customerId)
        .exists(false)
        .customerType(null)
        .customerProfile(null)
        .isActive(false)
        .respondedAt(Instant.now())
        .build();
  }
}
