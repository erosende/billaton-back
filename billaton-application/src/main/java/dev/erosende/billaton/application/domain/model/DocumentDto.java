package dev.erosende.billaton.application.domain.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentDto {

    private Integer documentId;
    private Integer documentTypeId;
    private String documentType;
    private String documentCode;
    private LocalDate documentDate;
    private String resourcePath;
    private Integer issuerId;
    private String issuerName;
    private Integer recipientId;
    private String recipientName;
    private Double totalAmount;
    private String tipoFactura;
    private String descripcionOperacion;
    private Integer facturaRectificadaId;
    private String tipoRectificativa;

}