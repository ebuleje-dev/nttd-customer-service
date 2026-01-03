package com.nttd.banking.customer.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.SignerRole;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Immutable value object representing an authorized signer for business customers.
 *
 * @author NTT Data
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class AuthorizedSigner {

  /** Signer's first name. */
  String firstName;

  /** Signer's last name. */
  String lastName;

  /** Signer's document type (DNI, CEX, PASSPORT). */
  DocumentType documentType;

  /** Signer's document number. */
  String documentNumber;

  /** Signer's role (TITULAR or AUTHORIZED). */
  SignerRole role;

  /**
   * Validates authorized signer data.
   *
   * @throws IllegalArgumentException if any field is invalid
   */
  public void validate() {
    if (firstName == null || firstName.trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre del firmante es obligatorio");
    }
    if (lastName == null || lastName.trim().isEmpty()) {
      throw new IllegalArgumentException("El apellido del firmante es obligatorio");
    }
    if (documentType == null) {
      throw new IllegalArgumentException("El tipo de documento del firmante es obligatorio");
    }
    if (documentType == DocumentType.RUC) {
      throw new IllegalArgumentException(
          "El tipo de documento del firmante no puede ser RUC (solo DNI, CEX o PASSPORT)");
    }
    if (documentNumber == null || documentNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("El número de documento del firmante es obligatorio");
    }
    if (role == null) {
      throw new IllegalArgumentException("El rol del firmante es obligatorio");
    }
  }

  /**
   * Gets signer's full name.
   *
   * @return full name (firstName + lastName)
   */
  @JsonIgnore
  public String getFullName() {
    return firstName + " " + lastName;
  }
}
