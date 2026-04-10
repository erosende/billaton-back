package dev.erosende.billaton.application.domain.service;

import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

@Service
public class HashChainService {

    private static final DateTimeFormatter FECHA_EXPEDICION_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FECHA_HORA_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public String calculateHuella(VerifactuRecordDto record, String previousHuella) {
        String concatenation = buildConcatenationAlta(record, previousHuella != null ? previousHuella : "");
        return sha256Hex(concatenation);
    }

    public String calculateHuellaAnulacion(VerifactuRecordDto record, String previousHuella) {
        String concatenation = buildConcatenationAnulacion(record, previousHuella != null ? previousHuella : "");
        return sha256Hex(concatenation);
    }

    public String buildConcatenationAlta(VerifactuRecordDto record, String previousHuella) {
        return "IDEmisorFactura=" + record.getIssuerNif()
                + "&NumSerieFactura=" + record.getNumSerieFactura()
                + "&FechaExpedicionFactura=" + formatFechaExpedicion(record.getFechaExpedicion())
                + "&TipoFactura=" + record.getTipoFactura()
                + "&CuotaTotal=" + formatDecimal(record.getCuotaTotal())
                + "&ImporteTotal=" + formatDecimal(record.getImporteTotal())
                + "&Huella=" + previousHuella
                + "&FechaHoraHusoGenRegistro=" + formatFechaHora(record.getFechaHoraGenRegistro());
    }

    private String buildConcatenationAnulacion(VerifactuRecordDto record, String previousHuella) {
        return "IDEmisorFactura=" + record.getIssuerNif()
                + "&NumSerieFactura=" + record.getNumSerieFactura()
                + "&FechaExpedicionFactura=" + formatFechaExpedicion(record.getFechaExpedicion())
                + "&Huella=" + previousHuella
                + "&FechaHoraHusoGenRegistro=" + formatFechaHora(record.getFechaHoraGenRegistro());
    }

    private String formatFechaExpedicion(LocalDate date) {
        return date.format(FECHA_EXPEDICION_FORMAT);
    }

    private String formatFechaHora(OffsetDateTime dateTime) {
        return dateTime.format(FECHA_HORA_FORMAT);
    }

    private String formatDecimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
