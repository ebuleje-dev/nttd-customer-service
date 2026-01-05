package com.nttd.banking.customer.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.nttd.banking.customer.domain.model.enums.BusinessProfile;
import com.nttd.banking.customer.domain.model.enums.BusinessType;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.SignerRole;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BusinessCustomer domain model.
 */
@DisplayName("BusinessCustomer Domain Model Tests")
class BusinessCustomerTest {

  private BusinessCustomer createValidCustomer() {
    return BusinessCustomer.builder()
        .businessName("Empresa SAC")
        .businessType(BusinessType.SAC)
        .taxId("20123456789")
        .businessProfile(BusinessProfile.STANDARD)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .email("empresa@company.com")
        .phoneNumber("+51987654321")
        .address("Av. Principal 456, Lima, Perú")
        .status(CustomerStatus.ACTIVE)
        .authorizedSigners(new ArrayList<>())
        .build();
  }

  private AuthorizedSigner createValidSigner() {
    return AuthorizedSigner.builder()
        .firstName("Carlos")
        .lastName("García")
        .documentType(DocumentType.DNI)
        .documentNumber("87654321")
        .role(SignerRole.TITULAR)
        .build();
  }

  @Test
  @DisplayName("Should validate successfully with all required fields")
  void shouldValidateSuccessfully() {
    BusinessCustomer customer = createValidCustomer();
    assertDoesNotThrow(() -> customer.validate());
  }

  @Test
  @DisplayName("Should fail validation when businessName is null")
  void shouldFailWhenBusinessNameIsNull() {
    BusinessCustomer customer = createValidCustomer();
    customer.setBusinessName(null);
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> customer.validate()
    );
    assertEquals("La razón social es obligatoria para clientes empresariales",
        exception.getMessage());
  }

  @Test
  @DisplayName("Should fail validation when taxId length is not 11")
  void shouldFailWhenTaxIdLengthIsNot11() {
    BusinessCustomer customer = createValidCustomer();
    customer.setTaxId("123456789");
    customer.setDocumentNumber("123456789");
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> customer.validate()
    );
    assertEquals("El RUC debe tener exactamente 11 dígitos", exception.getMessage());
  }

  @Test
  @DisplayName("Should add authorized signer successfully")
  void shouldAddAuthorizedSigner() {
    BusinessCustomer customer = createValidCustomer();
    AuthorizedSigner signer = createValidSigner();
    customer.addAuthorizedSigner(signer);

    assertEquals(1, customer.getAuthorizedSigners().size());
    assertEquals(signer, customer.getAuthorizedSigners().get(0));
  }

  @Test
  @DisplayName("Should fail to add duplicate signer")
  void shouldFailToAddDuplicateSigner() {
    BusinessCustomer customer = createValidCustomer();
    AuthorizedSigner signer = createValidSigner();
    customer.addAuthorizedSigner(signer);

    AuthorizedSigner duplicateSigner = AuthorizedSigner.builder()
        .firstName("María")
        .lastName("López")
        .documentType(DocumentType.DNI)
        .documentNumber("87654321") // Same as first signer
        .role(SignerRole.AUTHORIZED)
        .build();

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> customer.addAuthorizedSigner(duplicateSigner)
    );
    assertEquals("Ya existe un firmante con el documento 87654321", exception.getMessage());
  }

  @Test
  @DisplayName("Should remove authorized signer successfully")
  void shouldRemoveAuthorizedSigner() {
    BusinessCustomer customer = createValidCustomer();
    AuthorizedSigner signer = createValidSigner();
    customer.addAuthorizedSigner(signer);

    boolean removed = customer.removeAuthorizedSigner("87654321");

    assertTrue(removed);
    assertEquals(0, customer.getAuthorizedSigners().size());
  }

  @Test
  @DisplayName("Should return false when removing non-existent signer")
  void shouldReturnFalseWhenRemovingNonExistentSigner() {
    BusinessCustomer customer = createValidCustomer();
    boolean removed = customer.removeAuthorizedSigner("99999999");
    assertFalse(removed);
  }

  @Test
  @DisplayName("Should identify PYME customer correctly")
  void shouldIdentifyPymeCustomer() {
    BusinessCustomer customer = createValidCustomer();
    customer.setBusinessProfile(BusinessProfile.PYME);
    assertTrue(customer.isPyme());
  }

  @Test
  @DisplayName("Should upgrade customer to PYME profile")
  void shouldUpgradeToPyme() {
    BusinessCustomer customer = createValidCustomer();
    customer.upgradeToPyme();
    assertEquals(BusinessProfile.PYME, customer.getBusinessProfile());
    assertTrue(customer.isPyme());
  }

  @Test
  @DisplayName("Should downgrade customer to STANDARD profile")
  void shouldDowngradeToStandard() {
    BusinessCustomer customer = createValidCustomer();
    customer.setBusinessProfile(BusinessProfile.PYME);
    customer.downgradeToStandard();
    assertEquals(BusinessProfile.STANDARD, customer.getBusinessProfile());
    assertFalse(customer.isPyme());
  }

  @Test
  @DisplayName("Should initialize with default values")
  void shouldInitializeWithDefaults() {
    BusinessCustomer customer = createValidCustomer();
    customer.setCustomerType(null);
    customer.setDocumentType(null);
    customer.setBusinessProfile(null);
    customer.setStatus(null);
    customer.setCreatedAt(null);
    customer.setUpdatedAt(null);
    customer.setAuthorizedSigners(null);

    customer.initializeDefaults();

    assertEquals(CustomerType.BUSINESS, customer.getCustomerType());
    assertEquals(DocumentType.RUC, customer.getDocumentType());
    assertEquals(BusinessProfile.STANDARD, customer.getBusinessProfile());
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    assertNotNull(customer.getCreatedAt());
    assertNotNull(customer.getUpdatedAt());
    assertNotNull(customer.getAuthorizedSigners());
    assertTrue(customer.getAuthorizedSigners().isEmpty());
  }

  @Nested
  @DisplayName("ValidateBusinessData Tests")
  class ValidateBusinessDataTests {

    @Test
    @DisplayName("Should fail when businessName is empty")
    void shouldFailWhenBusinessNameIsEmpty() {
      BusinessCustomer customer = createValidCustomer();
      customer.setBusinessName("   ");

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> customer.validate()
      );
      assertEquals("La razón social es obligatoria para clientes empresariales",
          exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when businessName is too short")
    void shouldFailWhenBusinessNameIsTooShort() {
      BusinessCustomer customer = createValidCustomer();
      customer.setBusinessName("A");

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> customer.validate()
      );
      assertEquals("La razón social debe tener entre 2 y 200 caracteres",
          exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when businessName is too long")
    void shouldFailWhenBusinessNameIsTooLong() {
      BusinessCustomer customer = createValidCustomer();
      customer.setBusinessName("A".repeat(201));

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> customer.validate()
      );
      assertEquals("La razón social debe tener entre 2 y 200 caracteres",
          exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when businessType is null")
    void shouldFailWhenBusinessTypeIsNull() {
      BusinessCustomer customer = createValidCustomer();
      customer.setBusinessType(null);

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> customer.validate()
      );
      assertEquals("El tipo de empresa es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when documentType is not RUC")
    void shouldFailWhenDocumentTypeIsNotRuc() {
      BusinessCustomer customer = createValidCustomer();
      customer.setDocumentType(DocumentType.DNI);

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> customer.validate()
      );
      assertEquals("El tipo de documento para clientes empresariales debe ser RUC",
          exception.getMessage());
    }

    @Test
    @DisplayName("Should validate successfully with minimum length businessName")
    void shouldValidateWithMinimumLengthBusinessName() {
      BusinessCustomer customer = createValidCustomer();
      customer.setBusinessName("AB");
      assertDoesNotThrow(() -> customer.validate());
    }

    @Test
    @DisplayName("Should validate successfully with maximum length businessName")
    void shouldValidateWithMaximumLengthBusinessName() {
      BusinessCustomer customer = createValidCustomer();
      customer.setBusinessName("A".repeat(200));
      assertDoesNotThrow(() -> customer.validate());
    }
  }

  @Nested
  @DisplayName("ValidateAuthorizedSigners Tests")
  class ValidateAuthorizedSignersTests {

    @Test
    @DisplayName("Should validate successfully with null signers list")
    void shouldValidateWithNullSignersList() {
      BusinessCustomer customer = createValidCustomer();
      customer.setAuthorizedSigners(null);
      assertDoesNotThrow(() -> customer.validate());
    }

    @Test
    @DisplayName("Should validate successfully with empty signers list")
    void shouldValidateWithEmptySignersList() {
      BusinessCustomer customer = createValidCustomer();
      customer.setAuthorizedSigners(new ArrayList<>());
      assertDoesNotThrow(() -> customer.validate());
    }

    @Test
    @DisplayName("Should validate successfully with valid signers")
    void shouldValidateWithValidSigners() {
      BusinessCustomer customer = createValidCustomer();
      AuthorizedSigner signer1 = createValidSigner();
      AuthorizedSigner signer2 = AuthorizedSigner.builder()
          .firstName("María")
          .lastName("López")
          .documentType(DocumentType.DNI)
          .documentNumber("12345678")
          .role(SignerRole.AUTHORIZED)
          .build();

      customer.setAuthorizedSigners(List.of(signer1, signer2));
      assertDoesNotThrow(() -> customer.validate());
    }

    @Test
    @DisplayName("Should fail when signer has null firstName")
    void shouldFailWhenSignerHasNullFirstName() {
      BusinessCustomer customer = createValidCustomer();
      AuthorizedSigner invalidSigner = AuthorizedSigner.builder()
          .firstName(null)
          .lastName("García")
          .documentType(DocumentType.DNI)
          .documentNumber("87654321")
          .role(SignerRole.TITULAR)
          .build();

      customer.setAuthorizedSigners(List.of(invalidSigner));

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> customer.validate()
      );
      assertTrue(exception.getMessage().contains("nombre"));
    }

    @Test
    @DisplayName("Should fail when signer has null documentNumber")
    void shouldFailWhenSignerHasNullDocumentNumber() {
      BusinessCustomer customer = createValidCustomer();
      AuthorizedSigner invalidSigner = AuthorizedSigner.builder()
          .firstName("Carlos")
          .lastName("García")
          .documentType(DocumentType.DNI)
          .documentNumber(null)
          .role(SignerRole.TITULAR)
          .build();

      customer.setAuthorizedSigners(List.of(invalidSigner));

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> customer.validate()
      );
      assertTrue(exception.getMessage().contains("documento"));
    }

    @Test
    @DisplayName("Should fail when signer has RUC document type")
    void shouldFailWhenSignerHasRucDocumentType() {
      BusinessCustomer customer = createValidCustomer();
      AuthorizedSigner invalidSigner = AuthorizedSigner.builder()
          .firstName("Carlos")
          .lastName("García")
          .documentType(DocumentType.RUC)
          .documentNumber("20123456789")
          .role(SignerRole.TITULAR)
          .build();

      customer.setAuthorizedSigners(List.of(invalidSigner));

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> customer.validate()
      );
      assertTrue(exception.getMessage().contains("RUC"));
    }

    @Test
    @DisplayName("Should validate multiple signers correctly")
    void shouldValidateMultipleSigners() {
      BusinessCustomer customer = createValidCustomer();
      List<AuthorizedSigner> signers = new ArrayList<>();

      for (int i = 0; i < 3; i++) {
        AuthorizedSigner signer = AuthorizedSigner.builder()
            .firstName("Signer" + i)
            .lastName("LastName" + i)
            .documentType(DocumentType.DNI)
            .documentNumber(String.format("8765432%d", i))
            .role(i == 0 ? SignerRole.TITULAR : SignerRole.AUTHORIZED)
            .build();
        signers.add(signer);
      }

      customer.setAuthorizedSigners(signers);
      assertDoesNotThrow(() -> customer.validate());
    }
  }
}
