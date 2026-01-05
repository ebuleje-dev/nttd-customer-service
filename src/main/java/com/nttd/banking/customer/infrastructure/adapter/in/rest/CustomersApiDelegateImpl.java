package com.nttd.banking.customer.infrastructure.adapter.in.rest;

import com.nttd.banking.customer.api.CustomersApiDelegate;
import com.nttd.banking.customer.application.mapper.CustomerMapper;
import com.nttd.banking.customer.application.mapper.ProductSummaryMapper;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.port.in.CreateCustomerUseCase;
import com.nttd.banking.customer.domain.port.in.DeleteCustomerUseCase;
import com.nttd.banking.customer.domain.port.in.FindCustomerUseCase;
import com.nttd.banking.customer.domain.port.in.GetCustomerProductsUseCase;
import com.nttd.banking.customer.domain.port.in.UpdateCustomerUseCase;
import com.nttd.banking.customer.domain.port.in.UpdateProfileUseCase;
import com.nttd.banking.customer.model.dto.CustomerRequestDTO;
import com.nttd.banking.customer.model.dto.CustomerResponseDTO;
import com.nttd.banking.customer.model.dto.CustomerUpdateDTO;
import com.nttd.banking.customer.model.dto.ProductSummaryDTO;
import com.nttd.banking.customer.model.dto.ProfileUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST API delegate implementation for customers endpoints.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomersApiDelegateImpl implements CustomersApiDelegate {

  private final CreateCustomerUseCase createCustomerUseCase;
  private final FindCustomerUseCase findCustomerUseCase;
  private final UpdateCustomerUseCase updateCustomerUseCase;
  private final DeleteCustomerUseCase deleteCustomerUseCase;
  private final UpdateProfileUseCase updateProfileUseCase;
  private final GetCustomerProductsUseCase getCustomerProductsUseCase;
  private final CustomerMapper customerMapper;
  private final ProductSummaryMapper productSummaryMapper;

  @Override
  public Mono<ResponseEntity<CustomerResponseDTO>> createCustomer(
      Mono<CustomerRequestDTO> customerRequestDto, ServerWebExchange exchange) {

    log.debug("REST: Creating new customer");

    return customerRequestDto
        .doOnNext(dto -> log.debug("Request DTO received: customerType={}", dto.getCustomerType()))
        .map(customerMapper::toDomain)
        .flatMap(createCustomerUseCase::execute)
        .map(customerMapper::toResponseDto)
        .doOnNext(dto -> log.debug("Customer created with id: {}", dto.getId()))
        .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
  }

  @Override
  public Mono<ResponseEntity<Flux<CustomerResponseDTO>>> getAllCustomers(
      Integer page, Integer size, ServerWebExchange exchange) {

    int pageNumber = (page != null) ? page : 0;
    int pageSize = (size != null) ? size : 20;

    log.debug("REST: Getting all customers (page={}, size={})", pageNumber, pageSize);

    Flux<CustomerResponseDTO> customers =
        findCustomerUseCase.findAll(pageNumber, pageSize).map(customerMapper::toResponseDto);

    return Mono.just(ResponseEntity.ok(customers));
  }

  @Override
  public Mono<ResponseEntity<CustomerResponseDTO>> getCustomerById(
      String id, ServerWebExchange exchange) {

    log.debug("REST: Getting customer by id: {}", id);

    return findCustomerUseCase
        .findById(id)
        .map(customerMapper::toResponseDto)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CustomerResponseDTO>> updateCustomer(
      String id, Mono<CustomerUpdateDTO> customerUpdateDto, ServerWebExchange exchange) {

    log.debug("REST: Updating customer with id: {}", id);

    return customerUpdateDto
        .map(this::mapUpdateDtoToPartialCustomer)
        .flatMap(updates -> updateCustomerUseCase.update(id, updates))
        .map(customerMapper::toResponseDto)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteCustomer(String id, ServerWebExchange exchange) {

    log.debug("REST: Deleting customer with id: {}", id);

    return deleteCustomerUseCase
        .delete(id)
        .then(Mono.just(ResponseEntity.noContent().<Void>build()));
  }

  @Override
  public Mono<ResponseEntity<CustomerResponseDTO>> updateCustomerProfile(
      String id, Mono<ProfileUpdateDTO> profileUpdateDto, ServerWebExchange exchange) {

    log.info("REST: Updating profile for customer with id: {}", id);

    return profileUpdateDto
        .doOnNext(dto -> log.debug("Received profile update request: profileType={}",
            dto.getProfileType()))
        .map(dto -> dto.getProfileType().getValue())
        .doOnNext(profileType -> log.debug(
            "Extracted profileType={}, calling use case", profileType))
        .flatMap(profileType -> updateProfileUseCase.updateProfile(id, profileType))
        .doOnSuccess(customer -> log.info("Profile update successful for customer: {}", id))
        .doOnError(error -> log.error("Profile update failed for customer {}: {} [{}]",
            id, error.getMessage(), error.getClass().getSimpleName()))
        .map(customerMapper::toResponseDto)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CustomerResponseDTO>> getCustomerByDocument(
      String documentNumber, ServerWebExchange exchange) {

    log.debug("REST: Getting customer by document: {}", documentNumber);

    return findCustomerUseCase
        .findByDocumentNumber(documentNumber)
        .map(customerMapper::toResponseDto)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CustomerResponseDTO>> getCustomerByEmail(
      String email, ServerWebExchange exchange) {

    log.debug("REST: Getting customer by email: {}", email);

    return findCustomerUseCase
        .findByEmail(email)
        .map(customerMapper::toResponseDto)
        .map(ResponseEntity::ok);
  }

  /**
   * Gets all products for a customer from external microservices.
   * Queries account-service, credit-service, and card-service via Kafka.
   *
   * @param id       the customer ID
   * @param exchange the server web exchange
   * @return a Mono emitting the ResponseEntity with ProductSummaryDTO
   */
  @Override
  public Mono<ResponseEntity<ProductSummaryDTO>> getCustomerProducts(
      String id, ServerWebExchange exchange) {

    log.debug("REST: Getting products for customer: {}", id);

    return getCustomerProductsUseCase
        .getProducts(id)
        .map(productSummaryMapper::toDto)
        .doOnSuccess(dto -> log.debug("Retrieved {} products for customer: {}",
            dto.getTotalProducts(), id))
        .map(ResponseEntity::ok);
  }

  private Customer mapUpdateDtoToPartialCustomer(CustomerUpdateDTO dto) {
    return PersonalCustomer.builder()
        .email(dto.getEmail())
        .phoneNumber(dto.getPhoneNumber())
        .address(dto.getAddress())
        .build();
  }
}
