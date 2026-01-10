package com.nttd.banking.customer.domain.service;

import com.nttd.banking.customer.domain.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.port.in.GetCustomerProductsUseCase;
import com.nttd.banking.customer.domain.port.out.CustomerProductsPort;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the GetCustomerProductsUseCase.
 * Retrieves customer products from external microservices via Kafka.
 *
 * <p>This service first validates the customer exists, then delegates
 * to the CustomerProductsPort to query products from account-service,
 * credit-service, and card-service.</p>
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetCustomerProductsUseCaseImpl implements GetCustomerProductsUseCase {

  private final CustomerRepository customerRepository;
  private final CustomerProductsPort customerProductsPort;

  /**
   * {@inheritDoc}
   */
  @Override
  public Mono<ProductsQueryResponse> getProducts(String customerId) {
    log.debug("Getting products for customer: {}", customerId);

    return customerRepository.findById(customerId)
        .switchIfEmpty(Mono.error(
            new CustomerNotFoundException("Customer not found with id: " + customerId)))
        .flatMap(this::queryProducts)
        .doOnSuccess(response ->
            log.info("Retrieved {} products for customer: {}",
                response.getTotalProducts(), customerId))
        .doOnError(error ->
            log.error("Error getting products for customer {}: {}",
                customerId, error.getMessage()));
  }

  /**
   * Queries products for the given customer.
   *
   * @param customer the customer to query products for
   * @return a Mono emitting the products query response
   */
  private Mono<ProductsQueryResponse> queryProducts(Customer customer) {
    log.debug("Querying products via Kafka for customer: {} (type: {})",
        customer.getId(), customer.getCustomerType());

    return customerProductsPort.queryCustomerProducts(
        customer.getId(),
        customer.getCustomerType().name());
  }
}
