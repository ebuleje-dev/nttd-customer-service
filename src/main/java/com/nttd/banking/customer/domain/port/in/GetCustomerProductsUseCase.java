package com.nttd.banking.customer.domain.port.in;

import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import reactor.core.publisher.Mono;

/**
 * Input port (use case) for getting customer products from external microservices.
 * This use case queries account-service, credit-service, and card-service
 * to retrieve all products associated with a customer.
 *
 * @author NTT Data
 * @version 1.0
 */
public interface GetCustomerProductsUseCase {

  /**
   * Gets all products for a customer.
   *
   * <p>This method first validates that the customer exists, then queries
   * external microservices via Kafka to retrieve all associated products
   * including accounts, credits, and cards.</p>
   *
   * @param customerId the ID of the customer to get products for
   * @return a Mono emitting the products query response
   * @throws com.nttd.banking.customer.application.exception.CustomerNotFoundException
   *         if the customer does not exist
   */
  Mono<ProductsQueryResponse> getProducts(String customerId);
}
