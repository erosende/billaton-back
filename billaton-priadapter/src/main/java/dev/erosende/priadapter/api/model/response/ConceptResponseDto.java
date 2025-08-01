package dev.erosende.priadapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ConceptResponseDto {

  @Schema(
      description = "Identifier of the concept",
      example = "1")
  private Integer conceptId;

  @Schema(
      description = "Description of the concept",
      example = "Car repair")
  private String description;

  @Schema(
      description = "Amount of units",
      example = "2")
  private Integer amount;

  @Schema(
      description = "Price per unit",
      example = "50.00")
  private Double pricePerUnit;

  @Schema(
      description = "Identifier of the document",
      example = "1")
  private Integer documentId;

}
