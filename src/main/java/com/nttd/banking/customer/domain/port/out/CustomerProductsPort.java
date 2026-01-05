package com.nttd.banking.customer.domain.port.out;

import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import reactor.core.publisher.Mono;

/**
 * Output port for querying customer products from external microservices.
 * This port abstracts the communication with account-service, credit-service,
 * and card-service via Kafka messaging.
 *
 * <p>The implementation of this port should handle the asynchronous
 * request-reply pattern using Kafka topics.</p>
 *
 * @author NTT Data
 * @version 1.0
 * @see ProductsQueryResponse
 */
public interface CustomerProductsPort {

  /**
   * Queries all products for a customer from external microservices.
   *
   * <p>This method sends a request to aggregator service or directly to
   * account-service, credit-service, and card-service via Kafka and waits
   * for the aggregated response.</p>
   *
   * @param customerId   the ID of the customer to query products for
   * @param customerType the type of customer (PERSONAL or BUSINESS)
   * @return a Mono emitting the products query response
   */
  Mono<ProductsQueryResponse> queryCustomerProducts(String customerId, String customerType);

  /**
   * Queries only active products for a customer.
   *
   * @param customerId   the ID of the customer to query products for
   * @param customerType the type of customer (PERSONAL or BUSINESS)
   * @param activeOnly   whether to return only active products
   * @return a Mono emitting the products query response
   */
  Mono<ProductsQueryResponse> queryCustomerProducts(
      String customerId,
      String customerType,
      boolean activeOnly);
}
