package dev.erosende.billaton.application.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
    }

    @Test
    void buildVerifactuUrl_formatsCorrectly() {
        String url = qrCodeService.buildVerifactuUrl(
                "B72877814",
                "F-001-2025",
                LocalDate.of(2025, 4, 15),
                new BigDecimal("1210.40")
        );

        assertEquals(
                "https://www2.agenciatributaria.es/wlpl/TIKE-CONT/ValidarQR?nif=B72877814&numserie=F-001-2025&fecha=15-04-2025&importe=1210.4",
                url
        );
    }

    @Test
    void buildVerifactuUrl_removesTrailingZeros() {
        String url = qrCodeService.buildVerifactuUrl(
                "B12345678",
                "F-010-2025",
                LocalDate.of(2025, 1, 1),
                new BigDecimal("500.00")
        );

        assertTrue(url.endsWith("importe=500"));
    }

    @Test
    void generateQrImage_returnsValidImage() {
        BufferedImage image = qrCodeService.generateQrImage("https://example.com/test");

        assertNotNull(image);
        assertTrue(image.getWidth() >= 200);
        assertTrue(image.getHeight() >= 200);
    }
}
