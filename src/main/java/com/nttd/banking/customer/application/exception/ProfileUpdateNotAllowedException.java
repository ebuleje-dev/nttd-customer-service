package com.nttd.banking.customer.application.exception;

/**
 * Exception thrown when a profile update is not allowed.
 * This typically happens when a customer doesn't meet the requirements
 * for a VIP or PYME profile (e.g., no active credit card).
 *
 * @author NTT Data
 * @version 1.0
 */
public class ProfileUpdateNotAllowedException extends BusinessValidationException {

  /**
   * Constructs a new exception with the specified message.
   *
   * @param message error message
   */
  public ProfileUpdateNotAllowedException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified message and cause.
   *
   * @param message error message
   * @param cause cause of the exception
   */
  public ProfileUpdateNotAllowedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates an exception for missing credit card requirement.
   *
   * @param customerId the customer ID
   * @param profileType the requested profile type (VIP or PYME)
   * @return a new ProfileUpdateNotAllowedException
   */
  public static ProfileUpdateNotAllowedException noCreditCard(
      String customerId, String profileType) {
    return new ProfileUpdateNotAllowedException(
        String.format(
            "Cannot update customer %s to %s profile: "
                + "customer does not have an active credit card",
            customerId, profileType));
  }

  /**
   * Creates an exception for invalid profile type for customer type.
   *
   * @param customerId the customer ID
   * @param customerType the customer type (PERSONAL or BUSINESS)
   * @param profileType the requested profile type
   * @return a new ProfileUpdateNotAllowedException
   */
  public static ProfileUpdateNotAllowedException invalidProfileForType(
      String customerId, String customerType, String profileType) {
    return new ProfileUpdateNotAllowedException(
        String.format(
            "Cannot update customer %s to %s profile: "
                + "profile not valid for %s customer type",
            customerId, profileType, customerType));
  }

  /**
   * Creates an exception for validation timeout.
   *
   * @param customerId the customer ID
   * @return a new ProfileUpdateNotAllowedException
   */
  public static ProfileUpdateNotAllowedException validationTimeout(String customerId) {
    return new ProfileUpdateNotAllowedException(
        String.format(
            "Cannot update customer %s profile: "
                + "credit card validation timed out. Please try again later.",
            customerId));
  }
}
