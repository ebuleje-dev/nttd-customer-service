package com.nttd.banking.customer.infrastructure.adapter.out.persistence;

import com.nttd.banking.customer.domain.model.enums.Gender;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB entity for personal customers.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "customers")
public class PersonalCustomerEntity extends CustomerEntity {

  /** Customer's first name. */
  private String firstName;

  /** Customer's last name. */
  private String lastName;

  /** Customer's date of birth. */
  private Instant dateOfBirth;

  /** Customer's gender. */
  private Gender gender;

  /** Personal customer profile. */
  private PersonalProfile personalProfile;
}
