package com.nttd.banking.customer.domain.service;

import com.nttd.banking.customer.application.exception.BusinessValidationException;
import com.nttd.banking.customer.application.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.model.enums.BusinessProfile;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import com.nttd.banking.customer.domain.port.in.UpdateProfileUseCase;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the update profile use case.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProfileUseCaseImpl implements UpdateProfileUseCase {

  private final CustomerRepository customerRepository;
  private final CustomerCacheRepository cacheRepository;

  private static final Duration CACHE_TTL = Duration.ofHours(1);

  @Override
  public Mono<Customer> updateProfile(String id, String profileType) {
    log.info("Updating customer {} to profile: {}", id, profileType);

    return customerRepository
        .findById(id)
        .switchIfEmpty(Mono.error(CustomerNotFoundException.byId(id)))
        .flatMap(customer -> applyProfileUpdate(customer, profileType))
        .flatMap(customerRepository::save)
        .flatMap(
            updated ->
                evictCache(updated)
                    .then(cacheRepository.save(updated, CACHE_TTL))
                    .thenReturn(updated))
        .doOnSuccess(
            updated ->
                log.info("Customer {} updated to profile {} successfully", id, profileType))
        .doOnError(
            error ->
                log.error(
                    "Error updating customer {} to profile {}: {}",
                    id,
                    profileType,
                    error.getMessage()));
  }

  /**
   * Applies profile update according to customer type.
   *
   * @param customer customer to update
   * @param profileType profile type (VIP or PYME)
   * @return Mono with updated customer
   */
  private Mono<Customer> applyProfileUpdate(Customer customer, String profileType) {
    if ("VIP".equalsIgnoreCase(profileType)) {
      return validateAndUpdateToVip(customer);
    } else if ("PYME".equalsIgnoreCase(profileType)) {
      return validateAndUpdateToPyme(customer);
    } else if ("STANDARD".equalsIgnoreCase(profileType)) {
      return downgradeToStandard(customer);
    } else {
      return Mono.error(
          new IllegalArgumentException("Invalid profile type: " + profileType));
    }
  }

  /**
   * Validates and updates a personal customer to VIP.
   *
   * @param customer customer to update
   * @return Mono with updated customer
   */
  private Mono<Customer> validateAndUpdateToVip(Customer customer) {
    if (customer.getCustomerType() != CustomerType.PERSONAL) {
      return Mono.error(
          new BusinessValidationException("Only PERSONAL customers can have VIP profile"));
    }

    PersonalCustomer personalCustomer = (PersonalCustomer) customer;

    personalCustomer.setPersonalProfile(PersonalProfile.VIP);
    personalCustomer.setUpdatedAt(Instant.now());

    log.debug("VIP profile validation successful for customer: {}", customer.getId());
    return Mono.just(personalCustomer);
  }

  /**
   * Validates and updates a business customer to PYME.
   *
   * @param customer customer to update
   * @return Mono with updated customer
   */
  private Mono<Customer> validateAndUpdateToPyme(Customer customer) {
    if (customer.getCustomerType() != CustomerType.BUSINESS) {
      return Mono.error(
          new BusinessValidationException("Only BUSINESS customers can have PYME profile"));
    }

    BusinessCustomer businessCustomer = (BusinessCustomer) customer;

    businessCustomer.setBusinessProfile(BusinessProfile.PYME);
    businessCustomer.setUpdatedAt(Instant.now());

    log.debug("PYME profile validation successful for customer: {}", customer.getId());
    return Mono.just(businessCustomer);
  }

  /**
   * Downgrades a customer to STANDARD profile.
   *
   * @param customer customer to downgrade
   * @return Mono with updated customer
   */
  private Mono<Customer> downgradeToStandard(Customer customer) {
    customer.setUpdatedAt(Instant.now());

    if (customer instanceof PersonalCustomer personalCustomer) {
      personalCustomer.setPersonalProfile(PersonalProfile.STANDARD);
      log.debug("Downgraded personal customer {} to STANDARD", customer.getId());
    } else if (customer instanceof BusinessCustomer businessCustomer) {
      businessCustomer.setBusinessProfile(BusinessProfile.STANDARD);
      log.debug("Downgraded business customer {} to STANDARD", customer.getId());
    }

    return Mono.just(customer);
  }

  /**
   * Evicts all cache entries for a customer.
   *
   * @param customer customer to evict from cache
   * @return Mono that completes when cache is evicted
   */
  private Mono<Void> evictCache(Customer customer) {
    return Mono.when(
        cacheRepository.evict(customer.getId()),
        cacheRepository.evictByEmail(customer.getEmail()),
        cacheRepository.evictByDocumentNumber(customer.getDocumentNumber()));
  }
}
