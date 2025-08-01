package dev.erosende.priadapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IssuerConfigResponseDto {

  @Schema(
      description = "Identifier of the config",
      example = "1")
  private Integer issuerConfigId;

  @Schema(
      description = "Identifier of issuer that owns the config",
      example = "1")
  private Integer issuerId;

  @Schema(
      description = "VAT",
      example = "21")
  private Integer vat;

  @Schema(
      description = "Bank account number where the recipient has to pay",
      example = "ES74 8429 9428 4595 5493")
  private String paymentAccountNumber;

  @Schema(
      description = "Issuer's logo path",
      example = "https://placehold.co/600x400")
  private String logoPath;

}
