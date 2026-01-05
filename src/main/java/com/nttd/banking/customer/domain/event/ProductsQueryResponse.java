package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing a response containing all products for a customer.
 * This response aggregates data from account-service, credit-service, and card-service.
 *
 * <p>The response includes correlation ID to match with the original request
 * in the asynchronous Kafka communication pattern.</p>
 *
 * @author NTT Data
 * @version 1.0
 * @see ProductsQueryRequest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductsQueryResponse {

  /**
   * Correlation ID matching the original request.
   */
  private String correlationId;

  /**
   * ID of the customer.
   */
  private String customerId;

  /**
   * List of bank accounts.
   */
  @Builder.Default
  private List<AccountInfo> accounts = new ArrayList<>();

  /**
   * List of credits.
   */
  @Builder.Default
  private List<CreditInfo> credits = new ArrayList<>();

  /**
   * List of credit cards.
   */
  @Builder.Default
  private List<CreditCardInfo> creditCards = new ArrayList<>();

  /**
   * List of debit cards.
   */
  @Builder.Default
  private List<DebitCardInfo> debitCards = new ArrayList<>();

  /**
   * Timestamp when the response was created.
   */
  private Instant timestamp;

  /**
   * Indicates if the query was successful.
   */
  private boolean success;

  /**
   * Error message if the query failed.
   */
  private String errorMessage;

  /**
   * Creates an empty successful response for a customer.
   *
   * @param correlationId the correlation ID from the request
   * @param customerId    the customer ID
   * @return a new empty ProductsQueryResponse
   */
  public static ProductsQueryResponse empty(String correlationId, String customerId) {
    return ProductsQueryResponse.builder()
        .correlationId(correlationId)
        .customerId(customerId)
        .accounts(new ArrayList<>())
        .credits(new ArrayList<>())
        .creditCards(new ArrayList<>())
        .debitCards(new ArrayList<>())
        .timestamp(Instant.now())
        .success(true)
        .build();
  }

  /**
   * Creates an error response.
   *
   * @param correlationId the correlation ID from the request
   * @param customerId    the customer ID
   * @param errorMessage  the error message
   * @return a new error ProductsQueryResponse
   */
  public static ProductsQueryResponse error(
      String correlationId,
      String customerId,
      String errorMessage) {
    return ProductsQueryResponse.builder()
        .correlationId(correlationId)
        .customerId(customerId)
        .accounts(new ArrayList<>())
        .credits(new ArrayList<>())
        .creditCards(new ArrayList<>())
        .debitCards(new ArrayList<>())
        .timestamp(Instant.now())
        .success(false)
        .errorMessage(errorMessage)
        .build();
  }

  /**
   * Calculates the total number of products.
   *
   * @return total product count
   */
  public int getTotalProducts() {
    return accounts.size() + credits.size() + creditCards.size() + debitCards.size();
  }

  /**
   * Checks if the customer has any active products.
   *
   * @return true if customer has at least one product
   */
  public boolean hasActiveProducts() {
    return getTotalProducts() > 0;
  }

  /**
   * Account information from account-service.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AccountInfo {
    private String accountId;
    private String accountNumber;
    private String accountType;
    private String currency;
    private Double balance;
    private String status;
  }

  /**
   * Credit information from credit-service.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreditInfo {
    private String creditId;
    private String creditType;
    private Double creditLimit;
    private Double usedAmount;
    private Double availableAmount;
    private String currency;
    private String status;
  }

  /**
   * Credit card information from credit-card-service.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreditCardInfo {
    private String cardId;
    private String cardNumber;
    private String cardType;
    private Double creditLimit;
    private Double availableCredit;
    private String status;
  }

  /**
   * Debit card information from debit-card-service.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DebitCardInfo {
    private String cardId;
    private String cardNumber;
    private String cardType;
    private String linkedAccountId;
    private String status;
  }
}
