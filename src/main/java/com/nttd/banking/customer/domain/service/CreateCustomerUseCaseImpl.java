package com.nttd.banking.customer.domain.service;

import com.nttd.banking.customer.application.exception.BusinessValidationException;
import com.nttd.banking.customer.application.exception.DuplicateCustomerException;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.port.in.CreateCustomerUseCase;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the create customer use case.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCustomerUseCaseImpl implements CreateCustomerUseCase {

  private final CustomerRepository customerRepository;
  private final CustomerCacheRepository cacheRepository;

  private static final Duration CACHE_TTL = Duration.ofHours(1);

  @Override
  public Mono<Customer> execute(Customer customer) {
    log.info("Creating new customer of type: {}", customer.getCustomerType());

    return validateCustomer(customer)
        .then(checkEmailUniqueness(customer.getEmail()))
        .then(checkDocumentUniqueness(customer.getDocumentNumber()))
        .then(customerRepository.save(customer))
        .flatMap(saved -> cacheRepository.save(saved, CACHE_TTL).thenReturn(saved))
        .doOnSuccess(saved -> log.info("Customer created successfully with id: {}", saved.getId()))
        .doOnError(error -> log.error("Error creating customer: {}", error.getMessage()));
  }

  /**
   * Validates customer data according to type and business rules.
   *
   * @param customer customer to validate
   * @return Mono that completes if validation is successful
   */
  private Mono<Void> validateCustomer(Customer customer) {
    return Mono.fromRunnable(
        () -> {
          try {
            customer.validate();

            if (customer instanceof PersonalCustomer personalCustomer) {
              validatePersonalCustomer(personalCustomer);
            } else if (customer instanceof BusinessCustomer businessCustomer) {
              validateBusinessCustomer(businessCustomer);
            }

            log.debug("Customer validation successful");
          } catch (IllegalArgumentException e) {
            log.warn("Customer validation failed: {}", e.getMessage());
            throw new BusinessValidationException(e.getMessage(), e);
          }
        });
  }

  /**
   * Validates personal customer specific rules.
   *
   * @param customer personal customer to validate
   * @throws BusinessValidationException if validation fails
   */
  private void validatePersonalCustomer(PersonalCustomer customer) {
    if (customer.getDateOfBirth() != null && customer.getAge() < 18) {
      throw new BusinessValidationException(
          "Personal customer must be at least 18 years old. Current age: " + customer.getAge());
    }

    if (customer.getDocumentType().name().equals("RUC")) {
      throw new BusinessValidationException(
          "Personal customer cannot have RUC as document type");
    }

    log.debug("Personal customer validation successful");
  }

  /**
   * Validates business customer specific rules.
   *
   * @param customer business customer to validate
   * @throws BusinessValidationException if validation fails
   */
  private void validateBusinessCustomer(BusinessCustomer customer) {
    if (!customer.getDocumentType().name().equals("RUC")) {
      throw new BusinessValidationException(
          "Business customer must have RUC as document type");
    }

    if (customer.getTaxId() != null && customer.getTaxId().length() != 11) {
      throw new BusinessValidationException("RUC must be exactly 11 digits");
    }

    if (customer.getBusinessName() == null || customer.getBusinessName().isBlank()) {
      throw new BusinessValidationException("Business name is required for business customers");
    }

    log.debug("Business customer validation successful");
  }

  /**
   * Checks email uniqueness.
   *
   * @param email email to verify
   * @return Mono that completes if email is unique
   */
  private Mono<Void> checkEmailUniqueness(String email) {
    return customerRepository
        .existsByEmail(email)
        .flatMap(
            exists -> {
              if (exists) {
                log.warn("Email already exists: {}", email);
                return Mono.error(DuplicateCustomerException.byEmail(email));
              }
              return Mono.empty();
            });
  }

  /**
   * Checks document number uniqueness.
   *
   * @param documentNumber document number to verify
   * @return Mono that completes if document is unique
   */
  private Mono<Void> checkDocumentUniqueness(String documentNumber) {
    return customerRepository
        .existsByDocumentNumber(documentNumber)
        .flatMap(
            exists -> {
              if (exists) {
                log.warn("Document number already exists: {}", documentNumber);
                return Mono.error(DuplicateCustomerException.byDocument(documentNumber));
              }
              return Mono.empty();
            });
  }
}
