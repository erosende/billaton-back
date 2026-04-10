package dev.erosende.billaton.application.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifactuSoapResponseDto {

    private boolean success;
    private String csv;
    private String errorCode;
    private String errorMessage;
}
