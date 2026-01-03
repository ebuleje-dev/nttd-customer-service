package com.nttd.banking.customer.domain.model.enums;

/**
 * Enumeration of customer statuses.
 *
 * @author NTT Data
 * @version 1.0
 */
public enum CustomerStatus {

  /** Active and operational customer. */
  ACTIVE,

  /** Inactive customer (can be reactivated). */
  INACTIVE,

  /** Blocked customer (requires administrative intervention). */
  BLOCKED
}
