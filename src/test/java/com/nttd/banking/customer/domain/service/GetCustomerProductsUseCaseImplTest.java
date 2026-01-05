package com.nttd.banking.customer.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.nttd.banking.customer.application.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.Gender;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import com.nttd.banking.customer.domain.port.out.CustomerProductsPort;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
 * Unit tests for GetCustomerProductsUseCaseImpl using reactive testing with StepVerifier.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerProductsUseCase Tests")
class GetCustomerProductsUseCaseImplTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private CustomerProductsPort customerProductsPort;

  @InjectMocks
  private GetCustomerProductsUseCaseImpl useCase;

  private PersonalCustomer testCustomer;
  private ProductsQueryResponse productsResponse;

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

    // Sample products response
    ProductsQueryResponse.AccountInfo account = ProductsQueryResponse.AccountInfo.builder()
        .accountId("ACC001")
        .accountType("SAVINGS")
        .status("ACTIVE")
        .balance(5000.0)
        .build();

    ProductsQueryResponse.CreditInfo credit = ProductsQueryResponse.CreditInfo.builder()
        .creditId("CRED001")
        .creditType("PERSONAL")
        .status("ACTIVE")
        .creditLimit(10000.0)
        .usedAmount(2000.0)
        .availableAmount(8000.0)
        .build();

    productsResponse = ProductsQueryResponse.builder()
        .customerId("test-id-123")
        .accounts(List.of(account))
        .credits(List.of(credit))
        .build();
  }

  @Test
  @DisplayName("Should get customer products successfully")
  void shouldGetCustomerProductsSuccessfully() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(productsResponse));

    // When & Then
    StepVerifier.create(useCase.getProducts("test-id-123"))
        .assertNext(response -> {
          assertNotNull(response);
          assertEquals("test-id-123", response.getCustomerId());
          assertEquals(2, response.getTotalProducts());
          assertEquals(1, response.getAccounts().size());
          assertEquals(1, response.getCredits().size());
        })
        .verifyComplete();

    verify(customerRepository).findById("test-id-123");
    verify(customerProductsPort).queryCustomerProducts("test-id-123", "PERSONAL");
  }

  @Test
  @DisplayName("Should throw CustomerNotFoundException when customer not found")
  void shouldThrowExceptionWhenCustomerNotFound() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(useCase.getProducts("non-existent-id"))
        .expectErrorMatches(throwable ->
            throwable instanceof CustomerNotFoundException &&
            throwable.getMessage().contains("non-existent-id")
        )
        .verify();

    verify(customerRepository).findById("non-existent-id");
    verify(customerProductsPort, never()).queryCustomerProducts(anyString(), anyString());
  }

  @Test
  @DisplayName("Should return empty products when customer has no products")
  void shouldReturnEmptyProductsWhenNone() {
    // Given
    ProductsQueryResponse emptyResponse = ProductsQueryResponse.builder()
        .customerId("test-id-123")
        .build();

    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(emptyResponse));

    // When & Then
    StepVerifier.create(useCase.getProducts("test-id-123"))
        .assertNext(response -> {
          assertNotNull(response);
          assertEquals("test-id-123", response.getCustomerId());
          assertEquals(0, response.getTotalProducts());
        })
        .verifyComplete();

    verify(customerProductsPort).queryCustomerProducts("test-id-123", "PERSONAL");
  }

  @Test
  @DisplayName("Should pass correct customer type to products port")
  void shouldPassCorrectCustomerType() {
    // Given
    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(productsResponse));

    // When
    StepVerifier.create(useCase.getProducts("test-id-123"))
        .expectNextCount(1)
        .verifyComplete();

    // Then - Verify PERSONAL customer type was passed
    verify(customerProductsPort).queryCustomerProducts("test-id-123", "PERSONAL");
  }

  @Test
  @DisplayName("Should handle products response with only accounts")
  void shouldHandleProductsWithOnlyAccounts() {
    // Given
    ProductsQueryResponse accountsOnlyResponse = ProductsQueryResponse.builder()
        .customerId("test-id-123")
        .accounts(List.of(
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC001")
                .accountType("SAVINGS")
                .status("ACTIVE")
                .balance(5000.0)
                .build()
        ))
        .build();

    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(accountsOnlyResponse));

    // When & Then
    StepVerifier.create(useCase.getProducts("test-id-123"))
        .assertNext(response -> {
          assertNotNull(response);
          assertEquals(1, response.getTotalProducts());
          assertEquals(1, response.getAccounts().size());
          assertTrue(response.getCredits() == null || response.getCredits().isEmpty());
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should handle products response with multiple product types")
  void shouldHandleMultipleProductTypes() {
    // Given
    ProductsQueryResponse fullResponse = ProductsQueryResponse.builder()
        .customerId("test-id-123")
        .accounts(List.of(
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC001")
                .accountType("SAVINGS")
                .status("ACTIVE")
                .balance(5000.0)
                .build(),
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC002")
                .accountType("CHECKING")
                .status("ACTIVE")
                .balance(3000.0)
                .build()
        ))
        .credits(List.of(
            ProductsQueryResponse.CreditInfo.builder()
                .creditId("CRED001")
                .creditType("PERSONAL")
                .status("ACTIVE")
                .creditLimit(10000.0)
                .usedAmount(2000.0)
                .availableAmount(8000.0)
                .build()
        ))
        .build();

    when(customerRepository.findById(anyString())).thenReturn(Mono.just(testCustomer));
    when(customerProductsPort.queryCustomerProducts(anyString(), anyString()))
        .thenReturn(Mono.just(fullResponse));

    // When & Then
    StepVerifier.create(useCase.getProducts("test-id-123"))
        .assertNext(response -> {
          assertNotNull(response);
          assertEquals(3, response.getTotalProducts());
          assertEquals(2, response.getAccounts().size());
          assertEquals(1, response.getCredits().size());
        })
        .verifyComplete();
  }
}
