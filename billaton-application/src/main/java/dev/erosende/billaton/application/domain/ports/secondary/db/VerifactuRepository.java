package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;

import java.util.List;
import java.util.Optional;

public interface VerifactuRepository {

    Integer save(VerifactuRecordDto record);

    Optional<VerifactuRecordDto> findByDocumentId(Integer documentId);

    Optional<String> findLastHuellaByIssuerNif(String issuerNif);

    List<String> findDistinctIssuerNifsWithPendingRecords();

    List<VerifactuRecordDto> findAndLockPendingByIssuerNif(String issuerNif);

    void markAsSent(Integer verifactuRecordId, String csv, String xmlEnviado);

    void markAsError(Integer verifactuRecordId, String errorMessage);

    void markAsCancelled(Integer verifactuRecordId);

    void resetForRetry(Integer verifactuRecordId);
}
