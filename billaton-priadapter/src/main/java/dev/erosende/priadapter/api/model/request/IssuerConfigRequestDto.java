package dev.erosende.priadapter.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IssuerConfigRequestDto {

  @NotNull
  @Schema(
      description = "Vat",
      example = "21")
  private Integer vat;

  @NotNull
  @Schema(
      description = "Bank account number where the recipient has to pay",
      example = "ES74 8429 9428 4595 5493")
  private String paymentAccountNumber;

}
