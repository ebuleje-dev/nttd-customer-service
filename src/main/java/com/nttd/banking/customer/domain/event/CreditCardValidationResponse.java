package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing the response from credit-card-service regarding
 * whether a customer has an active credit card.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardValidationResponse {

  /**
   * Correlation ID matching the original request.
   */
  private String correlationId;

  /**
   * ID of the customer that was validated.
   */
  private String customerId;

  /**
   * Indicates whether the customer has an active credit card.
   */
  private boolean hasActiveCreditCard;

  /**
   * Optional message providing additional details.
   */
  private String message;

  /**
   * Timestamp when the response was created.
   */
  private Instant timestamp;

  /**
   * Creates a successful validation response.
   *
   * @param correlationId the correlation ID from the request
   * @param customerId the customer ID
   * @param hasActiveCreditCard whether the customer has an active credit card
   * @return a new CreditCardValidationResponse
   */
  public static CreditCardValidationResponse success(
      String correlationId,
      String customerId,
      boolean hasActiveCreditCard) {
    return CreditCardValidationResponse.builder()
        .correlationId(correlationId)
        .customerId(customerId)
        .hasActiveCreditCard(hasActiveCreditCard)
        .message(hasActiveCreditCard ? "Customer has active credit card"
            : "Customer does not have active credit card")
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Creates an error response for validation failure.
   *
   * @param correlationId the correlation ID from the request
   * @param customerId the customer ID
   * @param errorMessage the error message
   * @return a new CreditCardValidationResponse indicating failure
   */
  public static CreditCardValidationResponse error(
      String correlationId,
      String customerId,
      String errorMessage) {
    return CreditCardValidationResponse.builder()
        .correlationId(correlationId)
        .customerId(customerId)
        .hasActiveCreditCard(false)
        .message(errorMessage)
        .timestamp(Instant.now())
        .build();
  }
}
