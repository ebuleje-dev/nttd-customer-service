package com.nttd.banking.customer.application.exception;

/**
 * Exception thrown when a customer is not found.
 *
 * @author NTT Data
 * @version 1.0
 */
public class CustomerNotFoundException extends RuntimeException {

  /**
   * Constructs a new exception with the specified message.
   *
   * @param message error message
   */
  public CustomerNotFoundException(String message) {
    super(message);
  }

  /**
   * Creates an exception for customer not found by ID.
   *
   * @param id customer ID not found
   * @return formatted exception
   */
  public static CustomerNotFoundException byId(String id) {
    return new CustomerNotFoundException("Customer not found with id: " + id);
  }

  /**
   * Creates an exception for customer not found by email.
   *
   * @param email customer email not found
   * @return formatted exception
   */
  public static CustomerNotFoundException byEmail(String email) {
    return new CustomerNotFoundException("Customer not found with email: " + email);
  }

  /**
   * Creates an exception for customer not found by document.
   *
   * @param documentNumber customer document not found
   * @return formatted exception
   */
  public static CustomerNotFoundException byDocument(String documentNumber) {
    return new CustomerNotFoundException("Customer not found with document: " + documentNumber);
  }
}
