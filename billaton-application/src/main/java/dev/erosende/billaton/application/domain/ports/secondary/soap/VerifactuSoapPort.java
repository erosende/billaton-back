package dev.erosende.billaton.application.domain.ports.secondary.soap;

import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import dev.erosende.billaton.application.domain.model.VerifactuSoapResponseDto;

public interface VerifactuSoapPort {

    VerifactuSoapResponseDto sendAlta(VerifactuRecordDto record);

    VerifactuSoapResponseDto sendAnulacion(VerifactuRecordDto record);
}
