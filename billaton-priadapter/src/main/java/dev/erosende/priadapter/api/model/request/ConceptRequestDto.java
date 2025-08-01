package dev.erosende.priadapter.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConceptRequestDto {

  @NotBlank
  @Schema(
      description = "Description of the concept",
      example = "Car repair")
  private String description;

  @NotNull
  @Schema(
      description = "Amount of units",
      example = "2")
  private Integer amount;

  @NotNull
  @Schema(
      description = "Price per unit",
      example = "50.00")
  private Double pricePerUnit;

}
