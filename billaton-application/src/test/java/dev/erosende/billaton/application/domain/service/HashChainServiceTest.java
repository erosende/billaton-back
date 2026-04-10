package dev.erosende.billaton.application.domain.service;

import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class HashChainServiceTest {

    private HashChainService hashChainService;

    @BeforeEach
    void setUp() {
        hashChainService = new HashChainService();
    }

    @Test
    void calculateHuella_firstRecord_emptyPreviousHash() {
        VerifactuRecordDto record = VerifactuRecordDto.builder()
                .issuerNif("B72877814")
                .numSerieFactura("F-001-2025")
                .fechaExpedicion(LocalDate.of(2025, 4, 15))
                .tipoFactura("F1")
                .cuotaTotal(new BigDecimal("210"))
                .importeTotal(new BigDecimal("1210"))
                .fechaHoraGenRegistro(OffsetDateTime.of(2025, 4, 15, 10, 30, 0, 0, ZoneOffset.ofHours(2)))
                .build();

        String huella = hashChainService.calculateHuella(record, null);

        assertNotNull(huella);
        assertEquals(64, huella.length(), "SHA-256 hex should be 64 chars");
        assertEquals(huella, huella.toUpperCase(), "Hash must be uppercase hex");
    }

    @Test
    void calculateHuella_withPreviousHash_includesItInChain() {
        VerifactuRecordDto record = VerifactuRecordDto.builder()
                .issuerNif("B72877814")
                .numSerieFactura("F-002-2025")
                .fechaExpedicion(LocalDate.of(2025, 4, 16))
                .tipoFactura("F1")
                .cuotaTotal(new BigDecimal("105"))
                .importeTotal(new BigDecimal("605"))
                .fechaHoraGenRegistro(OffsetDateTime.of(2025, 4, 16, 11, 0, 0, 0, ZoneOffset.ofHours(2)))
                .build();

        String previousHash = "4EECCE4DD48C0539665385D61D451BA921B7160CA6FEF46CD3C2E2BC5C778E14";
        String huella = hashChainService.calculateHuella(record, previousHash);

        assertNotNull(huella);
        assertEquals(64, huella.length());
    }

    @Test
    void calculateHuella_sameInputs_sameOutput() {
        VerifactuRecordDto record = VerifactuRecordDto.builder()
                .issuerNif("B72877814")
                .numSerieFactura("F-001-2025")
                .fechaExpedicion(LocalDate.of(2025, 4, 15))
                .tipoFactura("F1")
                .cuotaTotal(new BigDecimal("210"))
                .importeTotal(new BigDecimal("1210"))
                .fechaHoraGenRegistro(OffsetDateTime.of(2025, 4, 15, 10, 30, 0, 0, ZoneOffset.ofHours(2)))
                .build();

        String h1 = hashChainService.calculateHuella(record, null);
        String h2 = hashChainService.calculateHuella(record, null);

        assertEquals(h1, h2, "Deterministic: same input must produce same hash");
    }

    @Test
    void calculateHuella_differentPreviousHash_differentOutput() {
        VerifactuRecordDto record = VerifactuRecordDto.builder()
                .issuerNif("B72877814")
                .numSerieFactura("F-001-2025")
                .fechaExpedicion(LocalDate.of(2025, 4, 15))
                .tipoFactura("F1")
                .cuotaTotal(new BigDecimal("210"))
                .importeTotal(new BigDecimal("1210"))
                .fechaHoraGenRegistro(OffsetDateTime.of(2025, 4, 15, 10, 30, 0, 0, ZoneOffset.ofHours(2)))
                .build();

        String h1 = hashChainService.calculateHuella(record, null);
        String h2 = hashChainService.calculateHuella(record, "AAAA");

        assertNotEquals(h1, h2, "Different previous hash must produce different output");
    }

    @Test
    void calculateHuellaAnulacion_usesReducedFieldSet() {
        VerifactuRecordDto record = VerifactuRecordDto.builder()
                .issuerNif("B72877814")
                .numSerieFactura("F-001-2025")
                .fechaExpedicion(LocalDate.of(2025, 4, 15))
                .fechaHoraGenRegistro(OffsetDateTime.of(2025, 4, 15, 10, 30, 0, 0, ZoneOffset.ofHours(2)))
                .build();

        String huella = hashChainService.calculateHuellaAnulacion(record, null);

        assertNotNull(huella);
        assertEquals(64, huella.length());
        assertEquals(huella, huella.toUpperCase());
    }

    @Test
    void buildConcatenationAlta_formatsCorrectly() {
        VerifactuRecordDto record = VerifactuRecordDto.builder()
                .issuerNif("B72877814")
                .numSerieFactura("F-001-2025")
                .fechaExpedicion(LocalDate.of(2025, 4, 15))
                .tipoFactura("F1")
                .cuotaTotal(new BigDecimal("210"))
                .importeTotal(new BigDecimal("1210"))
                .fechaHoraGenRegistro(OffsetDateTime.of(2025, 4, 15, 10, 30, 0, 0, ZoneOffset.ofHours(2)))
                .build();

        String concat = hashChainService.buildConcatenationAlta(record, "");

        assertTrue(concat.startsWith("IDEmisorFactura=B72877814&"));
        assertTrue(concat.contains("NumSerieFactura=F-001-2025&"));
        assertTrue(concat.contains("FechaExpedicionFactura=15-04-2025&"));
        assertTrue(concat.contains("TipoFactura=F1&"));
        assertTrue(concat.contains("CuotaTotal=210&"));
        assertTrue(concat.contains("ImporteTotal=1210&"));
        assertTrue(concat.contains("Huella=&"));
        assertTrue(concat.contains("FechaHoraHusoGenRegistro=2025-04-15T10:30:00+02:00"));
    }
}
