package com.nttd.banking.customer.application.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.nttd.banking.customer.domain.model.AuthorizedSigner;
import com.nttd.banking.customer.domain.model.BusinessCustomer;
import com.nttd.banking.customer.domain.model.Customer;
import com.nttd.banking.customer.domain.model.PersonalCustomer;
import com.nttd.banking.customer.domain.model.enums.BusinessProfile;
import com.nttd.banking.customer.domain.model.enums.BusinessType;
import com.nttd.banking.customer.domain.model.enums.CustomerStatus;
import com.nttd.banking.customer.domain.model.enums.CustomerType;
import com.nttd.banking.customer.domain.model.enums.DocumentType;
import com.nttd.banking.customer.domain.model.enums.Gender;
import com.nttd.banking.customer.domain.model.enums.PersonalProfile;
import com.nttd.banking.customer.domain.model.enums.SignerRole;
import com.nttd.banking.customer.model.dto.AuthorizedSignerDTO;
import com.nttd.banking.customer.model.dto.CustomerRequestDTO;
import com.nttd.banking.customer.model.dto.CustomerResponseDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CustomerMapper.
 */
@DisplayName("CustomerMapper Tests")
class CustomerMapperTest {

  private CustomerMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new CustomerMapper();
  }

  @Nested
  @DisplayName("toDomain - PersonalCustomer Tests")
  class ToDomainPersonalTests {

    @Test
    @DisplayName("Should map PersonalCustomer request DTO to domain")
    void shouldMapPersonalCustomerRequestToDomain() {
      CustomerRequestDTO dto = new CustomerRequestDTO();
      dto.setCustomerType(CustomerRequestDTO.CustomerTypeEnum.PERSONAL);
      dto.setDocumentType(CustomerRequestDTO.DocumentTypeEnum.DNI);
      dto.setDocumentNumber("12345678");
      dto.setEmail("test@email.com");
      dto.setPhoneNumber("+51987654321");
      dto.setAddress("Av. Test 123, Lima");
      dto.setFirstName("John");
      dto.setLastName("Doe");
      dto.setDateOfBirth(OffsetDateTime.now().minusYears(25));
      dto.setGender(CustomerRequestDTO.GenderEnum.MALE);
      dto.setPersonalProfile(CustomerRequestDTO.PersonalProfileEnum.STANDARD);

      Customer customer = mapper.toDomain(dto);

      assertNotNull(customer);
      assertInstanceOf(PersonalCustomer.class, customer);
      PersonalCustomer personalCustomer = (PersonalCustomer) customer;
      assertEquals("John", personalCustomer.getFirstName());
      assertEquals("Doe", personalCustomer.getLastName());
      assertEquals(DocumentType.DNI, personalCustomer.getDocumentType());
      assertEquals("test@email.com", personalCustomer.getEmail());
      assertEquals(Gender.MALE, personalCustomer.getGender());
      assertEquals(PersonalProfile.STANDARD, personalCustomer.getPersonalProfile());
    }

    @Test
    @DisplayName("Should set default STANDARD profile when not provided")
    void shouldSetDefaultProfileForPersonal() {
      CustomerRequestDTO dto = new CustomerRequestDTO();
      dto.setCustomerType(CustomerRequestDTO.CustomerTypeEnum.PERSONAL);
      dto.setDocumentType(CustomerRequestDTO.DocumentTypeEnum.DNI);
      dto.setDocumentNumber("12345678");
      dto.setEmail("test@email.com");
      dto.setPhoneNumber("+51987654321");
      dto.setAddress("Av. Test 123, Lima");
      dto.setFirstName("John");
      dto.setLastName("Doe");
      dto.setDateOfBirth(OffsetDateTime.now().minusYears(25));
      dto.setPersonalProfile(null);

      Customer customer = mapper.toDomain(dto);

      assertInstanceOf(PersonalCustomer.class, customer);
      PersonalCustomer personalCustomer = (PersonalCustomer) customer;
      assertEquals(PersonalProfile.STANDARD, personalCustomer.getPersonalProfile());
    }

    @Test
    @DisplayName("Should return null when DTO is null")
    void shouldReturnNullWhenDtoIsNull() {
      Customer customer = mapper.toDomain(null);
      assertNull(customer);
    }
  }

  @Nested
  @DisplayName("toDomain - BusinessCustomer Tests")
  class ToDomainBusinessTests {

    @Test
    @DisplayName("Should map BusinessCustomer request DTO to domain")
    void shouldMapBusinessCustomerRequestToDomain() {
      AuthorizedSignerDTO signerDto = new AuthorizedSignerDTO();
      signerDto.setFirstName("Carlos");
      signerDto.setLastName("Garcia");
      signerDto.setDocumentType(AuthorizedSignerDTO.DocumentTypeEnum.DNI);
      signerDto.setDocumentNumber("87654321");
      signerDto.setRole(AuthorizedSignerDTO.RoleEnum.TITULAR);

      CustomerRequestDTO dto = new CustomerRequestDTO();
      dto.setCustomerType(CustomerRequestDTO.CustomerTypeEnum.BUSINESS);
      dto.setDocumentType(CustomerRequestDTO.DocumentTypeEnum.RUC);
      dto.setDocumentNumber("20123456789");
      dto.setEmail("empresa@company.com");
      dto.setPhoneNumber("+51987654321");
      dto.setAddress("Av. Principal 456, Lima");
      dto.setBusinessName("Empresa SAC");
      dto.setBusinessType(CustomerRequestDTO.BusinessTypeEnum.SAC);
      dto.setTaxId("20123456789");
      dto.setBusinessProfile(CustomerRequestDTO.BusinessProfileEnum.STANDARD);
      dto.setAuthorizedSigners(List.of(signerDto));

      Customer customer = mapper.toDomain(dto);

      assertNotNull(customer);
      assertInstanceOf(BusinessCustomer.class, customer);
      BusinessCustomer businessCustomer = (BusinessCustomer) customer;
      assertEquals("Empresa SAC", businessCustomer.getBusinessName());
      assertEquals(BusinessType.SAC, businessCustomer.getBusinessType());
      assertEquals("20123456789", businessCustomer.getTaxId());
      assertEquals(BusinessProfile.STANDARD, businessCustomer.getBusinessProfile());
      assertEquals(1, businessCustomer.getAuthorizedSigners().size());
      assertEquals("Carlos", businessCustomer.getAuthorizedSigners().get(0).getFirstName());
    }

    @Test
    @DisplayName("Should set empty list when authorizedSigners is null")
    void shouldSetEmptyListWhenSignersIsNull() {
      CustomerRequestDTO dto = new CustomerRequestDTO();
      dto.setCustomerType(CustomerRequestDTO.CustomerTypeEnum.BUSINESS);
      dto.setDocumentType(CustomerRequestDTO.DocumentTypeEnum.RUC);
      dto.setDocumentNumber("20123456789");
      dto.setEmail("empresa@company.com");
      dto.setPhoneNumber("+51987654321");
      dto.setAddress("Av. Principal 456, Lima");
      dto.setBusinessName("Empresa SAC");
      dto.setTaxId("20123456789");
      dto.setAuthorizedSigners(null);

      Customer customer = mapper.toDomain(dto);

      assertInstanceOf(BusinessCustomer.class, customer);
      BusinessCustomer businessCustomer = (BusinessCustomer) customer;
      assertNotNull(businessCustomer.getAuthorizedSigners());
      assertTrue(businessCustomer.getAuthorizedSigners().isEmpty());
    }
  }

  @Nested
  @DisplayName("toResponseDto Tests")
  class ToResponseDtoTests {

    @Test
    @DisplayName("Should map PersonalCustomer domain to response DTO")
    void shouldMapPersonalCustomerToResponseDto() {
      Instant now = Instant.now();
      Instant birthDate = LocalDate.of(1990, 1, 1)
          .atStartOfDay(ZoneId.systemDefault()).toInstant();

      PersonalCustomer customer = PersonalCustomer.builder()
          .id("123")
          .customerType(CustomerType.PERSONAL)
          .documentType(DocumentType.DNI)
          .documentNumber("12345678")
          .email("test@email.com")
          .phoneNumber("+51987654321")
          .address("Av. Test 123")
          .status(CustomerStatus.ACTIVE)
          .createdAt(now)
          .updatedAt(now)
          .firstName("John")
          .lastName("Doe")
          .dateOfBirth(birthDate)
          .gender(Gender.MALE)
          .personalProfile(PersonalProfile.VIP)
          .build();

      CustomerResponseDTO dto = mapper.toResponseDto(customer);

      assertNotNull(dto);
      assertEquals("123", dto.getId());
      assertEquals("John", dto.getFirstName());
      assertEquals("Doe", dto.getLastName());
      assertEquals("test@email.com", dto.getEmail());
      assertEquals(CustomerResponseDTO.CustomerTypeEnum.PERSONAL, dto.getCustomerType());
      assertEquals(CustomerResponseDTO.GenderEnum.MALE, dto.getGender());
      assertEquals(CustomerResponseDTO.PersonalProfileEnum.VIP, dto.getPersonalProfile());
    }

    @Test
    @DisplayName("Should map BusinessCustomer domain to response DTO")
    void shouldMapBusinessCustomerToResponseDto() {
      Instant now = Instant.now();
      AuthorizedSigner signer = AuthorizedSigner.builder()
          .firstName("Carlos")
          .lastName("Garcia")
          .documentType(DocumentType.DNI)
          .documentNumber("87654321")
          .role(SignerRole.TITULAR)
          .build();

      List<AuthorizedSigner> signers = new ArrayList<>();
      signers.add(signer);

      BusinessCustomer customer = BusinessCustomer.builder()
          .id("456")
          .customerType(CustomerType.BUSINESS)
          .documentType(DocumentType.RUC)
          .documentNumber("20123456789")
          .email("empresa@company.com")
          .phoneNumber("+51987654321")
          .address("Av. Principal 456")
          .status(CustomerStatus.ACTIVE)
          .createdAt(now)
          .updatedAt(now)
          .businessName("Empresa SAC")
          .businessType(BusinessType.SAC)
          .taxId("20123456789")
          .businessProfile(BusinessProfile.PYME)
          .authorizedSigners(signers)
          .build();

      CustomerResponseDTO dto = mapper.toResponseDto(customer);

      assertNotNull(dto);
      assertEquals("456", dto.getId());
      assertEquals("Empresa SAC", dto.getBusinessName());
      assertEquals(CustomerResponseDTO.BusinessTypeEnum.SAC, dto.getBusinessType());
      assertEquals("20123456789", dto.getTaxId());
      assertEquals(CustomerResponseDTO.BusinessProfileEnum.PYME, dto.getBusinessProfile());
      assertNotNull(dto.getAuthorizedSigners());
      assertEquals(1, dto.getAuthorizedSigners().size());
      assertEquals("Carlos", dto.getAuthorizedSigners().get(0).getFirstName());
    }

    @Test
    @DisplayName("Should return null when customer is null")
    void shouldReturnNullWhenCustomerIsNull() {
      CustomerResponseDTO dto = mapper.toResponseDto(null);
      assertNull(dto);
    }

    @Test
    @DisplayName("Should handle null optional fields in PersonalCustomer")
    void shouldHandleNullOptionalFieldsInPersonal() {
      PersonalCustomer customer = PersonalCustomer.builder()
          .id("123")
          .customerType(CustomerType.PERSONAL)
          .documentType(DocumentType.DNI)
          .documentNumber("12345678")
          .email("test@email.com")
          .phoneNumber("+51987654321")
          .address("Av. Test 123")
          .status(CustomerStatus.ACTIVE)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .firstName("John")
          .lastName("Doe")
          .dateOfBirth(Instant.now().minusSeconds(31536000 * 25))
          .gender(null)
          .personalProfile(null)
          .build();

      CustomerResponseDTO dto = mapper.toResponseDto(customer);

      assertNotNull(dto);
      assertNull(dto.getGender());
      assertNull(dto.getPersonalProfile());
    }

    @Test
    @DisplayName("Should handle null optional fields in BusinessCustomer")
    void shouldHandleNullOptionalFieldsInBusiness() {
      BusinessCustomer customer = BusinessCustomer.builder()
          .id("456")
          .customerType(CustomerType.BUSINESS)
          .documentType(DocumentType.RUC)
          .documentNumber("20123456789")
          .email("empresa@company.com")
          .phoneNumber("+51987654321")
          .address("Av. Principal 456")
          .status(CustomerStatus.ACTIVE)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .businessName("Empresa SAC")
          .businessType(null)
          .taxId("20123456789")
          .businessProfile(null)
          .authorizedSigners(null)
          .build();

      CustomerResponseDTO dto = mapper.toResponseDto(customer);

      assertNotNull(dto);
      assertNull(dto.getBusinessType());
      assertNull(dto.getBusinessProfile());
      assertNotNull(dto.getAuthorizedSigners());
      assertTrue(dto.getAuthorizedSigners().isEmpty());
    }
  }

  @Nested
  @DisplayName("Enum Mapping Tests")
  class EnumMappingTests {

    @Test
    @DisplayName("Should map all DocumentType enums")
    void shouldMapAllDocumentTypes() {
      CustomerRequestDTO dto = new CustomerRequestDTO();
      dto.setCustomerType(CustomerRequestDTO.CustomerTypeEnum.PERSONAL);
      dto.setDocumentNumber("12345678");
      dto.setEmail("test@email.com");
      dto.setPhoneNumber("+51987654321");
      dto.setAddress("Av. Test 123");
      dto.setFirstName("John");
      dto.setLastName("Doe");
      dto.setDateOfBirth(OffsetDateTime.now().minusYears(25));

      // Test DNI
      dto.setDocumentType(CustomerRequestDTO.DocumentTypeEnum.DNI);
      Customer customer1 = mapper.toDomain(dto);
      assertEquals(DocumentType.DNI, customer1.getDocumentType());

      // Test CEX
      dto.setDocumentType(CustomerRequestDTO.DocumentTypeEnum.CEX);
      Customer customer2 = mapper.toDomain(dto);
      assertEquals(DocumentType.CEX, customer2.getDocumentType());

      // Test PASSPORT
      dto.setDocumentType(CustomerRequestDTO.DocumentTypeEnum.PASSPORT);
      Customer customer3 = mapper.toDomain(dto);
      assertEquals(DocumentType.PASSPORT, customer3.getDocumentType());
    }

    @Test
    @DisplayName("Should map all Gender enums")
    void shouldMapAllGenderEnums() {
      CustomerRequestDTO dto = new CustomerRequestDTO();
      dto.setCustomerType(CustomerRequestDTO.CustomerTypeEnum.PERSONAL);
      dto.setDocumentType(CustomerRequestDTO.DocumentTypeEnum.DNI);
      dto.setDocumentNumber("12345678");
      dto.setEmail("test@email.com");
      dto.setPhoneNumber("+51987654321");
      dto.setAddress("Av. Test 123");
      dto.setFirstName("John");
      dto.setLastName("Doe");
      dto.setDateOfBirth(OffsetDateTime.now().minusYears(25));

      // Test MALE
      dto.setGender(CustomerRequestDTO.GenderEnum.MALE);
      PersonalCustomer customer1 = (PersonalCustomer) mapper.toDomain(dto);
      assertEquals(Gender.MALE, customer1.getGender());

      // Test FEMALE
      dto.setGender(CustomerRequestDTO.GenderEnum.FEMALE);
      PersonalCustomer customer2 = (PersonalCustomer) mapper.toDomain(dto);
      assertEquals(Gender.FEMALE, customer2.getGender());

      // Test OTHER
      dto.setGender(CustomerRequestDTO.GenderEnum.OTHER);
      PersonalCustomer customer3 = (PersonalCustomer) mapper.toDomain(dto);
      assertEquals(Gender.OTHER, customer3.getGender());
    }

    @Test
    @DisplayName("Should map all CustomerStatus enums")
    void shouldMapAllCustomerStatusEnums() {
      PersonalCustomer activeCustomer = PersonalCustomer.builder()
          .id("1")
          .customerType(CustomerType.PERSONAL)
          .documentType(DocumentType.DNI)
          .documentNumber("12345678")
          .email("test@email.com")
          .phoneNumber("+51987654321")
          .address("Av. Test 123")
          .status(CustomerStatus.ACTIVE)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .firstName("John")
          .lastName("Doe")
          .dateOfBirth(Instant.now().minusSeconds(31536000 * 25))
          .build();

      CustomerResponseDTO dto = mapper.toResponseDto(activeCustomer);
      assertEquals(CustomerResponseDTO.StatusEnum.ACTIVE, dto.getStatus());
    }
  }
}
