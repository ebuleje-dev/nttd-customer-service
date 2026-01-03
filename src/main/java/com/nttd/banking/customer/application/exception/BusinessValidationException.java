package com.nttd.banking.customer.application.exception;

/**
 * Exception thrown when a business rule validation fails.
 *
 * @author NTT Data
 * @version 1.0
 */
public class BusinessValidationException extends RuntimeException {

  /**
   * Constructs a new exception with the specified message.
   *
   * @param message error message
   */
  public BusinessValidationException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified message and cause.
   *
   * @param message error message
   * @param cause cause of the exception
   */
  public BusinessValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
