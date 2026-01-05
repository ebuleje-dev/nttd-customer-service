package com.nttd.banking.customer.application.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for custom exceptions.
 */
@DisplayName("Custom Exceptions Tests")
class ExceptionsTest {

  @Nested
  @DisplayName("CustomerNotFoundException Tests")
  class CustomerNotFoundExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Customer not found";
      CustomerNotFoundException exception = new CustomerNotFoundException(message);

      assertEquals(message, exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception by ID")
    void shouldCreateExceptionById() {
      String id = "customer-123";
      CustomerNotFoundException exception = CustomerNotFoundException.byId(id);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(id));
      assertTrue(exception.getMessage().contains("Customer not found with id"));
    }

    @Test
    @DisplayName("Should create exception by email")
    void shouldCreateExceptionByEmail() {
      String email = "test@email.com";
      CustomerNotFoundException exception = CustomerNotFoundException.byEmail(email);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(email));
      assertTrue(exception.getMessage().contains("Customer not found with email"));
    }

    @Test
    @DisplayName("Should create exception by document")
    void shouldCreateExceptionByDocument() {
      String document = "12345678";
      CustomerNotFoundException exception = CustomerNotFoundException.byDocument(document);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(document));
      assertTrue(exception.getMessage().contains("Customer not found with document"));
    }
  }

  @Nested
  @DisplayName("DuplicateCustomerException Tests")
  class DuplicateCustomerExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Duplicate customer";
      DuplicateCustomerException exception = new DuplicateCustomerException(message);

      assertEquals(message, exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception by email")
    void shouldCreateExceptionByEmail() {
      String email = "duplicate@email.com";
      DuplicateCustomerException exception = DuplicateCustomerException.byEmail(email);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(email));
      assertTrue(exception.getMessage().contains("email"));
    }

    @Test
    @DisplayName("Should create exception by document")
    void shouldCreateExceptionByDocument() {
      String document = "12345678";
      DuplicateCustomerException exception = DuplicateCustomerException.byDocument(document);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(document));
      assertTrue(exception.getMessage().contains("document"));
    }
  }

  @Nested
  @DisplayName("BusinessValidationException Tests")
  class BusinessValidationExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Business validation failed";
      BusinessValidationException exception = new BusinessValidationException(message);

      assertEquals(message, exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
      String message = "Business validation failed";
      Throwable cause = new IllegalArgumentException("Invalid argument");
      BusinessValidationException exception = new BusinessValidationException(message, cause);

      assertEquals(message, exception.getMessage());
      assertEquals(cause, exception.getCause());
    }
  }

  @Nested
  @DisplayName("ProfileUpdateNotAllowedException Tests")
  class ProfileUpdateNotAllowedExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Profile update not allowed";
      ProfileUpdateNotAllowedException exception =
          new ProfileUpdateNotAllowedException(message);

      assertEquals(message, exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
      String message = "Profile update not allowed";
      Throwable cause = new RuntimeException("Validation failed");
      ProfileUpdateNotAllowedException exception =
          new ProfileUpdateNotAllowedException(message, cause);

      assertEquals(message, exception.getMessage());
      assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should create exception for no credit card")
    void shouldCreateExceptionForNoCreditCard() {
      String customerId = "customer-123";
      String profileType = "VIP";
      ProfileUpdateNotAllowedException exception =
          ProfileUpdateNotAllowedException.noCreditCard(customerId, profileType);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(customerId));
      assertTrue(exception.getMessage().contains(profileType));
      assertTrue(exception.getMessage().contains("credit card"));
    }

    @Test
    @DisplayName("Should create exception for invalid profile for type")
    void shouldCreateExceptionForInvalidProfileForType() {
      String customerId = "customer-123";
      String customerType = "PERSONAL";
      String profileType = "PYME";
      ProfileUpdateNotAllowedException exception =
          ProfileUpdateNotAllowedException.invalidProfileForType(
              customerId, customerType, profileType);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(customerId));
      assertTrue(exception.getMessage().contains(customerType));
      assertTrue(exception.getMessage().contains(profileType));
      assertTrue(exception.getMessage().contains("not valid for"));
    }

    @Test
    @DisplayName("Should create exception for validation timeout")
    void shouldCreateExceptionForValidationTimeout() {
      String customerId = "customer-123";
      ProfileUpdateNotAllowedException exception =
          ProfileUpdateNotAllowedException.validationTimeout(customerId);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(customerId));
      assertTrue(exception.getMessage().contains("timed out"));
    }
  }

  @Nested
  @DisplayName("CustomerHasActiveProductsException Tests")
  class CustomerHasActiveProductsExceptionTests {

    @Test
    @DisplayName("Should create exception with customer ID and product count")
    void shouldCreateExceptionWithProductCount() {
      String customerId = "customer-123";
      int productCount = 5;
      CustomerHasActiveProductsException exception =
          new CustomerHasActiveProductsException(customerId, productCount);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(customerId));
      assertTrue(exception.getMessage().contains(String.valueOf(productCount)));
      assertTrue(exception.getMessage().contains("active product"));
      assertEquals(customerId, exception.getCustomerId());
      assertEquals(productCount, exception.getProductCount());
    }

    @Test
    @DisplayName("Should create exception with customer ID only")
    void shouldCreateExceptionWithCustomerIdOnly() {
      String customerId = "customer-456";
      CustomerHasActiveProductsException exception =
          new CustomerHasActiveProductsException(customerId);

      assertNotNull(exception.getMessage());
      assertTrue(exception.getMessage().contains(customerId));
      assertTrue(exception.getMessage().contains("active products"));
      assertEquals(customerId, exception.getCustomerId());
      assertEquals(-1, exception.getProductCount());
    }

    @Test
    @DisplayName("Should have getters for customer ID and product count")
    void shouldHaveGetters() {
      String customerId = "customer-789";
      int productCount = 3;
      CustomerHasActiveProductsException exception =
          new CustomerHasActiveProductsException(customerId, productCount);

      assertEquals(customerId, exception.getCustomerId());
      assertEquals(productCount, exception.getProductCount());
    }
  }
}
