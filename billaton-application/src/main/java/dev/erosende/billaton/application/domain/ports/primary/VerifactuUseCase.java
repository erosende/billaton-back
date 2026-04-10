package dev.erosende.billaton.application.domain.ports.primary;

import dev.erosende.billaton.application.domain.exception.ResourceNotFoundException;
import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;

import java.util.Optional;

public interface VerifactuUseCase {

    Optional<VerifactuRecordDto> getStatus(Integer documentId);

    void retrySubmission(Integer documentId) throws ResourceNotFoundException;
}
