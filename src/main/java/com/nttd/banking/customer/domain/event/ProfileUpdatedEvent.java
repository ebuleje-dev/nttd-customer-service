package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a customer profile is updated (VIP/PYME).
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProfileUpdatedEvent extends CustomerEvent {

  private static final String EVENT_TYPE = "PROFILE_UPDATED";

  private String customerType;
  private String oldProfile;
  private String newProfile;

  /**
   * Creates a new ProfileUpdatedEvent with generated ID and current timestamp.
   *
   * @param customerId the customer identifier
   * @param customerType the type of customer
   * @param oldProfile the previous profile
   * @param newProfile the new profile
   * @return a new ProfileUpdatedEvent instance
   */
  public static ProfileUpdatedEvent create(
      String customerId,
      String customerType,
      String oldProfile,
      String newProfile) {
    return ProfileUpdatedEvent.builder()
        .eventId(generateEventId())
        .eventType(EVENT_TYPE)
        .customerId(customerId)
        .timestamp(Instant.now())
        .customerType(customerType)
        .oldProfile(oldProfile)
        .newProfile(newProfile)
        .build();
  }
}
