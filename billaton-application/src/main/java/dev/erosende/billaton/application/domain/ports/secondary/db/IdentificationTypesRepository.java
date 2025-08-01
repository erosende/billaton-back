package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.IdentificationTypeDto;
import java.util.List;

public interface IdentificationTypesRepository {

  List<IdentificationTypeDto> findAllIdentificationTypes();

}
