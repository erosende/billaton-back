package dev.erosende.priadapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdentificationTypeResponseDto {

  @Schema(
      description = "Identifier of the identification type",
      example = "1")
  private Integer identificationTypeId;

  @Schema(
      description = "Name of the identification type",
      example = "DNI")
  private String name;

}
