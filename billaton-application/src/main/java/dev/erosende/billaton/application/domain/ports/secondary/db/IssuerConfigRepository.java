package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.IssuerConfigDto;
import java.util.Optional;

public interface IssuerConfigRepository {

  Optional<IssuerConfigDto> findIssuerConfig(Integer participantId);

  Integer updateIssuerConfig(IssuerConfigDto issuerConfig);

}
