package com.nttd.banking.customer.application.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import com.nttd.banking.customer.model.dto.ProductSummaryDTO;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ProductSummaryMapper.
 */
@DisplayName("ProductSummaryMapper Tests")
class ProductSummaryMapperTest {

  private ProductSummaryMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ProductSummaryMapper();
  }

  @Test
  @DisplayName("Should return null when response is null")
  void shouldReturnNullWhenResponseIsNull() {
    ProductSummaryDTO dto = mapper.toDto(null);
    assertNull(dto);
  }

  @Test
  @DisplayName("Should map empty response")
  void shouldMapEmptyResponse() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-123")
        .timestamp(Instant.now())
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertEquals("customer-123", dto.getCustomerId());
    assertEquals(0, dto.getTotalProducts());
    assertFalse(dto.getHasActiveProducts());
    assertNotNull(dto.getAccounts());
    assertTrue(dto.getAccounts().isEmpty());
    assertNotNull(dto.getCredits());
    assertTrue(dto.getCredits().isEmpty());
    assertNotNull(dto.getCards());
  }

  @Test
  @DisplayName("Should map response with accounts only")
  void shouldMapResponseWithAccountsOnly() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-123")
        .timestamp(Instant.now())
        .accounts(List.of(
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC001")
                .accountNumber("1234567890")
                .accountType("SAVINGS")
                .currency("PEN")
                .balance(5000.0)
                .status("ACTIVE")
                .build(),
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC002")
                .accountNumber("0987654321")
                .accountType("CHECKING")
                .currency("USD")
                .balance(3000.0)
                .status("ACTIVE")
                .build()
        ))
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertEquals("customer-123", dto.getCustomerId());
    assertEquals(2, dto.getTotalProducts());
    assertTrue(dto.getHasActiveProducts());
    assertEquals(2, dto.getAccounts().size());
    assertTrue(dto.getCredits().isEmpty());

    // Verify first account
    assertEquals("ACC001", dto.getAccounts().get(0).getAccountId());
    assertEquals("1234567890", dto.getAccounts().get(0).getAccountNumber());
    assertEquals(5000.0, dto.getAccounts().get(0).getBalance());
  }

  @Test
  @DisplayName("Should map response with credits only")
  void shouldMapResponseWithCreditsOnly() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-456")
        .timestamp(Instant.now())
        .credits(List.of(
            ProductsQueryResponse.CreditInfo.builder()
                .creditId("CRED001")
                .creditType("PERSONAL")
                .creditLimit(10000.0)
                .usedAmount(2000.0)
                .availableAmount(8000.0)
                .currency("PEN")
                .status("ACTIVE")
                .build()
        ))
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertEquals(1, dto.getTotalProducts());
    assertEquals(1, dto.getCredits().size());
    assertTrue(dto.getAccounts().isEmpty());

    // Verify credit
    assertEquals("CRED001", dto.getCredits().get(0).getCreditId());
    assertEquals(10000.0, dto.getCredits().get(0).getCreditLimit());
    assertEquals(8000.0, dto.getCredits().get(0).getAvailableAmount());
  }

  @Test
  @DisplayName("Should map response with credit cards")
  void shouldMapResponseWithCreditCards() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-789")
        .timestamp(Instant.now())
        .creditCards(List.of(
            ProductsQueryResponse.CreditCardInfo.builder()
                .cardId("CC001")
                .cardNumber("****1234")
                .cardType("VISA")
                .creditLimit(5000.0)
                .availableCredit(4000.0)
                .status("ACTIVE")
                .build()
        ))
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertEquals(1, dto.getTotalProducts());
    assertNotNull(dto.getCards());
    assertEquals(1, dto.getCards().getCreditCards().size());
    assertTrue(dto.getCards().getDebitCards().isEmpty());

    // Verify credit card
    assertEquals("CC001", dto.getCards().getCreditCards().get(0).getCardId());
    assertEquals("****1234", dto.getCards().getCreditCards().get(0).getCardNumber());
    assertEquals(5000.0, dto.getCards().getCreditCards().get(0).getCreditLimit());
  }

  @Test
  @DisplayName("Should map response with debit cards")
  void shouldMapResponseWithDebitCards() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-101")
        .timestamp(Instant.now())
        .debitCards(List.of(
            ProductsQueryResponse.DebitCardInfo.builder()
                .cardId("DC001")
                .cardNumber("****5678")
                .cardType("MASTERCARD")
                .linkedAccountId("ACC001")
                .status("ACTIVE")
                .build()
        ))
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertEquals(1, dto.getTotalProducts());
    assertNotNull(dto.getCards());
    assertEquals(1, dto.getCards().getDebitCards().size());
    assertTrue(dto.getCards().getCreditCards().isEmpty());

    // Verify debit card
    assertEquals("DC001", dto.getCards().getDebitCards().get(0).getCardId());
    assertEquals("ACC001", dto.getCards().getDebitCards().get(0).getLinkedAccountId());
  }

  @Test
  @DisplayName("Should map complete response with all product types")
  void shouldMapCompleteResponseWithAllProductTypes() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-full")
        .timestamp(Instant.now())
        .accounts(List.of(
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC001")
                .accountNumber("1234567890")
                .accountType("SAVINGS")
                .currency("PEN")
                .balance(5000.0)
                .status("ACTIVE")
                .build()
        ))
        .credits(List.of(
            ProductsQueryResponse.CreditInfo.builder()
                .creditId("CRED001")
                .creditType("PERSONAL")
                .creditLimit(10000.0)
                .usedAmount(2000.0)
                .availableAmount(8000.0)
                .currency("PEN")
                .status("ACTIVE")
                .build()
        ))
        .creditCards(List.of(
            ProductsQueryResponse.CreditCardInfo.builder()
                .cardId("CC001")
                .cardNumber("****1234")
                .cardType("VISA")
                .creditLimit(5000.0)
                .availableCredit(4000.0)
                .status("ACTIVE")
                .build()
        ))
        .debitCards(List.of(
            ProductsQueryResponse.DebitCardInfo.builder()
                .cardId("DC001")
                .cardNumber("****5678")
                .cardType("MASTERCARD")
                .linkedAccountId("ACC001")
                .status("ACTIVE")
                .build()
        ))
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertEquals("customer-full", dto.getCustomerId());
    assertEquals(4, dto.getTotalProducts());
    assertTrue(dto.getHasActiveProducts());
    assertEquals(1, dto.getAccounts().size());
    assertEquals(1, dto.getCredits().size());
    assertEquals(1, dto.getCards().getCreditCards().size());
    assertEquals(1, dto.getCards().getDebitCards().size());
  }

  @Test
  @DisplayName("Should handle empty collections gracefully")
  void shouldHandleEmptyCollections() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-empty")
        .timestamp(Instant.now())
        .accounts(List.of())
        .credits(List.of())
        .creditCards(List.of())
        .debitCards(List.of())
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertNotNull(dto.getAccounts());
    assertTrue(dto.getAccounts().isEmpty());
    assertNotNull(dto.getCredits());
    assertTrue(dto.getCredits().isEmpty());
    assertNotNull(dto.getCards());
    assertNotNull(dto.getCards().getCreditCards());
    assertTrue(dto.getCards().getCreditCards().isEmpty());
  }

  @Test
  @DisplayName("Should handle invalid enum values gracefully")
  void shouldHandleInvalidEnumValues() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-invalid")
        .timestamp(Instant.now())
        .accounts(List.of(
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC001")
                .accountType("INVALID_TYPE")
                .currency("INVALID_CURRENCY")
                .status("INVALID_STATUS")
                .balance(1000.0)
                .build()
        ))
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertEquals(1, dto.getAccounts().size());
    // Should handle invalid enums gracefully by returning null
    assertNotNull(dto.getAccounts().get(0));
  }

  @Test
  @DisplayName("Should map timestamp correctly")
  void shouldMapTimestampCorrectly() {
    Instant now = Instant.now();
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-timestamp")
        .timestamp(now)
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertNotNull(dto.getQueryTimestamp());
  }

  @Test
  @DisplayName("Should handle null timestamp")
  void shouldHandleNullTimestamp() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-no-timestamp")
        .timestamp(null)
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertNull(dto.getQueryTimestamp());
  }

  @Test
  @DisplayName("Should map multiple accounts with different types")
  void shouldMapMultipleAccountsWithDifferentTypes() {
    ProductsQueryResponse response = ProductsQueryResponse.builder()
        .customerId("customer-multi")
        .timestamp(Instant.now())
        .accounts(List.of(
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC001")
                .accountType("SAVINGS")
                .currency("PEN")
                .balance(5000.0)
                .status("ACTIVE")
                .build(),
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC002")
                .accountType("CHECKING")
                .currency("USD")
                .balance(3000.0)
                .status("ACTIVE")
                .build(),
            ProductsQueryResponse.AccountInfo.builder()
                .accountId("ACC003")
                .accountType("TIME_DEPOSIT")
                .currency("PEN")
                .balance(10000.0)
                .status("ACTIVE")
                .build()
        ))
        .build();

    ProductSummaryDTO dto = mapper.toDto(response);

    assertNotNull(dto);
    assertEquals(3, dto.getTotalProducts());
    assertEquals(3, dto.getAccounts().size());
  }
}
