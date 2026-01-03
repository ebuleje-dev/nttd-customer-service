package com.nttd.banking.customer.infrastructure.adapter.out.persistence;

import com.nttd.banking.customer.domain.model.enums.BusinessProfile;
import com.nttd.banking.customer.domain.model.enums.BusinessType;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB entity for business customers.
 *
 * @author NTT Data
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "customers")
public class BusinessCustomerEntity extends CustomerEntity {

  /** Company's business name. */
  private String businessName;

  /** Company type. */
  private BusinessType businessType;

  /** Company's tax ID (RUC). */
  private String taxId;

  /** Business customer profile. */
  private BusinessProfile businessProfile;

  /** List of authorized signers (embedded documents). */
  private List<AuthorizedSignerEntity> authorizedSigners;
}
