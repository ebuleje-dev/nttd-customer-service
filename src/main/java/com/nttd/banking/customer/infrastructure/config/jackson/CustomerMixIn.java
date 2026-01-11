package com.nttd.banking.customer.infrastructure.config.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;

/**
 * Jackson MixIn for Customer domain model.
 * This allows defining Jackson annotations outside the domain model,
 * keeping the domain pure and free from framework dependencies.
 *
 * @author NTT Data
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PersonalCustomer.class),
    @JsonSubTypes.Type(value = BusinessCustomer.class)
})
public abstract class CustomerMixIn {

  /**
   * Ignore the isActive() computed method during serialization.
   */
  @JsonIgnore
  public abstract boolean isActive();

  /**
   * Ignore the isBlocked() computed method during serialization.
   */
  @JsonIgnore
  public abstract boolean isBlocked();
}
