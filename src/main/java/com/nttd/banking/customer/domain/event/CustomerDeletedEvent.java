package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when a customer is deleted (logical deletion).
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeletedEvent {

  private String customerId;
  private Instant deletedAt;

  /**
   * Creates a new CustomerDeletedEvent with current timestamp.
   *
   * @param customerId the customer identifier
   * @return a new CustomerDeletedEvent instance
   */
  public static CustomerDeletedEvent create(String customerId) {
    return CustomerDeletedEvent.builder()
        .customerId(customerId)
        .deletedAt(Instant.now())
        .build();
  }
}
