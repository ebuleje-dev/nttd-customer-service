package com.nttd.banking.customer.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.nttd.banking.customer.domain.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.exception.ProfileUpdateNotAllowedException;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.model.enums.BusinessProfile;
import com.nttd.banking.customer.domain.model.enums.BusinessType;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.Gender;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import com.nttd.banking.customer.domain.port.out.CreditCardValidationPort;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerEventPublisher;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for UpdateProfileUseCaseImpl using reactive testing with StepVerifier.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProfileUseCase Tests")
class UpdateProfileUseCaseImplTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private CustomerCacheRepository cacheRepository;

  @Mock
  private CreditCardValidationPort creditCardValidationPort;

  @Mock
  private CustomerEventPublisher eventPublisher;

  @InjectMocks
  private UpdateProfileUseCaseImpl useCase;

  private PersonalCustomer personalCustomer;
  private BusinessCustomer businessCustomer;

  @BeforeEach
  void setUp() {
    Instant birthDate = LocalDate.of(1990, 1, 1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();

    personalCustomer = PersonalCustomer.builder()
        .id("personal-id-123")
        .customerType(CustomerType.PERSONAL)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .email("personal@email.com")
        .phoneNumber("+51987654321")
        .address("Av. Test 123, Lima")
        .status(CustomerStatus.ACTIVE)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .firstName("John")
        .lastName("Doe")
        .dateOfBirth(birthDate)
        .gender(Gender.MALE)
        .personalProfile(PersonalProfile.STANDARD)
        .build();

    businessCustomer = BusinessCustomer.builder()
        .id("business-id-456")
        .customerType(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("12345678901")
        .email("business@email.com")
        .phoneNumber("+51987654322")
        .address("Av. Business 456, Lima")
        .status(CustomerStatus.ACTIVE)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .businessName("Test Business SAC")
        .businessType(BusinessType.SAC)
        .businessProfile(BusinessProfile.STANDARD)
        .build();
  }

  @Nested
  @DisplayName("VIP Profile Update Tests")
  class VipProfileTests {

    @Test
    @DisplayName("Should update personal customer to VIP with valid credit card")
    void shouldUpdateToVipWithValidCreditCard() {
      // Given
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(personalCustomer));
      when(creditCardValidationPort.validateHasActiveCreditCard(
          anyString(), anyString(), anyString())).thenReturn(Mono.just(true));
      when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(personalCustomer));
      when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.save(any(Customer.class), any(Duration.class)))
          .thenReturn(Mono.empty());
      when(eventPublisher.publishProfileUpdated(any(Customer.class), anyString(), anyString()))
          .thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.updateProfile("personal-id-123", "VIP"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertInstanceOf(PersonalCustomer.class, customer);
            PersonalCustomer updated = (PersonalCustomer) customer;
            assertEquals(PersonalProfile.VIP, updated.getPersonalProfile());
          })
          .verifyComplete();

      verify(creditCardValidationPort).validateHasActiveCreditCard(
          "personal-id-123", "PERSONAL", "VIP");
      verify(customerRepository).save(any(Customer.class));
      verify(eventPublisher).publishProfileUpdated(any(Customer.class), eq("STANDARD"), eq("VIP"));
    }

    @Test
    @DisplayName("Should fail VIP update when customer has no credit card")
    void shouldFailVipUpdateWithoutCreditCard() {
      // Given
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(personalCustomer));
      when(creditCardValidationPort.validateHasActiveCreditCard(
          anyString(), anyString(), anyString())).thenReturn(Mono.just(false));

      // When & Then
      StepVerifier.create(useCase.updateProfile("personal-id-123", "VIP"))
          .expectErrorMatches(throwable ->
              throwable instanceof ProfileUpdateNotAllowedException &&
              throwable.getMessage().contains("credit card")
          )
          .verify();

      verify(creditCardValidationPort).validateHasActiveCreditCard(
          "personal-id-123", "PERSONAL", "VIP");
      verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should fail VIP update for business customer")
    void shouldFailVipForBusinessCustomer() {
      // Given
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(businessCustomer));

      // When & Then
      StepVerifier.create(useCase.updateProfile("business-id-456", "VIP"))
          .expectErrorMatches(throwable ->
              throwable instanceof ProfileUpdateNotAllowedException &&
              throwable.getMessage().contains("VIP")
          )
          .verify();

      verify(creditCardValidationPort, never())
          .validateHasActiveCreditCard(anyString(), anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("PYME Profile Update Tests")
  class PymeProfileTests {

    @Test
    @DisplayName("Should update business customer to PYME with valid credit card")
    void shouldUpdateToPymeWithValidCreditCard() {
      // Given
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(businessCustomer));
      when(creditCardValidationPort.validateHasActiveCreditCard(
          anyString(), anyString(), anyString())).thenReturn(Mono.just(true));
      when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(businessCustomer));
      when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.save(any(Customer.class), any(Duration.class)))
          .thenReturn(Mono.empty());
      when(eventPublisher.publishProfileUpdated(any(Customer.class), anyString(), anyString()))
          .thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.updateProfile("business-id-456", "PYME"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertInstanceOf(BusinessCustomer.class, customer);
            BusinessCustomer updated = (BusinessCustomer) customer;
            assertEquals(BusinessProfile.PYME, updated.getBusinessProfile());
          })
          .verifyComplete();

      verify(creditCardValidationPort).validateHasActiveCreditCard(
          "business-id-456", "BUSINESS", "PYME");
      verify(customerRepository).save(any(Customer.class));
      verify(eventPublisher).publishProfileUpdated(
          any(Customer.class), eq("STANDARD"), eq("PYME"));
    }

    @Test
    @DisplayName("Should fail PYME update when customer has no credit card")
    void shouldFailPymeUpdateWithoutCreditCard() {
      // Given
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(businessCustomer));
      when(creditCardValidationPort.validateHasActiveCreditCard(
          anyString(), anyString(), anyString())).thenReturn(Mono.just(false));

      // When & Then
      StepVerifier.create(useCase.updateProfile("business-id-456", "PYME"))
          .expectErrorMatches(throwable ->
              throwable instanceof ProfileUpdateNotAllowedException &&
              throwable.getMessage().contains("credit card")
          )
          .verify();

      verify(creditCardValidationPort).validateHasActiveCreditCard(
          "business-id-456", "BUSINESS", "PYME");
      verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should fail PYME update for personal customer")
    void shouldFailPymeForPersonalCustomer() {
      // Given
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(personalCustomer));

      // When & Then
      StepVerifier.create(useCase.updateProfile("personal-id-123", "PYME"))
          .expectErrorMatches(throwable ->
              throwable instanceof ProfileUpdateNotAllowedException &&
              throwable.getMessage().contains("PYME")
          )
          .verify();

      verify(creditCardValidationPort, never())
          .validateHasActiveCreditCard(anyString(), anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("STANDARD Profile Update Tests")
  class StandardProfileTests {

    @Test
    @DisplayName("Should downgrade personal customer to STANDARD")
    void shouldDowngradePersonalToStandard() {
      // Given - Customer currently VIP
      personalCustomer.setPersonalProfile(PersonalProfile.VIP);
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(personalCustomer));
      when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(personalCustomer));
      when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.save(any(Customer.class), any(Duration.class)))
          .thenReturn(Mono.empty());
      when(eventPublisher.publishProfileUpdated(any(Customer.class), anyString(), anyString()))
          .thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.updateProfile("personal-id-123", "STANDARD"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertInstanceOf(PersonalCustomer.class, customer);
            PersonalCustomer updated = (PersonalCustomer) customer;
            assertEquals(PersonalProfile.STANDARD, updated.getPersonalProfile());
          })
          .verifyComplete();

      // No credit card validation needed for downgrade
      verify(creditCardValidationPort, never())
          .validateHasActiveCreditCard(anyString(), anyString(), anyString());
      verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should downgrade business customer to STANDARD")
    void shouldDowngradeBusinessToStandard() {
      // Given - Customer currently PYME
      businessCustomer.setBusinessProfile(BusinessProfile.PYME);
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(businessCustomer));
      when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(businessCustomer));
      when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
      when(cacheRepository.save(any(Customer.class), any(Duration.class)))
          .thenReturn(Mono.empty());
      when(eventPublisher.publishProfileUpdated(any(Customer.class), anyString(), anyString()))
          .thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.updateProfile("business-id-456", "STANDARD"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertInstanceOf(BusinessCustomer.class, customer);
            BusinessCustomer updated = (BusinessCustomer) customer;
            assertEquals(BusinessProfile.STANDARD, updated.getBusinessProfile());
          })
          .verifyComplete();

      verify(customerRepository).save(any(Customer.class));
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should throw exception when customer not found")
    void shouldThrowExceptionWhenCustomerNotFound() {
      // Given
      when(customerRepository.findById(anyString())).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.updateProfile("non-existent-id", "VIP"))
          .expectErrorMatches(throwable ->
              throwable instanceof CustomerNotFoundException &&
              throwable.getMessage().contains("non-existent-id")
          )
          .verify();

      verify(customerRepository).findById("non-existent-id");
      verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid profile type")
    void shouldThrowExceptionForInvalidProfileType() {
      // Given
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(personalCustomer));

      // When & Then
      StepVerifier.create(useCase.updateProfile("personal-id-123", "INVALID"))
          .expectErrorMatches(throwable ->
              throwable instanceof IllegalArgumentException &&
              throwable.getMessage().contains("Invalid profile type")
          )
          .verify();

      verify(customerRepository, never()).save(any(Customer.class));
    }
  }

  @Test
  @DisplayName("Should evict all cache entries when updating profile")
  void shouldEvictAllCacheEntries() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(personalCustomer));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(personalCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.save(any(Customer.class), any(Duration.class)))
        .thenReturn(Mono.empty());
    when(eventPublisher.publishProfileUpdated(any(Customer.class), anyString(), anyString()))
        .thenReturn(Mono.empty());

    // When
    StepVerifier.create(useCase.updateProfile("personal-id-123", "STANDARD"))
        .expectNextCount(1)
        .verifyComplete();

    // Then - Verify cache eviction happened
    verify(cacheRepository).evict("personal-id-123");
    verify(cacheRepository).evictByEmail("personal@email.com");
    verify(cacheRepository).evictByDocumentNumber("12345678");
  }
}
