package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.ParticipantDto;
import java.util.List;
import java.util.Optional;

public interface ParticipantsRepository {

  List<ParticipantDto> findParticipants(int participantType, String searchTerm);

  Optional<ParticipantDto> findParticipantById(Integer participantId);

  Integer saveParticipant(ParticipantDto participant);

  void updateParticipant(ParticipantDto participant);

  void deleteParticipant(Integer participantId);

}
