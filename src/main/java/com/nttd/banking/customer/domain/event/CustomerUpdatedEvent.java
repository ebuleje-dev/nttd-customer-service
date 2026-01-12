package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when a customer is updated.
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdatedEvent {

  private String customerId;
  private String customerType;
  private String customerProfile;
  private Instant updatedAt;

  /**
   * Creates a new CustomerUpdatedEvent with current timestamp.
   *
   * @param customerId the customer identifier
   * @param customerType the type of customer (PERSONAL/BUSINESS)
   * @param customerProfile the customer profile (STANDARD/VIP/PYME)
   * @return a new CustomerUpdatedEvent instance
   */
  public static CustomerUpdatedEvent create(
      String customerId,
      String customerType,
      String customerProfile) {
    return CustomerUpdatedEvent.builder()
        .customerId(customerId)
        .customerType(customerType)
        .customerProfile(customerProfile)
        .updatedAt(Instant.now())
        .build();
  }
}
