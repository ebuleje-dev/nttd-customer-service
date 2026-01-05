package com.nttd.banking.customer.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.SignerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AuthorizedSigner value object.
 * Tests validation logic and business rules.
 */
@DisplayName("AuthorizedSigner Value Object Tests")
class AuthorizedSignerTest {

  private AuthorizedSigner.AuthorizedSignerBuilder baseBuilder;

  @BeforeEach
  void setUp() {
    baseBuilder = AuthorizedSigner.builder()
        .firstName("Carlos")
        .lastName("García")
        .documentType(DocumentType.DNI)
        .documentNumber("87654321")
        .role(SignerRole.TITULAR);
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {

    @Test
    @DisplayName("Should validate successfully with all required fields")
    void shouldValidateSuccessfully() {
      AuthorizedSigner signer = baseBuilder.build();
      assertDoesNotThrow(() -> signer.validate());
    }

    @Test
    @DisplayName("Should fail validation when firstName is null")
    void shouldFailWhenFirstNameIsNull() {
      AuthorizedSigner signer = baseBuilder.firstName(null).build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El nombre del firmante es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail validation when firstName is empty")
    void shouldFailWhenFirstNameIsEmpty() {
      AuthorizedSigner signer = baseBuilder.firstName("  ").build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El nombre del firmante es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail validation when lastName is null")
    void shouldFailWhenLastNameIsNull() {
      AuthorizedSigner signer = baseBuilder.lastName(null).build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El apellido del firmante es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail validation when lastName is empty")
    void shouldFailWhenLastNameIsEmpty() {
      AuthorizedSigner signer = baseBuilder.lastName("  ").build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El apellido del firmante es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail validation when documentType is null")
    void shouldFailWhenDocumentTypeIsNull() {
      AuthorizedSigner signer = baseBuilder.documentType(null).build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El tipo de documento del firmante es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail validation when documentType is RUC")
    void shouldFailWhenDocumentTypeIsRuc() {
      AuthorizedSigner signer = baseBuilder.documentType(DocumentType.RUC).build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El tipo de documento del firmante no puede ser RUC (solo DNI, CEX o PASSPORT)",
          exception.getMessage());
    }

    @Test
    @DisplayName("Should accept DNI as valid document type")
    void shouldAcceptDniAsValidDocumentType() {
      AuthorizedSigner signer = baseBuilder.documentType(DocumentType.DNI).build();
      assertDoesNotThrow(() -> signer.validate());
    }

    @Test
    @DisplayName("Should accept CEX as valid document type")
    void shouldAcceptCexAsValidDocumentType() {
      AuthorizedSigner signer = baseBuilder.documentType(DocumentType.CEX).build();
      assertDoesNotThrow(() -> signer.validate());
    }

    @Test
    @DisplayName("Should accept PASSPORT as valid document type")
    void shouldAcceptPassportAsValidDocumentType() {
      AuthorizedSigner signer = baseBuilder.documentType(DocumentType.PASSPORT).build();
      assertDoesNotThrow(() -> signer.validate());
    }

    @Test
    @DisplayName("Should fail validation when documentNumber is null")
    void shouldFailWhenDocumentNumberIsNull() {
      AuthorizedSigner signer = baseBuilder.documentNumber(null).build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El número de documento del firmante es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail validation when documentNumber is empty")
    void shouldFailWhenDocumentNumberIsEmpty() {
      AuthorizedSigner signer = baseBuilder.documentNumber("  ").build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El número de documento del firmante es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail validation when role is null")
    void shouldFailWhenRoleIsNull() {
      AuthorizedSigner signer = baseBuilder.role(null).build();
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> signer.validate()
      );
      assertEquals("El rol del firmante es obligatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Should accept TITULAR as valid role")
    void shouldAcceptTitularAsValidRole() {
      AuthorizedSigner signer = baseBuilder.role(SignerRole.TITULAR).build();
      assertDoesNotThrow(() -> signer.validate());
    }

    @Test
    @DisplayName("Should accept AUTHORIZED as valid role")
    void shouldAcceptAuthorizedAsValidRole() {
      AuthorizedSigner signer = baseBuilder.role(SignerRole.AUTHORIZED).build();
      assertDoesNotThrow(() -> signer.validate());
    }
  }

  @Nested
  @DisplayName("Full Name Tests")
  class FullNameTests {

    @Test
    @DisplayName("Should return correct full name")
    void shouldReturnCorrectFullName() {
      AuthorizedSigner signer = baseBuilder
          .firstName("Carlos")
          .lastName("García")
          .build();
      assertEquals("Carlos García", signer.getFullName());
    }

    @Test
    @DisplayName("Should concatenate firstName and lastName with space")
    void shouldConcatenateNamesWithSpace() {
      AuthorizedSigner signer = baseBuilder
          .firstName("María")
          .lastName("López Pérez")
          .build();
      assertEquals("María López Pérez", signer.getFullName());
    }
  }

  @Nested
  @DisplayName("Immutability Tests")
  class ImmutabilityTests {

    @Test
    @DisplayName("Should create immutable value object")
    void shouldCreateImmutableObject() {
      AuthorizedSigner signer = baseBuilder.build();

      // Verify that getters return values
      assertNotNull(signer.getFirstName());
      assertNotNull(signer.getLastName());
      assertNotNull(signer.getDocumentType());
      assertNotNull(signer.getDocumentNumber());
      assertNotNull(signer.getRole());

      // Value objects should be immutable (no setters)
      // This is enforced by Lombok @Value annotation
    }

    @Test
    @DisplayName("Should support builder pattern")
    void shouldSupportBuilderPattern() {
      AuthorizedSigner signer = AuthorizedSigner.builder()
          .firstName("Pedro")
          .lastName("Ramírez")
          .documentType(DocumentType.PASSPORT)
          .documentNumber("AB123456")
          .role(SignerRole.AUTHORIZED)
          .build();

      assertEquals("Pedro", signer.getFirstName());
      assertEquals("Ramírez", signer.getLastName());
      assertEquals(DocumentType.PASSPORT, signer.getDocumentType());
      assertEquals("AB123456", signer.getDocumentNumber());
      assertEquals(SignerRole.AUTHORIZED, signer.getRole());
    }
  }

  @Nested
  @DisplayName("Equality Tests")
  class EqualityTests {

    @Test
    @DisplayName("Should be equal when all fields are equal")
    void shouldBeEqualWhenAllFieldsAreEqual() {
      AuthorizedSigner signer1 = baseBuilder.build();
      AuthorizedSigner signer2 = baseBuilder.build();

      assertEquals(signer1, signer2);
      assertEquals(signer1.hashCode(), signer2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when documentNumber differs")
    void shouldNotBeEqualWhenDocumentNumberDiffers() {
      AuthorizedSigner signer1 = baseBuilder.documentNumber("12345678").build();
      AuthorizedSigner signer2 = baseBuilder.documentNumber("87654321").build();

      assertNotEquals(signer1, signer2);
    }

    @Test
    @DisplayName("Should not be equal when comparing to null")
    void shouldNotBeEqualToNull() {
      AuthorizedSigner signer = baseBuilder.build();
      assertNotEquals(null, signer);
    }
  }
}
