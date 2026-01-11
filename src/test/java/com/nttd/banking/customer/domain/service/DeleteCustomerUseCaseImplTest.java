package com.nttd.banking.customer.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.nttd.banking.customer.application.exception.CustomerHasActiveProductsException;
import com.nttd.banking.customer.application.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.Gender;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerEventPublisher;
import com.nttd.banking.customer.domain.port.out.CustomerProductsPort;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
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
 * Unit tests for DeleteCustomerUseCaseImpl using reactive testing with StepVerifier.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCustomerUseCase Tests")
class DeleteCustomerUseCaseImplTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private CustomerCacheRepository cacheRepository;

  @Mock
  private CustomerEventPublisher eventPublisher;

  @Mock
  private CustomerProductsPort customerProductsPort;

  @InjectMocks
  private DeleteCustomerUseCaseImpl useCase;

  private PersonalCustomer testCustomer;
  private ProductsQueryResponse noProductsResponse;
  private ProductsQueryResponse hasProductsResponse;

  @BeforeEach
  void setUp() {
    Instant birthDate = LocalDate.of(1990, 1, 1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();

    testCustomer = PersonalCustomer.builder()
        .id("test-id-123")
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

    // Response with no products
    noProductsResponse = ProductsQueryResponse.builder()
        .customerId("test-id-123")
        .build();

    // Response with active products
    hasProductsResponse = ProductsQueryResponse.builder()
        .customerId("test-id-123")
        .accounts(java.util.List.of(
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC001")
                .accountType("SAVINGS")
                .status("ACTIVE")
                .balance(1000.0)
                .build()))
        .build();
  }

  @Test
  @DisplayName("Should delete customer successfully when no active products")
  void shouldDeleteCustomerSuccessfully() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(noProductsResponse));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(testCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerDeleted(anyString())).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.delete("test-id-123"))
        .verifyComplete();

    verify(customerRepository).findById("test-id-123");
    verify(customerProductsPort).queryCustomerProducts("test-id-123", "PERSONAL");
    verify(customerRepository).save(any(Customer.class));
    verify(cacheRepository).evict("test-id-123");
    verify(cacheRepository).evictByEmail("test@email.com");
    verify(cacheRepository).evictByDocumentNumber("12345678");
    verify(eventPublisher).publishCustomerDeleted("test-id-123");
  }

  @Test
  @DisplayName("Should throw CustomerNotFoundException when customer not found")
  void shouldThrowExceptionWhenCustomerNotFound() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.delete("non-existent-id"))
        .expectErrorMatches(throwable ->
            throwable instanceof CustomerNotFoundException &&
            throwable.getMessage().contains("non-existent-id")
        )
        .verify();

    verify(customerRepository).findById("non-existent-id");
    verify(customerProductsPort, never()).queryCustomerProducts(anyString(), anyString());
    verify(customerRepository, never()).save(any(Customer.class));
  }

  @Test
  @DisplayName("Should throw exception when customer has active products")
  void shouldThrowExceptionWhenHasActiveProducts() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(hasProductsResponse));

    // When & Then
    StepVerifier.create(useCase.delete("test-id-123"))
        .expectErrorMatches(throwable ->
            throwable instanceof CustomerHasActiveProductsException &&
            throwable.getMessage().contains("test-id-123")
        )
        .verify();

    verify(customerRepository).findById("test-id-123");
    verify(customerProductsPort).queryCustomerProducts("test-id-123", "PERSONAL");
    verify(customerRepository, never()).save(any(Customer.class));
    verify(eventPublisher, never()).publishCustomerDeleted(anyString());
  }

  @Test
  @DisplayName("Should perform soft delete by setting status to INACTIVE")
  void shouldPerformSoftDelete() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(noProductsResponse));
    when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
      Customer saved = invocation.getArgument(0);
      assertEquals(CustomerStatus.INACTIVE, saved.getStatus());
      return Mono.just(saved);
    });
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerDeleted(anyString())).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.delete("test-id-123"))
        .verifyComplete();

    verify(customerRepository).save(any(Customer.class));
  }

  @Test
  @DisplayName("Should evict all cache entries when deleting")
  void shouldEvictAllCacheEntries() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(noProductsResponse));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(testCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerDeleted(anyString())).thenReturn(Mono.empty());

    // When
    StepVerifier.create(useCase.delete("test-id-123"))
        .verifyComplete();

    // Then - Verify all cache evictions happened
    verify(cacheRepository).evict("test-id-123");
    verify(cacheRepository).evictByEmail("test@email.com");
    verify(cacheRepository).evictByDocumentNumber("12345678");
  }

  @Test
  @DisplayName("Should publish customer deleted event")
  void shouldPublishCustomerDeletedEvent() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(noProductsResponse));
    when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(testCustomer));
    when(cacheRepository.evict(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByEmail(anyString())).thenReturn(Mono.empty());
    when(cacheRepository.evictByDocumentNumber(anyString())).thenReturn(Mono.empty());
    when(eventPublisher.publishCustomerDeleted(anyString())).thenReturn(Mono.empty());

    // When
    StepVerifier.create(useCase.delete("test-id-123"))
        .verifyComplete();

    // Then
    verify(eventPublisher).publishCustomerDeleted("test-id-123");
  }
}
