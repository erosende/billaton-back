package dev.erosende.priadapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantResponseDto {

  @Schema(
      description = "Identifier of the participant",
      example = "1")
  private Integer participantId;

  @Schema(
      description = "DNI/NIF/NIE/CIF",
      example = "32456872J")
  private String identificationNumber;

  @Schema(
      description = "Identification number type",
      example = "DNI")
  private String identificationType;

  @Schema(
      description = "Identifier for the identification number type",
      example = "DNI")
  private String identificationTypeId;

  @Schema(
      description = "Name of the participant",
      example = "Andrea")
  private String name;

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

  @Schema(
      description = "Type of participant, issuer or recipient",
      example = "Issuer")
  private String participantType;

  @Schema(
      description = "Address of the participant",
      example = "n/a")
  private AddressResponseDto address;

}
