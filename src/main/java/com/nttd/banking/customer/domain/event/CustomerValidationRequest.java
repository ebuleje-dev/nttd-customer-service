package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing a request to validate if a customer exists.
 * This event is received from other services (e.g., account-service) via Kafka.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerValidationRequest {

  /**
   * Unique correlation ID for matching request with response.
   */
  private String correlationId;

  /**
   * ID of the customer to validate.
   */
  private String customerId;

  /**
   * Timestamp when the request was created.
   */
  private Instant requestedAt;
}
