package com.nttd.banking.customer.mapper;

import com.nttd.banking.customer.model.dto.CustomerRequestDTO;
import com.nttd.banking.customer.model.dto.CustomerResponseDTO;
import com.nttd.banking.customer.model.dto.CustomerUpdateDTO;
import com.nttd.banking.customer.model.entity.CustomerEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Mapper converter between DTOs (contract OpenAPI) and entity Mongo.
 */
@Component
public class CustomerMapper {

    public CustomerEntity toEntity(CustomerRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return CustomerEntity.builder()
                .id(UUID.randomUUID().toString())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .documentType(dto.getDocumentType() != null ? dto.getDocumentType().name() : null)
                .documentNumber(dto.getDocumentNumber())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .customerType(dto.getCustomerType() != null ? dto.getCustomerType().name() : null)
                .customerProfile(dto.getCustomerProfile() != null ? dto.getCustomerProfile().name() : null)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    public CustomerResponseDTO toResponse(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setDocumentNumber(entity.getDocumentNumber());
        dto.setActive(entity.isActive());

        if (entity.getDocumentType() != null) {
            dto.setDocumentType(CustomerResponseDTO.DocumentTypeEnum.valueOf(entity.getDocumentType()));
        }
        if (entity.getCustomerType() != null) {
            dto.setCustomerType(CustomerResponseDTO.CustomerTypeEnum.valueOf(entity.getCustomerType()));
        }
        if (entity.getCustomerProfile() != null) {
            dto.setCustomerProfile(CustomerResponseDTO.CustomerProfileEnum.valueOf(entity.getCustomerProfile()));
        }

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC));
        }

        return dto;
    }

    public void updateEntity(CustomerEntity entity, CustomerUpdateDTO dto) {
        if (dto == null) return;

        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getPhone() != null) entity.setPhone(dto.getPhone());
        if (dto.getAddress() != null) entity.setAddress(dto.getAddress());
        if (dto.getCustomerProfile() != null)
            entity.setCustomerProfile(dto.getCustomerProfile().name());

        entity.setUpdatedAt(Instant.now());
    }
}
