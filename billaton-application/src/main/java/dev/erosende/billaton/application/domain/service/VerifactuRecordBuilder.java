package dev.erosende.billaton.application.domain.service;

import dev.erosende.billaton.application.domain.enums.TipoRegistro;
import dev.erosende.billaton.application.domain.enums.VerifactuStatus;
import dev.erosende.billaton.application.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerifactuRecordBuilder {

    private static final ZoneId SPAIN_ZONE = ZoneId.of("Europe/Madrid");

    public VerifactuRecordDto buildAlta(DocumentDto document, ParticipantDto issuer,
                                         IssuerConfigDto issuerConfig, List<ConceptDto> concepts,
                                         String userId) {
        BigDecimal subtotal = calculateSubtotal(concepts);
        BigDecimal vatRate = BigDecimal.valueOf(issuerConfig.getVat()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal cuotaTotal = subtotal.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal importeTotal = subtotal.add(cuotaTotal).setScale(2, RoundingMode.HALF_UP);

        return VerifactuRecordDto.builder()
                .documentId(document.getDocumentId())
                .issuerNif(issuer.getIdentificationNumber())
                .issuerName(buildFullName(issuer))
                .tipoRegistro(TipoRegistro.ALTA.name())
                .numSerieFactura(document.getDocumentCode())
                .fechaExpedicion(document.getDocumentDate())
                .tipoFactura(document.getTipoFactura())
                .importeTotal(importeTotal)
                .cuotaTotal(cuotaTotal)
                .baseImponible(subtotal.setScale(2, RoundingMode.HALF_UP))
                .cuotaRepercutida(cuotaTotal)
                .vatPercentage(issuerConfig.getVat())
                .claveRegimen(issuerConfig.getClaveRegimen() != null ? issuerConfig.getClaveRegimen() : "01")
                .descripcionOperacion(document.getDescripcionOperacion())
                .tipoRectificativa(document.getTipoRectificativa())
                .fechaHoraGenRegistro(OffsetDateTime.now(SPAIN_ZONE))
                .status(VerifactuStatus.PENDING.name())
                .userId(userId)
                .build();
    }

    public VerifactuRecordDto buildAnulacion(DocumentDto document, ParticipantDto issuer, String userId) {
        return VerifactuRecordDto.builder()
                .documentId(document.getDocumentId())
                .issuerNif(issuer.getIdentificationNumber())
                .issuerName(buildFullName(issuer))
                .tipoRegistro(TipoRegistro.ANULACION.name())
                .numSerieFactura(document.getDocumentCode())
                .fechaExpedicion(document.getDocumentDate())
                .tipoFactura(document.getTipoFactura())
                .importeTotal(BigDecimal.ZERO)
                .cuotaTotal(BigDecimal.ZERO)
                .fechaHoraGenRegistro(OffsetDateTime.now(SPAIN_ZONE))
                .status(VerifactuStatus.PENDING.name())
                .userId(userId)
                .build();
    }

    private BigDecimal calculateSubtotal(List<ConceptDto> concepts) {
        return Optional.ofNullable(concepts).orElse(Collections.emptyList()).stream()
                .map(c -> BigDecimal.valueOf(c.getAmount()).multiply(BigDecimal.valueOf(c.getPricePerUnit())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String buildFullName(ParticipantDto p) {
        String name = p.getName() != null ? p.getName().trim() : "";
        String surnames = p.getSurnames() != null ? p.getSurnames().trim() : "";
        return (name + " " + surnames).trim();
    }
}
