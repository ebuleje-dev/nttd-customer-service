package com.nttd.banking.customer.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.nttd.banking.customer.domain.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
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
 * Unit tests for UpdateCustomerUseCaseImpl using reactive testing with StepVerifier.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCustomerUseCase Tests")
class UpdateCustomerUseCaseImplTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private CustomerCacheRepository cacheRepository;

  @Mock
  private CustomerEventPublisher eventPublisher;

  @InjectMocks
  private UpdateCustomerUseCaseImpl useCase;

  private PersonalCustomer existingCustomer;
  private PersonalCustomer updateData;

  @BeforeEach
  void setUp() {
    Instant birthDate = LocalDate.of(1990, 1, 1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();

    existingCustomer = PersonalCustomer.builder()
        .id("test-id-123")
        .customerType(CustomerType.PERSONAL)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .email("old@email.com")
        .phoneNumber("+51987654321")
        .address("Old Address 123")
        .status(CustomerStatus.ACTIVE)
        .createdAt(Instant.now().minusSeconds(86400))
        .updatedAt(Instant.now().minusSeconds(86400))
        .firstName("John")
        .lastName("Doe")
        .dateOfBirth(birthDate)
        .gender(Gender.MALE)
        .personalProfile(PersonalProfile.STANDARD)
        .build();

    updateData = PersonalCustomer.builder()
        .email("new@email.com")
        .phoneNumber("+51999999999")
        .address("New Address 456")
        .build();
  }

  @Test
  @DisplayName("Should update customer successfully with all fields")
  void shouldUpdateCustomerSuccessfully() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(existingCustomer));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerUpdated(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.update("test-id-123", updateData))
        .assertNext(customer -> {
          assertNotNull(customer);
          assertEquals("test-id-123", customer.getId());
          assertEquals("new@email.com", customer.getEmail());
          assertEquals("+51999999999", customer.getPhoneNumber());
          assertEquals("New Address 456", customer.getAddress());
        })
        .verifyComplete();

    verify(customerRepository).findById("test-id-123");
    verify(customerRepository).save(any(Customer.class));
    verify(cacheRepository).evict("test-id-123");
    verify(cacheRepository).evictByEmail("new@email.com");
    verify(cacheRepository).evictByDocumentNumber("12345678");
    verify(cacheRepository).save(any(Customer.class), any(Duration.class));
    verify(eventPublisher).publishCustomerUpdated(any(Customer.class));
  }

  @Test
  @DisplayName("Should update customer with partial data")
  void shouldUpdateCustomerWithPartialData() {
    // Given - Only update email
    PersonalCustomer partialUpdate = PersonalCustomer.builder()
        .email("partial@email.com")
        .build();

    when(customerRepository.findById(anyString())).thenReturn(Mono.just(existingCustomer));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerUpdated(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.update("test-id-123", partialUpdate))
        .assertNext(customer -> {
          assertNotNull(customer);
          assertEquals("partial@email.com", customer.getEmail());
          // Other fields should remain unchanged
          assertEquals("+51987654321", customer.getPhoneNumber());
          assertEquals("Old Address 123", customer.getAddress());
        })
        .verifyComplete();

    verify(customerRepository).findById("test-id-123");
    verify(customerRepository).save(any(Customer.class));
  }

  @Test
  @DisplayName("Should update only phone number")
  void shouldUpdateOnlyPhoneNumber() {
    // Given
    PersonalCustomer phoneUpdate = PersonalCustomer.builder()
        .phoneNumber("+51888888888")
        .build();

    when(customerRepository.findById(anyString())).thenReturn(Mono.just(existingCustomer));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerUpdated(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.update("test-id-123", phoneUpdate))
        .assertNext(customer -> {
          assertNotNull(customer);
          assertEquals("+51888888888", customer.getPhoneNumber());
          // Email and address should remain unchanged
          assertEquals("old@email.com", customer.getEmail());
          assertEquals("Old Address 123", customer.getAddress());
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should update only address")
  void shouldUpdateOnlyAddress() {
    // Given
    PersonalCustomer addressUpdate = PersonalCustomer.builder()
        .address("Updated Address Only 789")
        .build();

    when(customerRepository.findById(anyString())).thenReturn(Mono.just(existingCustomer));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerUpdated(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.update("test-id-123", addressUpdate))
        .assertNext(customer -> {
          assertNotNull(customer);
          assertEquals("Updated Address Only 789", customer.getAddress());
          // Email and phone should remain unchanged
          assertEquals("old@email.com", customer.getEmail());
          assertEquals("+51987654321", customer.getPhoneNumber());
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should throw CustomerNotFoundException when customer not found")
  void shouldThrowExceptionWhenCustomerNotFound() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.update("non-existent-id", updateData))
        .expectErrorMatches(throwable ->
            throwable instanceof CustomerNotFoundException &&
            throwable.getMessage().contains("non-existent-id")
        )
        .verify();

    verify(customerRepository).findById("non-existent-id");
    verify(customerRepository, never()).save(any(Customer.class));
    verify(eventPublisher, never()).publishCustomerUpdated(any(Customer.class));
  }

  @Test
  @DisplayName("Should update updatedAt timestamp")
  void shouldUpdateTimestamp() {
    // Given
    Instant beforeUpdate = Instant.now();

    when(customerRepository.findById(anyString())).thenReturn(Mono.just(existingCustomer));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerUpdated(any(Customer.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.update("test-id-123", updateData))
        .assertNext(customer -> {
          assertNotNull(customer.getUpdatedAt());
          assertTrue(customer.getUpdatedAt().isAfter(beforeUpdate) ||
                     customer.getUpdatedAt().equals(beforeUpdate));
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should evict all cache entries before saving")
  void shouldEvictCacheBeforeSaving() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(existingCustomer));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerUpdated(any(Customer.class))).thenReturn(Mono.empty());

    // When
    StepVerifier.create(useCase.update("test-id-123", updateData))
        .expectNextCount(1)
        .verifyComplete();

    // Then - Verify cache eviction happened
    verify(cacheRepository).evict("test-id-123");
    verify(cacheRepository).evictByEmail("new@email.com");
    verify(cacheRepository).evictByDocumentNumber("12345678");
  }
}
