package dev.erosende.priadapter.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentRequestDto {

    @NotNull
    @Schema(
        description = "Type of document identifier",
        example = "1")
    private Integer documentTypeId;

    @NotNull
    @Schema(
        description = "Date when the document was created",
        example = "2024-01-15")
    private LocalDate documentDate;

    @NotNull
    @Schema(
        description = "Identifier of the issuer",
        example = "1")
    private Integer issuerId;

    @NotNull
    @Schema(
        description = "Identifier of the recipient",
        example = "1")
    private Integer recipientId;

    @NotNull
    @Schema(description = "Invoice type code", example = "F1")
    private String tipoFactura;

    @NotNull
    @Schema(description = "Operation description for AEAT", example = "Servicios de consultoría")
    private String descripcionOperacion;

    @Schema(description = "ID of the rectified document (only for R1/R4)", example = "42")
    private Integer facturaRectificadaId;

    @Schema(description = "Rectification type: S (sustitutiva) or I (incremental)")
    private String tipoRectificativa;

}
