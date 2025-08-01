package dev.erosende.billaton.application.domain.model;

import lombok.Data;

@Data
public class IssuerConfigDto {

  private Integer issuerConfigId;
  private Integer issuerId;
  private Integer vat;
  private String paymentAccountNumber;
  private String logoPath;

}
