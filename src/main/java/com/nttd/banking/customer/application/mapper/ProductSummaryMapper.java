package com.nttd.banking.customer.application.mapper;

import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import com.nttd.banking.customer.model.dto.AccountSummaryDTO;
import com.nttd.banking.customer.model.dto.CardsSummaryDTO;
import com.nttd.banking.customer.model.dto.CreditCardSummaryDTO;
import com.nttd.banking.customer.model.dto.CreditSummaryDTO;
import com.nttd.banking.customer.model.dto.DebitCardSummaryDTO;
import com.nttd.banking.customer.model.dto.ProductSummaryDTO;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting ProductsQueryResponse to ProductSummaryDTO.
 * Handles the conversion between domain events and API DTOs.
 *
 * @author NTT Data
 * @version 1.0
 */
@Component
public class ProductSummaryMapper {

  /**
   * Converts a ProductsQueryResponse to ProductSummaryDTO.
   *
   * @param response the products query response
   * @return the ProductSummaryDTO for the API response
   */
  public ProductSummaryDTO toDto(ProductsQueryResponse response) {
    if (response == null) {
      return null;
    }

    ProductSummaryDTO dto = new ProductSummaryDTO();
    dto.setCustomerId(response.getCustomerId());
    dto.setQueryTimestamp(toOffsetDateTime(response.getTimestamp()));
    dto.setAccounts(mapAccounts(response.getAccounts()));
    dto.setCredits(mapCredits(response.getCredits()));
    dto.setCards(mapCards(response.getCreditCards(), response.getDebitCards()));
    dto.setHasActiveProducts(response.hasActiveProducts());
    dto.setTotalProducts(response.getTotalProducts());

    return dto;
  }

  /**
   * Maps account info list to AccountSummaryDTO list.
   *
   * @param accounts the account info list
   * @return list of AccountSummaryDTO
   */
  private List<AccountSummaryDTO> mapAccounts(
      List<ProductsQueryResponse.AccountInfo> accounts) {
    if (accounts == null) {
      return List.of();
    }

    return accounts.stream()
        .map(this::mapAccount)
        .toList();
  }

  /**
   * Maps a single account info to AccountSummaryDTO.
   *
   * @param account the account info
   * @return AccountSummaryDTO
   */
  private AccountSummaryDTO mapAccount(ProductsQueryResponse.AccountInfo account) {
    AccountSummaryDTO dto = new AccountSummaryDTO();
    dto.setAccountId(account.getAccountId());
    dto.setAccountNumber(account.getAccountNumber());
    dto.setAccountType(mapAccountType(account.getAccountType()));
    dto.setCurrency(mapCurrency(account.getCurrency()));
    dto.setBalance(account.getBalance());
    dto.setStatus(mapAccountStatus(account.getStatus()));
    return dto;
  }

  /**
   * Maps credit info list to CreditSummaryDTO list.
   *
   * @param credits the credit info list
   * @return list of CreditSummaryDTO
   */
  private List<CreditSummaryDTO> mapCredits(
      List<ProductsQueryResponse.CreditInfo> credits) {
    if (credits == null) {
      return List.of();
    }

    return credits.stream()
        .map(this::mapCredit)
        .toList();
  }

  /**
   * Maps a single credit info to CreditSummaryDTO.
   *
   * @param credit the credit info
   * @return CreditSummaryDTO
   */
  private CreditSummaryDTO mapCredit(ProductsQueryResponse.CreditInfo credit) {
    CreditSummaryDTO dto = new CreditSummaryDTO();
    dto.setCreditId(credit.getCreditId());
    dto.setCreditType(mapCreditType(credit.getCreditType()));
    dto.setCreditLimit(credit.getCreditLimit());
    dto.setUsedAmount(credit.getUsedAmount());
    dto.setAvailableAmount(credit.getAvailableAmount());
    dto.setCurrency(mapCreditCurrency(credit.getCurrency()));
    dto.setStatus(mapCreditStatus(credit.getStatus()));
    return dto;
  }

  /**
   * Maps credit cards and debit cards to CardsSummaryDTO.
   *
   * @param creditCards the credit cards list
   * @param debitCards  the debit cards list
   * @return CardsSummaryDTO
   */
  private CardsSummaryDTO mapCards(
      List<ProductsQueryResponse.CreditCardInfo> creditCards,
      List<ProductsQueryResponse.DebitCardInfo> debitCards) {

    CardsSummaryDTO dto = new CardsSummaryDTO();
    dto.setCreditCards(mapCreditCards(creditCards));
    dto.setDebitCards(mapDebitCards(debitCards));
    return dto;
  }

  /**
   * Maps credit card info list to CreditCardSummaryDTO list.
   *
   * @param cards the credit card info list
   * @return list of CreditCardSummaryDTO
   */
  private List<CreditCardSummaryDTO> mapCreditCards(
      List<ProductsQueryResponse.CreditCardInfo> cards) {
    if (cards == null) {
      return List.of();
    }

    return cards.stream()
        .map(this::mapCreditCard)
        .toList();
  }

  /**
   * Maps a single credit card info to CreditCardSummaryDTO.
   *
   * @param card the credit card info
   * @return CreditCardSummaryDTO
   */
  private CreditCardSummaryDTO mapCreditCard(ProductsQueryResponse.CreditCardInfo card) {
    CreditCardSummaryDTO dto = new CreditCardSummaryDTO();
    dto.setCardId(card.getCardId());
    dto.setCardNumber(card.getCardNumber());
    dto.setCardType(mapCreditCardType(card.getCardType()));
    dto.setCreditLimit(card.getCreditLimit());
    dto.setAvailableCredit(card.getAvailableCredit());
    dto.setStatus(mapCardStatus(card.getStatus()));
    return dto;
  }

  /**
   * Maps debit card info list to DebitCardSummaryDTO list.
   *
   * @param cards the debit card info list
   * @return list of DebitCardSummaryDTO
   */
  private List<DebitCardSummaryDTO> mapDebitCards(
      List<ProductsQueryResponse.DebitCardInfo> cards) {
    if (cards == null) {
      return List.of();
    }

    return cards.stream()
        .map(this::mapDebitCard)
        .toList();
  }

  /**
   * Maps a single debit card info to DebitCardSummaryDTO.
   *
   * @param card the debit card info
   * @return DebitCardSummaryDTO
   */
  private DebitCardSummaryDTO mapDebitCard(ProductsQueryResponse.DebitCardInfo card) {
    DebitCardSummaryDTO dto = new DebitCardSummaryDTO();
    dto.setCardId(card.getCardId());
    dto.setCardNumber(card.getCardNumber());
    dto.setCardType(mapDebitCardType(card.getCardType()));
    dto.setLinkedAccountId(card.getLinkedAccountId());
    dto.setStatus(mapDebitCardStatus(card.getStatus()));
    return dto;
  }

  /**
   * Converts Instant to OffsetDateTime.
   *
   * @param instant the Instant to convert
   * @return OffsetDateTime in UTC
   */
  private OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
    return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
  }

  /**
   * Maps account type string to enum.
   */
  private AccountSummaryDTO.AccountTypeEnum mapAccountType(String type) {
    if (type == null) {
      return null;
    }
    try {
      return AccountSummaryDTO.AccountTypeEnum.fromValue(type);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps currency string to enum for accounts.
   */
  private AccountSummaryDTO.CurrencyEnum mapCurrency(String currency) {
    if (currency == null) {
      return null;
    }
    try {
      return AccountSummaryDTO.CurrencyEnum.fromValue(currency);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps account status string to enum.
   */
  private AccountSummaryDTO.StatusEnum mapAccountStatus(String status) {
    if (status == null) {
      return null;
    }
    try {
      return AccountSummaryDTO.StatusEnum.fromValue(status);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps credit type string to enum.
   */
  private CreditSummaryDTO.CreditTypeEnum mapCreditType(String type) {
    if (type == null) {
      return null;
    }
    try {
      return CreditSummaryDTO.CreditTypeEnum.fromValue(type);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps currency string to enum for credits.
   */
  private CreditSummaryDTO.CurrencyEnum mapCreditCurrency(String currency) {
    if (currency == null) {
      return null;
    }
    try {
      return CreditSummaryDTO.CurrencyEnum.fromValue(currency);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps credit status string to enum.
   */
  private CreditSummaryDTO.StatusEnum mapCreditStatus(String status) {
    if (status == null) {
      return null;
    }
    try {
      return CreditSummaryDTO.StatusEnum.fromValue(status);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps credit card type string to enum.
   */
  private CreditCardSummaryDTO.CardTypeEnum mapCreditCardType(String type) {
    if (type == null) {
      return null;
    }
    try {
      return CreditCardSummaryDTO.CardTypeEnum.fromValue(type);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps debit card type string to enum.
   */
  private DebitCardSummaryDTO.CardTypeEnum mapDebitCardType(String type) {
    if (type == null) {
      return null;
    }
    try {
      return DebitCardSummaryDTO.CardTypeEnum.fromValue(type);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps credit card status string to enum.
   */
  private CreditCardSummaryDTO.StatusEnum mapCardStatus(String status) {
    if (status == null) {
      return null;
    }
    try {
      return CreditCardSummaryDTO.StatusEnum.fromValue(status);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Maps debit card status string to enum.
   */
  private DebitCardSummaryDTO.StatusEnum mapDebitCardStatus(String status) {
    if (status == null) {
      return null;
    }
    try {
      return DebitCardSummaryDTO.StatusEnum.fromValue(status);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
