package dev.erosende.priadapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponseDto {

  @Schema(
      description = "Identifier of the address",
      example = "1")
  private Integer addressId;

  @Schema(
      description = "First line of the address",
      example = "Elmo Street 2")
  private String addressLineOne;

  @Schema(
      description = "Second line of the address",
      example = "4ºD")
  private String addressLineTwo;

  @Schema(
      description = "Postal code",
      example = "1")
  private String postalCode;

  @Schema(
      description = "City",
      example = "1")
  private String city;

  @Schema(
      description = "Province",
      example = "1")
  private String province;

}
