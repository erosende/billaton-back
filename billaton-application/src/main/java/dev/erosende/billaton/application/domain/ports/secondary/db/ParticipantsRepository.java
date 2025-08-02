package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.ParticipantDto;
import java.util.List;
import java.util.Optional;

public interface ParticipantsRepository {

  List<ParticipantDto> findParticipants(String userId, int participantType, String searchTerm);

  Optional<ParticipantDto> findParticipantById(Integer participantId);

  Integer saveParticipant(String userId, ParticipantDto participant);

  void updateParticipant(ParticipantDto participant);

  void deleteParticipant(String userId, Integer participantId);

  void softDeleteParticipant(String userId, Integer participantId);

}
