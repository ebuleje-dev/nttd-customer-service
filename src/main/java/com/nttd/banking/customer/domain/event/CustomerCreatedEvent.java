package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a new customer is created.
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerCreatedEvent extends CustomerEvent {

  private static final String EVENT_TYPE = "CUSTOMER_CREATED";

  private String customerType;
  private String documentType;
  private String documentNumber;
  private String email;
  private String profile;

  /**
   * Creates a new CustomerCreatedEvent with generated ID and current timestamp.
   *
   * @param customerId the customer identifier
   * @param customerType the type of customer (PERSONAL/BUSINESS)
   * @param documentType the document type
   * @param documentNumber the document number
   * @param email the customer email
   * @param profile the customer profile
   * @return a new CustomerCreatedEvent instance
   */
  public static CustomerCreatedEvent create(
      String customerId,
      String customerType,
      String documentType,
      String documentNumber,
      String email,
      String profile) {
    return CustomerCreatedEvent.builder()
        .eventId(generateEventId())
        .eventType(EVENT_TYPE)
        .customerId(customerId)
        .timestamp(Instant.now())
        .customerType(customerType)
        .documentType(documentType)
        .documentNumber(documentNumber)
        .email(email)
        .profile(profile)
        .build();
  }
}
