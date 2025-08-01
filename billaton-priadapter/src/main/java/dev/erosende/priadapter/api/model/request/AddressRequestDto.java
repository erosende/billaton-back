package dev.erosende.priadapter.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressRequestDto {

  @NotBlank
  @Schema(
      description = "First line of the address",
      example = "Elmo Street 2")
  private String addressLineOne;

  @Schema(
      description = "Second line of the address",
      example = "4ºD")
  private String addressLineTwo;

  @NotBlank
  @Schema(
      description = "Postal code",
      example = "1")
  private String postalCode;

  @NotBlank
  @Schema(
      description = "City",
      example = "1")
  private String city;

  @NotBlank
  @Schema(
      description = "Province",
      example = "1")
  private String province;

}
