package dev.erosende.billaton.application.domain.ports.primary;

import dev.erosende.billaton.application.domain.enums.ParticipantType;
import dev.erosende.billaton.application.domain.exception.InvalidRequestException;
import dev.erosende.billaton.application.domain.exception.ResourceNotFoundException;
import dev.erosende.billaton.application.domain.model.IssuerConfigDto;
import dev.erosende.billaton.application.domain.model.ParticipantDto;
import java.util.List;

public interface ParticipantsUseCase {

  List<ParticipantDto> getParticipants(String userId, ParticipantType participantType, String searchTerm);

  Integer createParticipant(String userId, ParticipantDto participant) throws InvalidRequestException;

  void updateRecipientParticipant(ParticipantDto participant);

  IssuerConfigDto getIssuerConfig(Integer participantId) throws ResourceNotFoundException;

  void updateIssuerConfig(IssuerConfigDto issuerConfig);

  void deleteRecipientParticipant(String userId, Integer participantId) throws ResourceNotFoundException;

}
