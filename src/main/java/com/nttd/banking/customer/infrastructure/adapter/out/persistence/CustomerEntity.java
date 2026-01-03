package com.nttd.banking.customer.infrastructure.adapter.out.persistence;

import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Abstract MongoDB entity for customers.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customers")
public abstract class CustomerEntity {

  /** Unique customer identifier. */
  @Id
  private String id;

  /** Customer type discriminator. */
  private CustomerType customerType;

  /** Document type. */
  private DocumentType documentType;

  /** Document number (unique). */
  @Indexed(unique = true)
  private String documentNumber;

  /** Email (unique). */
  @Indexed(unique = true)
  private String email;

  /** Phone number. */
  private String phoneNumber;

  /** Address. */
  private String address;

  /** Customer status. */
  @Indexed
  private CustomerStatus status;

  /** Creation timestamp. */
  private Instant createdAt;

  /** Last update timestamp. */
  private Instant updatedAt;
}
