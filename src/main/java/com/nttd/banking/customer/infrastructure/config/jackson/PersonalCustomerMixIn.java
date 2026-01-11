package com.nttd.banking.customer.infrastructure.config.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson MixIn for PersonalCustomer domain model.
 * This allows defining Jackson annotations outside the domain model,
 * keeping the domain pure and free from framework dependencies.
 *
 * @author NTT Data
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PersonalCustomerMixIn extends CustomerMixIn {

  /**
   * Ignore the getAge() computed method during serialization.
   */
  @JsonIgnore
  public abstract int getAge();

  /**
   * Ignore the getFullName() computed method during serialization.
   */
  @JsonIgnore
  public abstract String getFullName();

  /**
   * Ignore the isVip() computed method during serialization.
   */
  @JsonIgnore
  public abstract boolean isVip();
}
