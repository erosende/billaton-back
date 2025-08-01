package dev.erosende.billaton.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDto {

  private Integer participantId;
  private String identificationNumber;

  private String identificationType;
  private Integer identificationTypeId;

  private String participantType;
  private Integer participantTypeId;

  private String name;
  private String surnames;
  private String email;
  private String phoneNumber;
  private AddressDto address;

}
