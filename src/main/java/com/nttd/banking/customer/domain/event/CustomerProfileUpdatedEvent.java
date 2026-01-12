package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when a customer profile is updated (VIP/PYME).
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileUpdatedEvent {

  private String customerId;
  private String oldProfile;
  private String newProfile;
  private Instant updatedAt;

  /**
   * Creates a new CustomerProfileUpdatedEvent with current timestamp.
   *
   * @param customerId the customer identifier
   * @param oldProfile the previous profile (STANDARD/VIP/PYME)
   * @param newProfile the new profile (STANDARD/VIP/PYME)
   * @return a new CustomerProfileUpdatedEvent instance
   */
  public static CustomerProfileUpdatedEvent create(
      String customerId,
      String oldProfile,
      String newProfile) {
    return CustomerProfileUpdatedEvent.builder()
        .customerId(customerId)
        .oldProfile(oldProfile)
        .newProfile(newProfile)
        .updatedAt(Instant.now())
        .build();
  }
}
