package com.nttd.banking.customer.application.mapper;

import com.nttd.banking.customer.domain.model.AuthorizedSigner;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.model.enums.BusinessProfile;
import com.nttd.banking.customer.domain.model.enums.BusinessType;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.Gender;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import com.nttd.banking.customer.domain.model.enums.SignerRole;
import com.nttd.banking.customer.model.dto.AuthorizedSignerDTO;
import com.nttd.banking.customer.model.dto.CustomerRequestDTO;
import com.nttd.banking.customer.model.dto.CustomerResponseDTO;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper between API DTOs and domain objects.
 *
 * @author NTT Data
 * @version 1.0
 */
@Component
public class CustomerMapper {

  /**
   * Converts CustomerRequestDTO to domain Customer.
   *
   * @param dto request DTO
   * @return domain Customer (PersonalCustomer or BusinessCustomer)
   * @throws IllegalArgumentException if customerType is invalid
   */
  public Customer toDomain(CustomerRequestDTO dto) {
    if (dto == null) {
      return null;
    }

    CustomerType customerType = CustomerType.valueOf(dto.getCustomerType().getValue());

    if (customerType == CustomerType.PERSONAL) {
      return PersonalCustomer.builder()
          .customerType(customerType)
          .documentType(mapDocumentType(dto.getDocumentType()))
          .documentNumber(dto.getDocumentNumber())
          .email(dto.getEmail())
          .phoneNumber(dto.getPhoneNumber())
          .address(dto.getAddress())
          .status(CustomerStatus.ACTIVE)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .firstName(dto.getFirstName())
          .lastName(dto.getLastName())
          .dateOfBirth(
              dto.getDateOfBirth() != null
                  ? dto.getDateOfBirth().toInstant()
                  : null)
          .gender(dto.getGender() != null ? mapGender(dto.getGender()) : null)
          .personalProfile(
              dto.getPersonalProfile() != null
                  ? mapPersonalProfile(dto.getPersonalProfile())
                  : PersonalProfile.STANDARD)
          .build();
    } else if (customerType == CustomerType.BUSINESS) {
      return BusinessCustomer.builder()
          .customerType(customerType)
          .documentType(mapDocumentType(dto.getDocumentType()))
          .documentNumber(dto.getDocumentNumber())
          .email(dto.getEmail())
          .phoneNumber(dto.getPhoneNumber())
          .address(dto.getAddress())
          .status(CustomerStatus.ACTIVE)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .businessName(dto.getBusinessName())
          .businessType(
              dto.getBusinessType() != null ? mapBusinessType(dto.getBusinessType()) : null)
          .taxId(dto.getTaxId())
          .businessProfile(
              dto.getBusinessProfile() != null
                  ? mapBusinessProfile(dto.getBusinessProfile())
                  : BusinessProfile.STANDARD)
          .authorizedSigners(
              dto.getAuthorizedSigners() != null
                  ? dto.getAuthorizedSigners().stream()
                      .map(this::mapAuthorizedSignerToDomain)
                      .collect(Collectors.toList())
                  : Collections.emptyList())
          .build();
    }

    throw new IllegalArgumentException("Unknown customer type: " + dto.getCustomerType());
  }

  /**
   * Converts domain Customer to CustomerResponseDTO.
   *
   * @param customer domain object
   * @return response DTO
   */
  public CustomerResponseDTO toResponseDTO(Customer customer) {
    if (customer == null) {
      return null;
    }

    CustomerResponseDTO dto = new CustomerResponseDTO();
    dto.setId(customer.getId());
    dto.setCustomerType(mapCustomerTypeToDTO(customer.getCustomerType()));
    dto.setDocumentType(mapDocumentTypeToDTO(customer.getDocumentType()));
    dto.setDocumentNumber(customer.getDocumentNumber());
    dto.setEmail(customer.getEmail());
    dto.setPhoneNumber(customer.getPhoneNumber());
    dto.setAddress(customer.getAddress());
    dto.setStatus(mapCustomerStatusToDTO(customer.getStatus()));
    dto.setCreatedAt(instantToOffsetDateTime(customer.getCreatedAt()));
    dto.setUpdatedAt(instantToOffsetDateTime(customer.getUpdatedAt()));

    if (customer instanceof PersonalCustomer personalCustomer) {
      dto.setFirstName(personalCustomer.getFirstName());
      dto.setLastName(personalCustomer.getLastName());
      dto.setDateOfBirth(instantToOffsetDateTime(personalCustomer.getDateOfBirth()));
      dto.setGender(
          personalCustomer.getGender() != null
              ? mapGenderToDTO(personalCustomer.getGender())
              : null);
      dto.setPersonalProfile(
          personalCustomer.getPersonalProfile() != null
              ? mapPersonalProfileToDTO(personalCustomer.getPersonalProfile())
              : null);
    } else if (customer instanceof BusinessCustomer businessCustomer) {
      dto.setBusinessName(businessCustomer.getBusinessName());
      dto.setBusinessType(
          businessCustomer.getBusinessType() != null
              ? mapBusinessTypeToDTO(businessCustomer.getBusinessType())
              : null);
      dto.setTaxId(businessCustomer.getTaxId());
      dto.setBusinessProfile(
          businessCustomer.getBusinessProfile() != null
              ? mapBusinessProfileToDTO(businessCustomer.getBusinessProfile())
              : null);
      dto.setAuthorizedSigners(
          businessCustomer.getAuthorizedSigners() != null
              ? businessCustomer.getAuthorizedSigners().stream()
                  .map(this::mapAuthorizedSignerToDTO)
                  .collect(Collectors.toList())
              : Collections.emptyList());
    }

    return dto;
  }

  private DocumentType mapDocumentType(CustomerRequestDTO.DocumentTypeEnum dto) {
    return DocumentType.valueOf(dto.getValue());
  }

  private CustomerResponseDTO.DocumentTypeEnum mapDocumentTypeToDTO(DocumentType domain) {
    return CustomerResponseDTO.DocumentTypeEnum.fromValue(domain.name());
  }

  private Gender mapGender(CustomerRequestDTO.GenderEnum dto) {
    return Gender.valueOf(dto.getValue());
  }

  private CustomerResponseDTO.GenderEnum mapGenderToDTO(Gender domain) {
    return CustomerResponseDTO.GenderEnum.fromValue(domain.name());
  }

  private PersonalProfile mapPersonalProfile(CustomerRequestDTO.PersonalProfileEnum dto) {
    return PersonalProfile.valueOf(dto.getValue());
  }

  private CustomerResponseDTO.PersonalProfileEnum mapPersonalProfileToDTO(
      PersonalProfile domain) {
    return CustomerResponseDTO.PersonalProfileEnum.fromValue(domain.name());
  }

  private BusinessType mapBusinessType(CustomerRequestDTO.BusinessTypeEnum dto) {
    return BusinessType.valueOf(dto.getValue());
  }

  private CustomerResponseDTO.BusinessTypeEnum mapBusinessTypeToDTO(BusinessType domain) {
    return CustomerResponseDTO.BusinessTypeEnum.fromValue(domain.name());
  }

  private BusinessProfile mapBusinessProfile(CustomerRequestDTO.BusinessProfileEnum dto) {
    return BusinessProfile.valueOf(dto.getValue());
  }

  private CustomerResponseDTO.BusinessProfileEnum mapBusinessProfileToDTO(
      BusinessProfile domain) {
    return CustomerResponseDTO.BusinessProfileEnum.fromValue(domain.name());
  }

  private CustomerResponseDTO.CustomerTypeEnum mapCustomerTypeToDTO(CustomerType domain) {
    return CustomerResponseDTO.CustomerTypeEnum.fromValue(domain.name());
  }

  private CustomerResponseDTO.StatusEnum mapCustomerStatusToDTO(CustomerStatus domain) {
    return CustomerResponseDTO.StatusEnum.fromValue(domain.name());
  }

  private AuthorizedSigner mapAuthorizedSignerToDomain(AuthorizedSignerDTO dto) {
    if (dto == null) {
      return null;
    }

    return AuthorizedSigner.builder()
        .firstName(dto.getFirstName())
        .lastName(dto.getLastName())
        .documentType(DocumentType.valueOf(dto.getDocumentType().getValue()))
        .documentNumber(dto.getDocumentNumber())
        .role(SignerRole.valueOf(dto.getRole().getValue()))
        .build();
  }

  private AuthorizedSignerDTO mapAuthorizedSignerToDTO(AuthorizedSigner domain) {
    if (domain == null) {
      return null;
    }

    AuthorizedSignerDTO dto = new AuthorizedSignerDTO();
    dto.setFirstName(domain.getFirstName());
    dto.setLastName(domain.getLastName());
    dto.setDocumentType(
        AuthorizedSignerDTO.DocumentTypeEnum.fromValue(domain.getDocumentType().name()));
    dto.setDocumentNumber(domain.getDocumentNumber());
    dto.setRole(AuthorizedSignerDTO.RoleEnum.fromValue(domain.getRole().name()));

    return dto;
  }

  private OffsetDateTime instantToOffsetDateTime(Instant instant) {
    if (instant == null) {
      return null;
    }
    return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
  }
}
