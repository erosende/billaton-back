package dev.erosende.billaton.application.domain.usecase;

import dev.erosende.billaton.application.domain.model.IdentificationTypeDto;
import dev.erosende.billaton.application.domain.ports.primary.IdentificationTypesUseCase;
import dev.erosende.billaton.application.domain.ports.secondary.db.IdentificationTypesRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentificationTypesUseCaseImpl implements IdentificationTypesUseCase {

  private final IdentificationTypesRepository identificationTypesRepository;

  @Override
  public List<IdentificationTypeDto> getIdentificationTypes() {
    return identificationTypesRepository.findAllIdentificationTypes();
  }

}
