package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing a request to query all products for a customer.
 * This event is sent to aggregator service or directly to account-service,
 * credit-service, and card-service via Kafka.
 *
 * <p>The request uses a correlation ID for matching request with response
 * in the asynchronous Kafka communication pattern.</p>
 *
 * @author NTT Data
 * @version 1.0
 * @see ProductsQueryResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductsQueryRequest {

  /**
   * Unique correlation ID for matching request with response.
   */
  private String correlationId;

  /**
   * ID of the customer to query products for.
   */
  private String customerId;

  /**
   * Type of customer (PERSONAL or BUSINESS).
   */
  private String customerType;

  /**
   * Timestamp when the request was created.
   */
  private Instant timestamp;

  /**
   * Flag indicating if only active products should be returned.
   */
  private boolean activeOnly;

  /**
   * Creates a new products query request for a customer.
   *
   * @param customerId   the customer ID to query products for
   * @param customerType the type of customer (PERSONAL or BUSINESS)
   * @return a new ProductsQueryRequest with generated correlation ID
   */
  public static ProductsQueryRequest create(String customerId, String customerType) {
    return ProductsQueryRequest.builder()
        .correlationId(UUID.randomUUID().toString())
        .customerId(customerId)
        .customerType(customerType)
        .timestamp(Instant.now())
        .activeOnly(true)
        .build();
  }

  /**
   * Creates a new products query request with custom active filter.
   *
   * @param customerId   the customer ID to query products for
   * @param customerType the type of customer (PERSONAL or BUSINESS)
   * @param activeOnly   whether to return only active products
   * @return a new ProductsQueryRequest with generated correlation ID
   */
  public static ProductsQueryRequest create(
      String customerId,
      String customerType,
      boolean activeOnly) {
    return ProductsQueryRequest.builder()
        .correlationId(UUID.randomUUID().toString())
        .customerId(customerId)
        .customerType(customerType)
        .timestamp(Instant.now())
        .activeOnly(activeOnly)
        .build();
  }
}
