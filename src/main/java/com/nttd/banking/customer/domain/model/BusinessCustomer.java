package com.nttd.banking.customer.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nttd.banking.customer.domain.model.enums.BusinessProfile;
import com.nttd.banking.customer.domain.model.enums.BusinessType;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain entity for business customers (legal entities).
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessCustomer extends Customer {

  /** Company's legal business name. */
  private String businessName;

  /** Company type (SAC, SRL, SA, EIRL). */
  private BusinessType businessType;

  /** Company's tax ID (RUC - 11 digits). */
  private String taxId;

  /** Business customer profile (STANDARD or PYME). */
  private BusinessProfile businessProfile;

  /** List of authorized signers. */
  private List<AuthorizedSigner> authorizedSigners;

  /**
   * Validates business customer data including inherited fields.
   *
   * @throws IllegalArgumentException if any field is invalid
   */
  @Override
  public void validate() {
    super.validate();
    validateBusinessData();
    validateTaxId();
    validateAuthorizedSigners();
  }

  /**
   * Validates business-specific data.
   *
   * @throws IllegalArgumentException if business data is invalid
   */
  private void validateBusinessData() {
    if (businessName == null || businessName.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "La razón social es obligatoria para clientes empresariales");
    }
    if (businessName.length() < 2 || businessName.length() > 200) {
      throw new IllegalArgumentException("La razón social debe tener entre 2 y 200 caracteres");
    }
    if (businessType == null) {
      throw new IllegalArgumentException("El tipo de empresa es obligatorio");
    }
    if (super.getDocumentType() != DocumentType.RUC) {
      throw new IllegalArgumentException(
          "El tipo de documento para clientes empresariales debe ser RUC");
    }
  }

  /**
   * Validates the tax ID (RUC).
   *
   * @throws IllegalArgumentException if tax ID is invalid
   */
  private void validateTaxId() {
    if (taxId == null || taxId.trim().isEmpty()) {
      throw new IllegalArgumentException("El RUC es obligatorio para clientes empresariales");
    }
    if (taxId.length() != 11) {
      throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos");
    }
    if (!taxId.matches("^[0-9]{11}$")) {
      throw new IllegalArgumentException("El RUC solo debe contener dígitos");
    }
    if (!taxId.equals(super.getDocumentNumber())) {
      throw new IllegalArgumentException(
          "El RUC debe ser igual al número de documento del cliente");
    }
  }

  /**
   * Validates the list of authorized signers.
   *
   * @throws IllegalArgumentException if any signer is invalid
   */
  private void validateAuthorizedSigners() {
    if (authorizedSigners != null && !authorizedSigners.isEmpty()) {
      for (AuthorizedSigner signer : authorizedSigners) {
        signer.validate();
      }
    }
  }

  /**
   * Adds an authorized signer to the company.
   *
   * @param signer the signer to add
   * @throws IllegalArgumentException if signer is invalid or already exists
   */
  public void addAuthorizedSigner(AuthorizedSigner signer) {
    if (signer == null) {
      throw new IllegalArgumentException("El firmante no puede ser nulo");
    }
    signer.validate();

    if (this.authorizedSigners == null) {
      this.authorizedSigners = new ArrayList<>();
    }

    boolean exists =
        this.authorizedSigners.stream()
            .anyMatch(s -> s.getDocumentNumber().equals(signer.getDocumentNumber()));

    if (exists) {
      throw new IllegalArgumentException(
          "Ya existe un firmante con el documento " + signer.getDocumentNumber());
    }

    this.authorizedSigners.add(signer);
    touch();
  }

  /**
   * Removes an authorized signer by document number.
   *
   * @param documentNumber document number of the signer to remove
   * @return true if removed, false if not found
   */
  public boolean removeAuthorizedSigner(String documentNumber) {
    if (this.authorizedSigners == null || this.authorizedSigners.isEmpty()) {
      return false;
    }

    boolean removed =
        this.authorizedSigners.removeIf(s -> s.getDocumentNumber().equals(documentNumber));

    if (removed) {
      touch();
    }

    return removed;
  }

  /**
   * Checks if company has PYME profile.
   *
   * @return true if profile is PYME
   */
  @JsonIgnore
  public boolean isPyme() {
    return BusinessProfile.PYME.equals(this.businessProfile);
  }

  /**
   * Upgrades company profile to PYME.
   */
  public void upgradeToPyme() {
    this.businessProfile = BusinessProfile.PYME;
    touch();
  }

  /**
   * Downgrades company profile to STANDARD.
   */
  public void downgradeToStandard() {
    this.businessProfile = BusinessProfile.STANDARD;
    touch();
  }

  /**
   * Initializes default values for a new business customer.
   */
  @Override
  public void initializeDefaults() {
    super.initializeDefaults();
    super.setCustomerType(CustomerType.BUSINESS);
    super.setDocumentType(DocumentType.RUC);
    if (this.businessProfile == null) {
      this.businessProfile = BusinessProfile.STANDARD;
    }
    if (this.authorizedSigners == null) {
      this.authorizedSigners = new ArrayList<>();
    }
  }
}
