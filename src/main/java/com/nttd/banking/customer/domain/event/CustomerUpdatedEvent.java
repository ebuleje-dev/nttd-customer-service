package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a customer is updated.
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerUpdatedEvent extends CustomerEvent {

  private static final String EVENT_TYPE = "CUSTOMER_UPDATED";

  private String customerType;
  private String email;
  private String phoneNumber;
  private String address;

  /**
   * Creates a new CustomerUpdatedEvent with generated ID and current timestamp.
   *
   * @param customerId the customer identifier
   * @param customerType the type of customer
   * @param email the updated email
   * @param phoneNumber the updated phone number
   * @param address the updated address
   * @return a new CustomerUpdatedEvent instance
   */
  public static CustomerUpdatedEvent create(
      String customerId,
      String customerType,
      String email,
      String phoneNumber,
      String address) {
    return CustomerUpdatedEvent.builder()
        .eventId(generateEventId())
        .eventType(EVENT_TYPE)
        .customerId(customerId)
        .timestamp(Instant.now())
        .customerType(customerType)
        .email(email)
        .phoneNumber(phoneNumber)
        .address(address)
        .build();
  }
}
