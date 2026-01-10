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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for FindCustomerUseCaseImpl using reactive testing with StepVerifier.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FindCustomerUseCase Tests")
class FindCustomerUseCaseImplTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private CustomerCacheRepository cacheRepository;

  @InjectMocks
  private FindCustomerUseCaseImpl useCase;

  private PersonalCustomer testCustomer;

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
  }

  @Nested
  @DisplayName("findById Tests")
  class FindByIdTests {

    @Test
    @DisplayName("Should return customer from cache when found")
    void shouldReturnCustomerFromCache() {
      // Given
      when(cacheRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
      when(customerRepository.findById(anyString())).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findById("test-id-123"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertEquals("test-id-123", customer.getId());
            assertEquals("test@email.com", customer.getEmail());
          })
          .verifyComplete();

      verify(cacheRepository).findById("test-id-123");
      // Repository method is called during chain construction but not executed
    }

    @Test
    @DisplayName("Should return customer from repository and cache it when cache miss")
    void shouldReturnFromRepositoryAndCacheWhenCacheMiss() {
      // Given
      when(cacheRepository.findById(anyString())).thenReturn(Mono.empty());
      when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
      when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findById("test-id-123"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertEquals("test-id-123", customer.getId());
            assertEquals("test@email.com", customer.getEmail());
          })
          .verifyComplete();

      verify(cacheRepository).findById("test-id-123");
      verify(customerRepository).findById("test-id-123");
      verify(cacheRepository).save(eq(testCustomer), any(Duration.class));
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when not found")
    void shouldThrowExceptionWhenNotFound() {
      // Given
      when(cacheRepository.findById(anyString())).thenReturn(Mono.empty());
      when(customerRepository.findById(anyString())).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findById("non-existent-id"))
          .expectErrorMatches(throwable ->
              throwable instanceof CustomerNotFoundException &&
              throwable.getMessage().contains("non-existent-id")
          )
          .verify();

      verify(cacheRepository).findById("non-existent-id");
      verify(customerRepository).findById("non-existent-id");
    }
  }

  @Nested
  @DisplayName("findByEmail Tests")
  class FindByEmailTests {

    @Test
    @DisplayName("Should return customer from cache when found")
    void shouldReturnCustomerFromCache() {
      // Given
      when(cacheRepository.findByEmail(anyString())).thenReturn(Mono.just(testCustomer));
      when(customerRepository.findByEmail(anyString())).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findByEmail("test@email.com"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertEquals("test@email.com", customer.getEmail());
          })
          .verifyComplete();

      verify(cacheRepository).findByEmail("test@email.com");
      // Repository method is called during chain construction but not executed
    }

    @Test
    @DisplayName("Should return customer from repository and cache it when cache miss")
    void shouldReturnFromRepositoryAndCacheWhenCacheMiss() {
      // Given
      when(cacheRepository.findByEmail(anyString())).thenReturn(Mono.empty());
      when(customerRepository.findByEmail(anyString())).thenReturn(Mono.just(testCustomer));
      when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findByEmail("test@email.com"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertEquals("test@email.com", customer.getEmail());
          })
          .verifyComplete();

      verify(cacheRepository).findByEmail("test@email.com");
      verify(customerRepository).findByEmail("test@email.com");
      verify(cacheRepository).save(eq(testCustomer), any(Duration.class));
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when not found")
    void shouldThrowExceptionWhenNotFound() {
      // Given
      when(cacheRepository.findByEmail(anyString())).thenReturn(Mono.empty());
      when(customerRepository.findByEmail(anyString())).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findByEmail("notfound@email.com"))
          .expectErrorMatches(throwable ->
              throwable instanceof CustomerNotFoundException &&
              throwable.getMessage().contains("notfound@email.com")
          )
          .verify();

      verify(cacheRepository).findByEmail("notfound@email.com");
      verify(customerRepository).findByEmail("notfound@email.com");
    }
  }

  @Nested
  @DisplayName("findByDocumentNumber Tests")
  class FindByDocumentNumberTests {

    @Test
    @DisplayName("Should return customer from cache when found")
    void shouldReturnCustomerFromCache() {
      // Given
      when(cacheRepository.findByDocumentNumber(anyString())).thenReturn(Mono.just(testCustomer));
      when(customerRepository.findByDocumentNumber(anyString())).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findByDocumentNumber("12345678"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertEquals("12345678", customer.getDocumentNumber());
          })
          .verifyComplete();

      verify(cacheRepository).findByDocumentNumber("12345678");
      // Repository method is called during chain construction but not executed
    }

    @Test
    @DisplayName("Should return customer from repository and cache it when cache miss")
    void shouldReturnFromRepositoryAndCacheWhenCacheMiss() {
      // Given
      when(cacheRepository.findByDocumentNumber(anyString())).thenReturn(Mono.empty());
      when(customerRepository.findByDocumentNumber(anyString())).thenReturn(Mono.just(testCustomer));
      when(cacheRepository.save(any(Customer.class), any(Duration.class))).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findByDocumentNumber("12345678"))
          .assertNext(customer -> {
            assertNotNull(customer);
            assertEquals("12345678", customer.getDocumentNumber());
          })
          .verifyComplete();

      verify(cacheRepository).findByDocumentNumber("12345678");
      verify(customerRepository).findByDocumentNumber("12345678");
      verify(cacheRepository).save(eq(testCustomer), any(Duration.class));
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when not found")
    void shouldThrowExceptionWhenNotFound() {
      // Given
      when(cacheRepository.findByDocumentNumber(anyString())).thenReturn(Mono.empty());
      when(customerRepository.findByDocumentNumber(anyString())).thenReturn(Mono.empty());

      // When & Then
      StepVerifier.create(useCase.findByDocumentNumber("99999999"))
          .expectErrorMatches(throwable ->
              throwable instanceof CustomerNotFoundException &&
              throwable.getMessage().contains("99999999")
          )
          .verify();

      verify(cacheRepository).findByDocumentNumber("99999999");
      verify(customerRepository).findByDocumentNumber("99999999");
    }
  }

  @Nested
  @DisplayName("findAll Tests")
  class FindAllTests {

    @Test
    @DisplayName("Should return all customers")
    void shouldReturnAllCustomers() {
      // Given
      PersonalCustomer customer2 = PersonalCustomer.builder()
          .id("test-id-456")
          .customerType(CustomerType.PERSONAL)
          .documentType(DocumentType.DNI)
          .documentNumber("87654321")
          .email("customer2@email.com")
          .phoneNumber("+51987654322")
          .address("Av. Test 456, Lima")
          .status(CustomerStatus.ACTIVE)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .firstName("Jane")
          .lastName("Smith")
          .dateOfBirth(Instant.now().minusSeconds(31536000 * 30))
          .gender(Gender.FEMALE)
          .personalProfile(PersonalProfile.VIP)
          .build();

      when(customerRepository.findAll()).thenReturn(Flux.just(testCustomer, customer2));

      // When & Then
      StepVerifier.create(useCase.findAll())
          .assertNext(customer -> assertEquals("test-id-123", customer.getId()))
          .assertNext(customer -> assertEquals("test-id-456", customer.getId()))
          .verifyComplete();

      verify(customerRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty flux when no customers")
    void shouldReturnEmptyFluxWhenNoCustomers() {
      // Given
      when(customerRepository.findAll()).thenReturn(Flux.empty());

      // When & Then
      StepVerifier.create(useCase.findAll())
          .verifyComplete();

      verify(customerRepository).findAll();
    }
  }

  @Nested
  @DisplayName("findAll with Pagination Tests")
  class FindAllPaginatedTests {

    @Test
    @DisplayName("Should return paginated customers")
    void shouldReturnPaginatedCustomers() {
      // Given
      when(customerRepository.findAll(0, 10)).thenReturn(Flux.just(testCustomer));

      // When & Then
      StepVerifier.create(useCase.findAll(0, 10))
          .assertNext(customer -> assertEquals("test-id-123", customer.getId()))
          .verifyComplete();

      verify(customerRepository).findAll(0, 10);
    }

    @Test
    @DisplayName("Should return empty flux for empty page")
    void shouldReturnEmptyFluxForEmptyPage() {
      // Given
      when(customerRepository.findAll(5, 10)).thenReturn(Flux.empty());

      // When & Then
      StepVerifier.create(useCase.findAll(5, 10))
          .verifyComplete();

      verify(customerRepository).findAll(5, 10);
    }
  }
}
