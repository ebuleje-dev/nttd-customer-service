package com.nttd.banking.customer.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customers")
public class CustomerEntity {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    private String documentType;     // enum en DTO (DNI, CEX, PASSPORT, RUC)
    private String documentNumber;

    private String email;
    private String phone;
    private String address;

    private String customerType;     // enum en DTO (PERSONAL, BUSINESS)
    private String customerProfile;  // enum en DTO (STANDARD, VIP, PYME)

    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
