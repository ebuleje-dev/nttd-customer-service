package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a customer is deleted (logical deletion).
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerDeletedEvent extends CustomerEvent {

  private static final String EVENT_TYPE = "CUSTOMER_DELETED";

  private String reason;

  /**
   * Creates a new CustomerDeletedEvent with generated ID and current timestamp.
   *
   * @param customerId the customer identifier
   * @return a new CustomerDeletedEvent instance
   */
  public static CustomerDeletedEvent create(String customerId) {
    return CustomerDeletedEvent.builder()
        .eventId(generateEventId())
        .eventType(EVENT_TYPE)
        .customerId(customerId)
        .timestamp(Instant.now())
        .reason("Customer deactivated")
        .build();
  }
}
