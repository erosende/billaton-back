package dev.erosende.billaton.application.domain.usecase;

import dev.erosende.billaton.application.domain.enums.VerifactuStatus;
import dev.erosende.billaton.application.domain.exception.ResourceNotFoundException;
import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import dev.erosende.billaton.application.domain.ports.primary.VerifactuUseCase;
import dev.erosende.billaton.application.domain.ports.secondary.db.VerifactuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerifactuUseCaseImpl implements VerifactuUseCase {

    private final VerifactuRepository verifactuRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<VerifactuRecordDto> getStatus(Integer documentId) {
        return verifactuRepository.findByDocumentId(documentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retrySubmission(Integer documentId) throws ResourceNotFoundException {
        VerifactuRecordDto record = verifactuRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("VerifactuRecord", "documentId", documentId));

        if (!VerifactuStatus.ERROR.name().equals(record.getStatus())) {
            throw new IllegalStateException("Can only retry records in ERROR status, current: " + record.getStatus());
        }

        verifactuRepository.resetForRetry(record.getVerifactuRecordId());
    }
}
