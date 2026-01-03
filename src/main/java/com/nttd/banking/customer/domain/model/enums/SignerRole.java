package com.nttd.banking.customer.domain.model.enums;

/**
 * Enumeration of authorized signer roles.
 *
 * @author NTT Data
 * @version 1.0
 */
public enum SignerRole {

  /** Account holder (owner or legal representative). */
  TITULAR,

  /** Authorized signer (can perform operations but is not the holder). */
  AUTHORIZED
}
