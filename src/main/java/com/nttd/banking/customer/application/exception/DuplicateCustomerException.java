package com.nttd.banking.customer.application.exception;

/**
 * Exception thrown when attempting to create a customer with duplicate email or document.
 *
 * @author NTT Data
 * @version 1.0
 */
public class DuplicateCustomerException extends RuntimeException {

  /**
   * Constructs a new exception with the specified message.
   *
   * @param message error message
   */
  public DuplicateCustomerException(String message) {
    super(message);
  }

  /**
   * Creates an exception for duplicate email.
   *
   * @param email duplicate email
   * @return formatted exception
   */
  public static DuplicateCustomerException byEmail(String email) {
    return new DuplicateCustomerException(
        "Customer already exists with email: " + email);
  }

  /**
   * Creates an exception for duplicate document.
   *
   * @param documentNumber duplicate document number
   * @return formatted exception
   */
  public static DuplicateCustomerException byDocument(String documentNumber) {
    return new DuplicateCustomerException(
        "Customer already exists with document: " + documentNumber);
  }
}
