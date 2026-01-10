package com.nttd.banking.customer.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.nttd.banking.customer.domain.exception.BusinessValidationException;
import com.nttd.banking.customer.domain.exception.DuplicateCustomerException;
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
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerEventPublisher;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for CreateCustomerUseCaseImpl using reactive testing with StepVerifier.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCustomerUseCase Tests")
class CreateCustomerUseCaseImplTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private CustomerCacheRepository cacheRepository;

  @Mock
  private CustomerEventPublisher eventPublisher;

  @InjectMocks
  private CreateCustomerUseCaseImpl useCase;

  private PersonalCustomer validPersonalCustomer;
  private BusinessCustomer validBusinessCustomer;

  @BeforeEach
  void setUp() {
    Instant birthDate = LocalDate.of(1990, 1, 1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();

    validPersonalCustomer = PersonalCustomer.builder()
        .customerType(CustomerType.PERSONAL)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .email("test@email.com")
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

    validBusinessCustomer = BusinessCustomer.builder()
        .customerType(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .email("empresa@company.com")
        .phoneNumber("+51987654321")
        .address("Av. Principal 456, Lima")
        .status(CustomerStatus.ACTIVE)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .businessName("Empresa SAC")
        .businessType(BusinessType.SAC)
        .taxId("20123456789")
        .businessProfile(BusinessProfile.STANDARD)
        .authorizedSigners(new ArrayList<>())
        .build();
  }

  @Test
  @DisplayName("Should create PersonalCustomer successfully")
  void shouldCreatePersonalCustomerSuccessfully() {
    // Given
    PersonalCustomer savedCustomer = PersonalCustomer.builder()
        .id("generated-id-123")
        .customerType(validPersonalCustomer.getCustomerType())
        .documentType(validPersonalCustomer.getDocumentType())
        .documentNumber(validPersonalCustomer.getDocumentNumber())
        .email(validPersonalCustomer.getEmail())
        .phoneNumber(validPersonalCustomer.getPhoneNumber())
        .address(validPersonalCustomer.getAddress())
        .status(validPersonalCustomer.getStatus())
        .createdAt(validPersonalCustomer.getCreatedAt())
        .updatedAt(validPersonalCustomer.getUpdatedAt())
        .firstName(validPersonalCustomer.getFirstName())
        .lastName(validPersonalCustomer.getLastName())
        .dateOfBirth(validPersonalCustomer.getDateOfBirth())
        .gender(validPersonalCustomer.getGender())
        .personalProfile(validPersonalCustomer.getPersonalProfile())
        .build();

    when(customerRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(savedCustomer));
    when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerCreated(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.execute(validPersonalCustomer))
        .assertNext(customer -> {
          assertNotNull(customer);
          assertEquals("generated-id-123", customer.getId());
          assertInstanceOf(PersonalCustomer.class, customer);
          PersonalCustomer personalCustomer = (PersonalCustomer) customer;
          assertEquals("John", personalCustomer.getFirstName());
          assertEquals("test@email.com", personalCustomer.getEmail());
        })
        .verifyComplete();

    verify(customerRepository).existsByEmail("test@email.com");
    verify(customerRepository).existsByDocumentNumber("12345678");
    verify(customerRepository).save(any(Customer.class));
    verify(cacheRepository).save(eq(savedCustomer), any(Duration.class));
    verify(eventPublisher).publishCustomerCreated(savedCustomer);
  }

  @Test
  @DisplayName("Should create BusinessCustomer successfully")
  void shouldCreateBusinessCustomerSuccessfully() {
    // Given
    BusinessCustomer savedCustomer = BusinessCustomer.builder()
        .id("generated-id-456")
        .customerType(validBusinessCustomer.getCustomerType())
        .documentType(validBusinessCustomer.getDocumentType())
        .documentNumber(validBusinessCustomer.getDocumentNumber())
        .email(validBusinessCustomer.getEmail())
        .phoneNumber(validBusinessCustomer.getPhoneNumber())
        .address(validBusinessCustomer.getAddress())
        .status(validBusinessCustomer.getStatus())
        .createdAt(validBusinessCustomer.getCreatedAt())
        .updatedAt(validBusinessCustomer.getUpdatedAt())
        .businessName(validBusinessCustomer.getBusinessName())
        .businessType(validBusinessCustomer.getBusinessType())
        .taxId(validBusinessCustomer.getTaxId())
        .businessProfile(validBusinessCustomer.getBusinessProfile())
        .authorizedSigners(validBusinessCustomer.getAuthorizedSigners())
        .build();

    when(customerRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(savedCustomer));
    when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerCreated(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.execute(validBusinessCustomer))
        .assertNext(customer -> {
          assertNotNull(customer);
          assertEquals("generated-id-456", customer.getId());
          assertInstanceOf(BusinessCustomer.class, customer);
          BusinessCustomer businessCustomer = (BusinessCustomer) customer;
          assertEquals("Empresa SAC", businessCustomer.getBusinessName());
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should fail when email already exists")
  void shouldFailWhenEmailAlreadyExists() {
    // Given
    when(customerRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));
    when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.execute(validPersonalCustomer))
        .expectErrorMatches(throwable ->
            throwable instanceof DuplicateCustomerException &&
            throwable.getMessage().contains("email")
        )
        .verify();

    verify(customerRepository).existsByEmail("test@email.com");
    // Note: save() may be called during reactive chain construction but won't execute
  }

  @Test
  @DisplayName("Should fail when document number already exists")
  void shouldFailWhenDocumentNumberAlreadyExists() {
    // Given
    when(customerRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(true));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.execute(validPersonalCustomer))
        .expectErrorMatches(throwable ->
            throwable instanceof DuplicateCustomerException &&
            throwable.getMessage().contains("document")
        )
        .verify();

    verify(customerRepository).existsByDocumentNumber("12345678");
    // Note: save() may be called during reactive chain construction but won't execute
  }

  @Test
  @DisplayName("Should fail validation for underage personal customer")
  void shouldFailValidationForUnderageCustomer() {
    // Given - Create underage customer (17 years old)
    Instant underageBirthDate = LocalDate.now().minusYears(17)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();

    PersonalCustomer underageCustomer = PersonalCustomer.builder()
        .customerType(CustomerType.PERSONAL)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .email("underage@email.com")
        .phoneNumber("+51987654321")
        .address("Av. Test 123")
        .status(CustomerStatus.ACTIVE)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .firstName("Young")
        .lastName("Person")
        .dateOfBirth(underageBirthDate)
        .gender(Gender.MALE)
        .personalProfile(PersonalProfile.STANDARD)
        .build();

    when(customerRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.execute(underageCustomer))
        .expectErrorMatches(throwable ->
            throwable instanceof BusinessValidationException &&
            (throwable.getMessage().contains("18 years old") ||
             throwable.getMessage().contains("mayor de 18"))
        )
        .verify();

    // Note: save() may be called during reactive chain construction but won't execute
  }

  @Test
  @DisplayName("Should fail validation for invalid customer data")
  void shouldFailValidationForInvalidData() {
    // Given - Customer with null email (create new instance to avoid affecting other tests)
    Instant birthDate = LocalDate.of(1990, 1, 1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();

    PersonalCustomer invalidCustomer = PersonalCustomer.builder()
        .customerType(CustomerType.PERSONAL)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .email(null)  // Invalid: null email
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

    when(customerRepository.existsByEmail(isNull())).thenReturn(Mono.just(false));
    when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.execute(invalidCustomer))
        .expectError(BusinessValidationException.class)
        .verify();

    // Note: save() may be called during reactive chain construction but won't execute
  }
}
