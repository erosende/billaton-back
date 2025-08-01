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

    @Schema(
        description = "Code of the document",
        example = "1-2025")
    private String documentCode;

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

}