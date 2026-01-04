package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.CustomerCreatedEvent;
import com.nttd.banking.customer.domain.event.CustomerDeletedEvent;
import com.nttd.banking.customer.domain.event.CustomerUpdatedEvent;
import com.nttd.banking.customer.domain.event.ProfileUpdatedEvent;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting domain models to Kafka events.
 *
 * @author NTT Data
 * @version 1.0
 */
@Component
public class CustomerEventMapper {

  /**
   * Converts a Customer to CustomerCreatedEvent.
   *
   * @param customer the created customer
   * @return the event to publish
   */
  public CustomerCreatedEvent toCreatedEvent(Customer customer) {
    return CustomerCreatedEvent.create(
        customer.getId(),
        customer.getCustomerType().name(),
        customer.getDocumentType().name(),
        customer.getDocumentNumber(),
        customer.getEmail(),
        extractProfile(customer));
  }

  /**
   * Converts a Customer to CustomerUpdatedEvent.
   *
   * @param customer the updated customer
   * @return the event to publish
   */
  public CustomerUpdatedEvent toUpdatedEvent(Customer customer) {
    return CustomerUpdatedEvent.create(
        customer.getId(),
        customer.getCustomerType().name(),
        customer.getEmail(),
        customer.getPhoneNumber(),
        customer.getAddress());
  }

  /**
   * Creates a CustomerDeletedEvent from customer ID.
   *
   * @param customerId the deleted customer ID
   * @return the event to publish
   */
  public CustomerDeletedEvent toDeletedEvent(String customerId) {
    return CustomerDeletedEvent.create(customerId);
  }

  /**
   * Creates a ProfileUpdatedEvent from customer and profile changes.
   *
   * @param customer the customer
   * @param oldProfile the previous profile
   * @param newProfile the new profile
   * @return the event to publish
   */
  public ProfileUpdatedEvent toProfileUpdatedEvent(
      Customer customer, String oldProfile, String newProfile) {
    return ProfileUpdatedEvent.create(
        customer.getId(),
        customer.getCustomerType().name(),
        oldProfile,
        newProfile);
  }

  /**
   * Extracts the profile string from a customer based on type.
   *
   * @param customer the customer
   * @return the profile as string
   */
  private String extractProfile(Customer customer) {
    if (customer instanceof PersonalCustomer personalCustomer) {
      return personalCustomer.getPersonalProfile() != null
          ? personalCustomer.getPersonalProfile().name()
          : "STANDARD";
    } else if (customer instanceof BusinessCustomer businessCustomer) {
      return businessCustomer.getBusinessProfile() != null
          ? businessCustomer.getBusinessProfile().name()
          : "STANDARD";
    }
    return "STANDARD";
  }
}
