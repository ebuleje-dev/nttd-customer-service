package com.nttd.banking.customer.domain.model;

import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.SignerRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object representing an authorized signer for business customers.
 * This is a pure domain model without framework dependencies.
 * Jackson serialization is configured via MixIns in infrastructure layer.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizedSigner {

  /** Signer's first name. */
  private String firstName;

  /** Signer's last name. */
  private String lastName;

  /** Signer's document type (DNI, CEX, PASSPORT). */
  private DocumentType documentType;

  /** Signer's document number. */
  private String documentNumber;

  /** Signer's role (TITULAR or AUTHORIZED). */
  private SignerRole role;

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
  public String getFullName() {
    return firstName + " " + lastName;
  }
}
