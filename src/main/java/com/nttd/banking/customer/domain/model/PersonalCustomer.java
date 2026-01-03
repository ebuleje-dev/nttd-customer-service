package com.nttd.banking.customer.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.Gender;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain entity for personal customers (natural persons).
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalCustomer extends Customer {

  /** Customer's first name. */
  private String firstName;

  /** Customer's last name. */
  private String lastName;

  /** Customer's date of birth. */
  private Instant dateOfBirth;

  /** Customer's gender. */
  private Gender gender;

  /** Personal customer profile (STANDARD or VIP). */
  private PersonalProfile personalProfile;

  /**
   * Validates personal customer data including inherited fields.
   *
   * @throws IllegalArgumentException if any field is invalid
   */
  @Override
  public void validate() {
    super.validate();
    validatePersonalData();
    validateAge();
  }

  /**
   * Validates personal-specific data.
   *
   * @throws IllegalArgumentException if personal data is invalid
   */
  private void validatePersonalData() {
    if (firstName == null || firstName.trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre es obligatorio para clientes personales");
    }
    if (firstName.length() < 2 || firstName.length() > 100) {
      throw new IllegalArgumentException("El nombre debe tener entre 2 y 100 caracteres");
    }
    if (lastName == null || lastName.trim().isEmpty()) {
      throw new IllegalArgumentException("El apellido es obligatorio para clientes personales");
    }
    if (lastName.length() < 2 || lastName.length() > 100) {
      throw new IllegalArgumentException("El apellido debe tener entre 2 y 100 caracteres");
    }
    if (dateOfBirth == null) {
      throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
    }
  }

  /**
   * Validates that customer is at least 18 years old.
   *
   * @throws IllegalArgumentException if customer is underage
   */
  private void validateAge() {
    int age = getAge();
    if (age < 18) {
      throw new IllegalArgumentException(
          "El cliente debe ser mayor de 18 años. Edad actual: " + age);
    }
  }

  /**
   * Calculates customer's age in years.
   *
   * @return age in years
   */
  @JsonIgnore
  public int getAge() {
    LocalDate birthDate =
        LocalDate.ofInstant(dateOfBirth, ZoneId.systemDefault());
    LocalDate currentDate = LocalDate.now();
    return Period.between(birthDate, currentDate).getYears();
  }

  /**
   * Gets customer's full name.
   *
   * @return full name (firstName + lastName)
   */
  @JsonIgnore
  public String getFullName() {
    return firstName + " " + lastName;
  }

  /**
   * Checks if customer has VIP profile.
   *
   * @return true if profile is VIP
   */
  @JsonIgnore
  public boolean isVip() {
    return PersonalProfile.VIP.equals(this.personalProfile);
  }

  /**
   * Upgrades customer profile to VIP.
   */
  public void upgradeToVip() {
    this.personalProfile = PersonalProfile.VIP;
    touch();
  }

  /**
   * Downgrades customer profile to STANDARD.
   */
  public void downgradeToStandard() {
    this.personalProfile = PersonalProfile.STANDARD;
    touch();
  }

  /**
   * Initializes default values for a new personal customer.
   */
  @Override
  public void initializeDefaults() {
    super.initializeDefaults();
    super.setCustomerType(CustomerType.PERSONAL);
    if (this.personalProfile == null) {
      this.personalProfile = PersonalProfile.STANDARD;
    }
  }
}
