package dev.erosende.billaton.application.domain.usecase;

import dev.erosende.billaton.application.domain.enums.ParticipantType;
import dev.erosende.billaton.application.domain.exception.ResourceNotFoundException;
import dev.erosende.billaton.application.domain.model.IssuerConfigDto;
import dev.erosende.billaton.application.domain.model.ParticipantDto;
import dev.erosende.billaton.application.domain.ports.primary.ParticipantsUseCase;
import dev.erosende.billaton.application.domain.ports.secondary.db.AddressRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.DocumentsRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.IssuerConfigRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.ParticipantsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipantsUseCaseImpl implements ParticipantsUseCase {

  private final AddressRepository addressRepository;
  private final DocumentsRepository documentsRepository;
  private final ParticipantsRepository participantsRepository;
  private final IssuerConfigRepository issuerConfigRepository;

  @Override
  public List<ParticipantDto> getParticipants(String userId, ParticipantType participantType, String searchTerm) {
    return participantsRepository.findParticipants(userId, participantType.getValue(), searchTerm);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Integer createRecipientParticipant(String userId, ParticipantDto participant) {
    Integer addressId = addressRepository.saveAddress(participant.getAddress());
    participant.getAddress().setAddressId(addressId);
    participant.setParticipantTypeId(ParticipantType.RECIPIENT.getValue());

    return participantsRepository.saveParticipant(userId, participant);
  }

  @Override
  public IssuerConfigDto getIssuerConfig(Integer participantId) throws ResourceNotFoundException {
    return issuerConfigRepository.findIssuerConfig(participantId)
        .orElseThrow(() -> new ResourceNotFoundException("IssuerConfig", "participantId", participantId));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateIssuerConfig(IssuerConfigDto issuerConfig) {
    issuerConfigRepository.updateIssuerConfig(issuerConfig);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateRecipientParticipant(ParticipantDto participant) {
    if (participant.getAddress() != null) {
      addressRepository.updateAddress(participant.getAddress());
    }

    participantsRepository.updateParticipant(participant);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteRecipientParticipant(String userId, Integer participantId) throws ResourceNotFoundException {
    int historifiedCount = documentsRepository.softDeleteDocumentByRecipient(userId, participantId);
    if (historifiedCount == 0) {
      ParticipantDto recipient = participantsRepository.findParticipantById(participantId)
          .orElseThrow(() -> new ResourceNotFoundException("Recipient", "participantId", participantId));
      participantsRepository.deleteParticipant(userId, participantId);
      addressRepository.deleteAddress(recipient.getAddress().getAddressId());
    } else {
      participantsRepository.softDeleteParticipant(userId, participantId);
    }
  }

}
