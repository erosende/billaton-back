package dev.erosende.billaton.application.domain.ports.primary;

import dev.erosende.billaton.application.domain.model.IdentificationTypeDto;
import java.util.List;

public interface IdentificationTypesUseCase {

  List<IdentificationTypeDto> getIdentificationTypes();

}
