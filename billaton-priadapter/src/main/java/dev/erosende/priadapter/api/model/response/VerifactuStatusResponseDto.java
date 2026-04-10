package dev.erosende.priadapter.api.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class VerifactuStatusResponseDto {

    private String status;
    private String csvAeat;
    private String errorMessage;
    private Integer retryCount;
    private String huella;
    private OffsetDateTime sentAt;
}
