package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when a new customer is created.
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent {

  private String customerId;
  private String customerType;
  private String customerProfile;
  private Instant createdAt;

  /**
   * Creates a new CustomerCreatedEvent with current timestamp.
   *
   * @param customerId the customer identifier
   * @param customerType the type of customer (PERSONAL/BUSINESS)
   * @param customerProfile the customer profile (STANDARD/VIP/PYME)
   * @return a new CustomerCreatedEvent instance
   */
  public static CustomerCreatedEvent create(
      String customerId,
      String customerType,
      String customerProfile) {
    return CustomerCreatedEvent.builder()
        .customerId(customerId)
        .customerType(customerType)
        .customerProfile(customerProfile)
        .createdAt(Instant.now())
        .build();
  }
}
