package com.nttd.banking.customer.infrastructure.adapter.out.persistence;

import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.SignerRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded MongoDB entity for authorized signers.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizedSignerEntity {

  /** Signer's first name. */
  private String firstName;

  /** Signer's last name. */
  private String lastName;

  /** Signer's document type. */
  private DocumentType documentType;

  /** Signer's document number. */
  private String documentNumber;

  /** Signer's role. */
  private SignerRole role;
}
