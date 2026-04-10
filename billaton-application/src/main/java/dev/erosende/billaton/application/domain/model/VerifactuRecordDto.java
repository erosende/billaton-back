package dev.erosende.billaton.application.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class VerifactuRecordDto {

    private Integer verifactuRecordId;
    private Integer documentId;
    private String issuerNif;
    private String issuerName;
    private String tipoRegistro;
    private String numSerieFactura;
    private LocalDate fechaExpedicion;
    private String tipoFactura;
    private BigDecimal importeTotal;
    private BigDecimal cuotaTotal;
    private String huella;
    private String huellaAnterior;
    private OffsetDateTime fechaHoraGenRegistro;
    private String xmlEnviado;
    private String status;
    private String csvAeat;
    private String errorMessage;
    private Integer retryCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime sentAt;
    private String userId;

    private String descripcionOperacion;

    // Rectificativa fields (loaded from document when building XML)
    private String tipoRectificativa;
    private String facturaRectificadaNif;
    private String facturaRectificadaNumSerie;
    private LocalDate facturaRectificadaFecha;

    // Issuer config fields (loaded when building XML)
    private String claveRegimen;
    private Integer vatPercentage;

    // Tax breakdown for XML
    private BigDecimal baseImponible;
    private BigDecimal cuotaRepercutida;
}
