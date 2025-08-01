package dev.erosende.billaton.application.domain.enums;

import dev.erosende.billaton.application.domain.exception.InvalidRequestException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum ParticipantType {

  ISSUER("ISSUER", 1),
  RECIPIENT("RECIPIENT", 2);

  private final String code;
  private final int value;

  private static final Map<String, ParticipantType> MAP = new HashMap<>();

  static {
    for (ParticipantType participantType : ParticipantType.values()) {
      MAP.put(participantType.getCode(), participantType);
    }
  }

  ParticipantType(String code, int value) {
    this.code = code;
    this.value = value;
  }

  public static ParticipantType getByCode(String code) throws InvalidRequestException {
    ParticipantType participantType = MAP.get(code);
    if (participantType == null) {
      throw new InvalidRequestException(String.format(
          "No participant type with code: %s. Valid codes are ISSUER or RECIPIENT",
          code
      ));
    }
    return participantType;
  }

}
