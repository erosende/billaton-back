package dev.erosende.priadapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentResponseDto {

    @Schema(
        description = "Identifier of the document",
        example = "1")
    private Integer documentId;

    @Schema(
        description = "Type of document identifier",
        example = "1")
    private Integer documentTypeId;

    @Schema(
        description = "Code of the document",
        example = "1-2025")
    private String documentCode;

    @Schema(
        description = "Date when the document was created",
        example = "2024-01-15")
    private LocalDate documentDate;

    @Schema(
        description = "Path where the document is stored",
        example = "https://placehold.co/600x400")
    private String resourcePath;

    @Schema(
        description = "Identifier of the issuer",
        example = "1")
    private Integer issuerId;

    @Schema(
        description = "Name of the issuer",
        example = "<NAME>")
    private String issuerName;

    @Schema(
        description = "Identifier of the recipient",
        example = "1")
    private Integer recipientId;

    @Schema(
        description = "Name of the recipient",
        example = "<NAME>")
    private String recipientName;

    @Schema(
        description = "Total amount of the document",
        example = "20.00")
    private Double totalAmount;

}