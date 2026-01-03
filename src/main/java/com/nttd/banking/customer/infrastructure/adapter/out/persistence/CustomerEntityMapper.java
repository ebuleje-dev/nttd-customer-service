package com.nttd.banking.customer.infrastructure.adapter.out.persistence;

import com.nttd.banking.customer.domain.model.AuthorizedSigner;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper between domain entities and persistence entities.
 *
 * @author NTT Data
 * @version 1.0
 */
@Component
public class CustomerEntityMapper {

  /**
   * Converts a domain entity to a persistence entity.
   *
   * @param customer domain entity
   * @return persistence entity (PersonalCustomerEntity or BusinessCustomerEntity)
   * @throws IllegalArgumentException if customer type is unknown
   */
  public CustomerEntity toPersistence(Customer customer) {
    if (customer == null) {
      return null;
    }

    if (customer instanceof PersonalCustomer personalCustomer) {
      return PersonalCustomerEntity.builder()
          .id(personalCustomer.getId())
          .customerType(personalCustomer.getCustomerType())
          .documentType(personalCustomer.getDocumentType())
          .documentNumber(personalCustomer.getDocumentNumber())
          .email(personalCustomer.getEmail())
          .phoneNumber(personalCustomer.getPhoneNumber())
          .address(personalCustomer.getAddress())
          .status(personalCustomer.getStatus())
          .createdAt(personalCustomer.getCreatedAt())
          .updatedAt(personalCustomer.getUpdatedAt())
          .firstName(personalCustomer.getFirstName())
          .lastName(personalCustomer.getLastName())
          .dateOfBirth(personalCustomer.getDateOfBirth())
          .gender(personalCustomer.getGender())
          .personalProfile(personalCustomer.getPersonalProfile())
          .build();
    }

    if (customer instanceof BusinessCustomer businessCustomer) {
      return BusinessCustomerEntity.builder()
          .id(businessCustomer.getId())
          .customerType(businessCustomer.getCustomerType())
          .documentType(businessCustomer.getDocumentType())
          .documentNumber(businessCustomer.getDocumentNumber())
          .email(businessCustomer.getEmail())
          .phoneNumber(businessCustomer.getPhoneNumber())
          .address(businessCustomer.getAddress())
          .status(businessCustomer.getStatus())
          .createdAt(businessCustomer.getCreatedAt())
          .updatedAt(businessCustomer.getUpdatedAt())
          .businessName(businessCustomer.getBusinessName())
          .businessType(businessCustomer.getBusinessType())
          .taxId(businessCustomer.getTaxId())
          .businessProfile(businessCustomer.getBusinessProfile())
          .authorizedSigners(toSignersPersistence(businessCustomer.getAuthorizedSigners()))
          .build();
    }

    throw new IllegalArgumentException(
        "Unknown customer type: " + customer.getClass().getName());
  }

  /**
   * Converts a persistence entity to a domain entity.
   *
   * @param entity persistence entity
   * @return domain entity (PersonalCustomer or BusinessCustomer)
   * @throws IllegalArgumentException if entity type is unknown
   */
  public Customer toDomain(CustomerEntity entity) {
    if (entity == null) {
      return null;
    }

    if (entity instanceof PersonalCustomerEntity personalEntity) {
      return PersonalCustomer.builder()
          .id(personalEntity.getId())
          .customerType(personalEntity.getCustomerType())
          .documentType(personalEntity.getDocumentType())
          .documentNumber(personalEntity.getDocumentNumber())
          .email(personalEntity.getEmail())
          .phoneNumber(personalEntity.getPhoneNumber())
          .address(personalEntity.getAddress())
          .status(personalEntity.getStatus())
          .createdAt(personalEntity.getCreatedAt())
          .updatedAt(personalEntity.getUpdatedAt())
          .firstName(personalEntity.getFirstName())
          .lastName(personalEntity.getLastName())
          .dateOfBirth(personalEntity.getDateOfBirth())
          .gender(personalEntity.getGender())
          .personalProfile(personalEntity.getPersonalProfile())
          .build();
    }

    if (entity instanceof BusinessCustomerEntity businessEntity) {
      return BusinessCustomer.builder()
          .id(businessEntity.getId())
          .customerType(businessEntity.getCustomerType())
          .documentType(businessEntity.getDocumentType())
          .documentNumber(businessEntity.getDocumentNumber())
          .email(businessEntity.getEmail())
          .phoneNumber(businessEntity.getPhoneNumber())
          .address(businessEntity.getAddress())
          .status(businessEntity.getStatus())
          .createdAt(businessEntity.getCreatedAt())
          .updatedAt(businessEntity.getUpdatedAt())
          .businessName(businessEntity.getBusinessName())
          .businessType(businessEntity.getBusinessType())
          .taxId(businessEntity.getTaxId())
          .businessProfile(businessEntity.getBusinessProfile())
          .authorizedSigners(toSignersDomain(businessEntity.getAuthorizedSigners()))
          .build();
    }

    throw new IllegalArgumentException("Unknown entity type: " + entity.getClass().getName());
  }

  /**
   * Converts a domain signer to a persistence signer.
   *
   * @param signer domain signer
   * @return persistence signer
   */
  public AuthorizedSignerEntity toSignerPersistence(AuthorizedSigner signer) {
    if (signer == null) {
      return null;
    }

    return AuthorizedSignerEntity.builder()
        .firstName(signer.getFirstName())
        .lastName(signer.getLastName())
        .documentType(signer.getDocumentType())
        .documentNumber(signer.getDocumentNumber())
        .role(signer.getRole())
        .build();
  }

  /**
   * Converts a persistence signer to a domain signer.
   *
   * @param entity persistence signer
   * @return domain signer
   */
  public AuthorizedSigner toSignerDomain(AuthorizedSignerEntity entity) {
    if (entity == null) {
      return null;
    }

    return AuthorizedSigner.builder()
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .documentType(entity.getDocumentType())
        .documentNumber(entity.getDocumentNumber())
        .role(entity.getRole())
        .build();
  }

  /**
   * Converts a list of domain signers to persistence signers.
   *
   * @param signers list of domain signers
   * @return list of persistence signers
   */
  public List<AuthorizedSignerEntity> toSignersPersistence(List<AuthorizedSigner> signers) {
    if (signers == null) {
      return Collections.emptyList();
    }

    return signers.stream().map(this::toSignerPersistence).collect(Collectors.toList());
  }

  /**
   * Converts a list of persistence signers to domain signers.
   *
   * @param entities list of persistence signers
   * @return list of domain signers
   */
  public List<AuthorizedSigner> toSignersDomain(List<AuthorizedSignerEntity> entities) {
    if (entities == null) {
      return Collections.emptyList();
    }

    return entities.stream().map(this::toSignerDomain).collect(Collectors.toList());
  }
}
