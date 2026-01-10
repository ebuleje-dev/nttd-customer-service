package com.nttd.banking.customer.domain.service;

import com.nttd.banking.customer.domain.exception.BusinessValidationException;
import com.nttd.banking.customer.domain.exception.CustomerNotFoundException;
import com.nttd.banking.customer.domain.exception.ProfileUpdateNotAllowedException;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.model.enums.BusinessProfile;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import com.nttd.banking.customer.domain.port.in.UpdateProfileUseCase;
import com.nttd.banking.customer.domain.port.out.CreditCardValidationPort;
import com.nttd.banking.customer.domain.port.out.CustomerCacheRepository;
import com.nttd.banking.customer.domain.port.out.CustomerEventPublisher;
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
  private final CreditCardValidationPort creditCardValidationPort;
  private final CustomerEventPublisher eventPublisher;

  private static final Duration CACHE_TTL = Duration.ofHours(1);

  @Override
  public Mono<Customer> updateProfile(String id, String profileType) {
    log.info("Updating customer {} to profile: {}", id, profileType);

    return customerRepository
        .findById(id)
        .switchIfEmpty(Mono.error(CustomerNotFoundException.byId(id)))
        .flatMap(customer -> {
          String oldProfile = getCustomerProfile(customer);
          return applyProfileUpdate(customer, profileType)
              .map(updated -> new ProfileUpdateContext(updated, oldProfile, profileType));
        })
        .flatMap(ctx -> customerRepository.save(ctx.customer())
            .map(saved -> new ProfileUpdateContext(saved, ctx.oldProfile(), ctx.newProfile())))
        .flatMap(ctx ->
            evictCache(ctx.customer())
                .then(cacheRepository.save(ctx.customer(), CACHE_TTL))
                .then(eventPublisher.publishProfileUpdated(
                    ctx.customer(), ctx.oldProfile(), ctx.newProfile()))
                .thenReturn(ctx.customer()))
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
   * Gets the current profile of a customer.
   *
   * @param customer the customer
   * @return the profile name (VIP, PYME, or STANDARD)
   */
  private String getCustomerProfile(Customer customer) {
    if (customer instanceof PersonalCustomer personal) {
      return personal.getPersonalProfile() != null
          ? personal.getPersonalProfile().name() : "STANDARD";
    } else if (customer instanceof BusinessCustomer business) {
      return business.getBusinessProfile() != null
          ? business.getBusinessProfile().name() : "STANDARD";
    }
    return "STANDARD";
  }

  /**
   * Context object to carry profile update information through the reactive chain.
   */
  private record ProfileUpdateContext(Customer customer, String oldProfile, String newProfile) {
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
   * Requires credit card validation via Kafka.
   *
   * @param customer customer to update
   * @return Mono with updated customer
   */
  private Mono<Customer> validateAndUpdateToVip(Customer customer) {
    if (customer.getCustomerType() != CustomerType.PERSONAL) {
      return Mono.error(
          ProfileUpdateNotAllowedException.invalidProfileForType(
              customer.getId(), customer.getCustomerType().name(), "VIP"));
    }

    PersonalCustomer personalCustomer = (PersonalCustomer) customer;

    log.info("Validating credit card for VIP upgrade: customerId={}", customer.getId());

    return creditCardValidationPort
        .validateHasActiveCreditCard(
            customer.getId(),
            customer.getCustomerType().name(),
            "VIP")
        .doOnSubscribe(s -> log.debug("Starting credit card validation for customer: {}",
            customer.getId()))
        .doOnSuccess(result -> log.debug("Credit card validation returned: {} for customer: {}",
            result, customer.getId()))
        .doOnError(error -> log.error("Credit card validation error for customer {}: {} [{}]",
            customer.getId(), error.getMessage(), error.getClass().getSimpleName()))
        .flatMap(hasActiveCreditCard -> {
          log.debug("Processing validation result: hasActiveCreditCard={} for customer: {}",
              hasActiveCreditCard, customer.getId());

          if (!hasActiveCreditCard) {
            log.warn("VIP upgrade rejected: customer {} has no active credit card",
                customer.getId());
            return Mono.error(
                ProfileUpdateNotAllowedException.noCreditCard(customer.getId(), "VIP"));
          }

          personalCustomer.setPersonalProfile(PersonalProfile.VIP);
          personalCustomer.setUpdatedAt(Instant.now());

          log.info("VIP profile validation successful for customer: {}", customer.getId());
          return Mono.just((Customer) personalCustomer);
        });
  }

  /**
   * Validates and updates a business customer to PYME.
   * Requires credit card validation via Kafka.
   *
   * @param customer customer to update
   * @return Mono with updated customer
   */
  private Mono<Customer> validateAndUpdateToPyme(Customer customer) {
    if (customer.getCustomerType() != CustomerType.BUSINESS) {
      return Mono.error(
          ProfileUpdateNotAllowedException.invalidProfileForType(
              customer.getId(), customer.getCustomerType().name(), "PYME"));
    }

    BusinessCustomer businessCustomer = (BusinessCustomer) customer;

    log.info("Validating credit card for PYME upgrade: customerId={}", customer.getId());

    return creditCardValidationPort
        .validateHasActiveCreditCard(
            customer.getId(),
            customer.getCustomerType().name(),
            "PYME")
        .doOnSubscribe(s -> log.debug("Starting credit card validation for customer: {}",
            customer.getId()))
        .doOnSuccess(result -> log.debug("Credit card validation returned: {} for customer: {}",
            result, customer.getId()))
        .doOnError(error -> log.error("Credit card validation error for customer {}: {} [{}]",
            customer.getId(), error.getMessage(), error.getClass().getSimpleName()))
        .flatMap(hasActiveCreditCard -> {
          log.debug("Processing validation result: hasActiveCreditCard={} for customer: {}",
              hasActiveCreditCard, customer.getId());

          if (!hasActiveCreditCard) {
            log.warn("PYME upgrade rejected: customer {} has no active credit card",
                customer.getId());
            return Mono.error(
                ProfileUpdateNotAllowedException.noCreditCard(customer.getId(), "PYME"));
          }

          businessCustomer.setBusinessProfile(BusinessProfile.PYME);
          businessCustomer.setUpdatedAt(Instant.now());

          log.info("PYME profile validation successful for customer: {}", customer.getId());
          return Mono.just((Customer) businessCustomer);
        });
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
