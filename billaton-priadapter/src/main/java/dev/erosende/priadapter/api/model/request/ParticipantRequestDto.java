package dev.erosende.priadapter.api.model.request;

import dev.erosende.billaton.application.domain.enums.ParticipantType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantRequestDto {

  @NotBlank
  @Schema(
      description = "DNI/NIF/NIE/CIF",
      example = "32456872J")
  private String identificationNumber;

  @NotNull
  @Schema(
      description = "Identification number type",
      example = "DNI")
  private Integer identificationTypeId;

  @NotNull
  @Schema(
      description = "Type of participant, issuer or recipient",
      example = "Issuer")
  private String participantTypeCode;

  @NotBlank
  @Schema(
      description = "Name of the participant",
      example = "Andrea")
  private String name;

  @NotBlank
  @Schema(
      description = "Surnames of the participant",
      example = "Barrera López")
  private String surnames;

  @Schema(
      description = "Email",
      example = "ejar@fake.com")
  private String email;

  @Schema(
      description = "Phone number of the participant",
      example = "6237129665")
  private String phoneNumber;

  @NotNull
  @Schema(
      description = "Address of the participant",
      example = "n/a")
  private AddressRequestDto address;

}
