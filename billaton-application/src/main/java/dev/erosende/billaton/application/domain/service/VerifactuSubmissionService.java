package dev.erosende.billaton.application.domain.service;

import dev.erosende.billaton.application.domain.enums.TipoRegistro;
import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import dev.erosende.billaton.application.domain.model.VerifactuSoapResponseDto;
import dev.erosende.billaton.application.domain.ports.secondary.db.VerifactuRepository;
import dev.erosende.billaton.application.domain.ports.secondary.soap.VerifactuSoapPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifactuSubmissionService {

    private final VerifactuRepository verifactuRepository;
    private final VerifactuSoapPort verifactuSoapPort;

    public void processQueue() {
        List<String> issuerNifs = verifactuRepository.findDistinctIssuerNifsWithPendingRecords();
        log.debug("VeriFactu queue: {} issuer(s) with pending records", issuerNifs.size());

        for (String issuerNif : issuerNifs) {
            try {
                processIssuerQueue(issuerNif);
            } catch (Exception e) {
                log.error("Error processing VeriFactu queue for NIF {}: {}", issuerNif, e.getMessage());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void processIssuerQueue(String issuerNif) {
        List<VerifactuRecordDto> records = verifactuRepository.findAndLockPendingByIssuerNif(issuerNif);
        log.info("Processing {} pending VeriFactu records for NIF {}", records.size(), issuerNif);

        for (VerifactuRecordDto record : records) {
            try {
                VerifactuSoapResponseDto response;

                if (TipoRegistro.ANULACION.name().equals(record.getTipoRegistro())) {
                    response = verifactuSoapPort.sendAnulacion(record);
                } else {
                    response = verifactuSoapPort.sendAlta(record);
                }

                if (response.isSuccess()) {
                    verifactuRepository.markAsSent(record.getVerifactuRecordId(), response.getCsv(), null);
                    log.info("VeriFactu record {} sent successfully, CSV: {}", record.getVerifactuRecordId(), response.getCsv());
                } else {
                    verifactuRepository.markAsError(record.getVerifactuRecordId(), response.getErrorMessage());
                    log.warn("VeriFactu record {} rejected: {} - {}", record.getVerifactuRecordId(), response.getErrorCode(), response.getErrorMessage());
                }
            } catch (Exception e) {
                verifactuRepository.markAsError(record.getVerifactuRecordId(), e.getMessage());
                log.error("Failed to send VeriFactu record {}: {}", record.getVerifactuRecordId(), e.getMessage());
            }
        }
    }
}
