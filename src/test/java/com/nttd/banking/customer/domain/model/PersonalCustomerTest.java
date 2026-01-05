package com.nttd.banking.customer.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.Gender;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PersonalCustomer domain model.
 */
@DisplayName("PersonalCustomer Domain Model Tests")
class PersonalCustomerTest {

  private PersonalCustomer createValidCustomer() {
    Instant birthDate = LocalDate.of(1990, 1, 1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();

    return PersonalCustomer.builder()
        .firstName("Juan")
        .lastName("Pérez")
        .dateOfBirth(birthDate)
        .gender(Gender.MALE)
        .personalProfile(PersonalProfile.STANDARD)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .email("juan.perez@email.com")
        .phoneNumber("+51987654321")
        .address("Av. Lima 123, Lima, Perú")
        .status(CustomerStatus.ACTIVE)
        .build();
  }

  @Test
  @DisplayName("Should validate successfully with all required fields")
  void shouldValidateSuccessfully() {
    PersonalCustomer customer = createValidCustomer();
    assertDoesNotThrow(() -> customer.validate());
  }

  @Test
  @DisplayName("Should fail validation when firstName is null")
  void shouldFailWhenFirstNameIsNull() {
    PersonalCustomer customer = createValidCustomer();
    customer.setFirstName(null);
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> customer.validate()
    );
    assertEquals("El nombre es obligatorio para clientes personales", exception.getMessage());
  }

  @Test
  @DisplayName("Should fail validation when customer is underage")
  void shouldFailWhenCustomerIsUnderage() {
    Instant underageBirthDate = LocalDate.now().minusYears(17)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();
    PersonalCustomer customer = createValidCustomer();
    customer.setDateOfBirth(underageBirthDate);
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> customer.validate()
    );
    assertTrue(exception.getMessage().contains("El cliente debe ser mayor de 18 años"));
  }

  @Test
  @DisplayName("Should calculate correct age")
  void shouldCalculateCorrectAge() {
    PersonalCustomer customer = createValidCustomer();
    int age = customer.getAge();
    assertTrue(age >= 18);
  }

  @Test
  @DisplayName("Should return full name correctly")
  void shouldReturnFullName() {
    PersonalCustomer customer = createValidCustomer();
    assertEquals("Juan Pérez", customer.getFullName());
  }

  @Test
  @DisplayName("Should identify VIP customer correctly")
  void shouldIdentifyVipCustomer() {
    PersonalCustomer customer = createValidCustomer();
    customer.setPersonalProfile(PersonalProfile.VIP);
    assertTrue(customer.isVip());
  }

  @Test
  @DisplayName("Should upgrade customer to VIP profile")
  void shouldUpgradeToVip() {
    PersonalCustomer customer = createValidCustomer();
    customer.upgradeToVip();
    assertEquals(PersonalProfile.VIP, customer.getPersonalProfile());
    assertTrue(customer.isVip());
  }

  @Test
  @DisplayName("Should downgrade customer to STANDARD profile")
  void shouldDowngradeToStandard() {
    PersonalCustomer customer = createValidCustomer();
    customer.setPersonalProfile(PersonalProfile.VIP);
    customer.downgradeToStandard();
    assertEquals(PersonalProfile.STANDARD, customer.getPersonalProfile());
    assertFalse(customer.isVip());
  }

  @Test
  @DisplayName("Should initialize with default values")
  void shouldInitializeWithDefaults() {
    PersonalCustomer customer = createValidCustomer();
    customer.setCustomerType(null);
    customer.setPersonalProfile(null);
    customer.setStatus(null);
    customer.setCreatedAt(null);
    customer.setUpdatedAt(null);

    customer.initializeDefaults();

    assertEquals(CustomerType.PERSONAL, customer.getCustomerType());
    assertEquals(PersonalProfile.STANDARD, customer.getPersonalProfile());
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    assertNotNull(customer.getCreatedAt());
    assertNotNull(customer.getUpdatedAt());
  }

  @Test
  @DisplayName("Should activate customer")
  void shouldActivateCustomer() {
    PersonalCustomer customer = createValidCustomer();
    customer.setStatus(CustomerStatus.INACTIVE);
    customer.activate();
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    assertTrue(customer.isActive());
  }

  @Test
  @DisplayName("Should deactivate customer")
  void shouldDeactivateCustomer() {
    PersonalCustomer customer = createValidCustomer();
    customer.deactivate();
    assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
    assertFalse(customer.isActive());
  }

  @Test
  @DisplayName("Should block customer")
  void shouldBlockCustomer() {
    PersonalCustomer customer = createValidCustomer();
    customer.block();
    assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
    assertTrue(customer.isBlocked());
  }
}
