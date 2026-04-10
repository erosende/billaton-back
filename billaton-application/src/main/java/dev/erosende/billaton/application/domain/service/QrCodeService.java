package dev.erosende.billaton.application.domain.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class QrCodeService {

    private static final String VERIFACTU_QR_BASE_URL = "https://www2.agenciatributaria.es/wlpl/TIKE-CONT/ValidarQR";
    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int QR_SIZE = 256;

    public String buildVerifactuUrl(String nif, String numSerie, LocalDate fecha, BigDecimal importeTotal) {
        return VERIFACTU_QR_BASE_URL
                + "?nif=" + URLEncoder.encode(nif, StandardCharsets.UTF_8)
                + "&numserie=" + URLEncoder.encode(numSerie, StandardCharsets.UTF_8)
                + "&fecha=" + URLEncoder.encode(fecha.format(FECHA_FORMAT), StandardCharsets.UTF_8)
                + "&importe=" + importeTotal.stripTrailingZeros().toPlainString();
    }

    public BufferedImage generateQrImage(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE,
                    Map.of(EncodeHintType.MARGIN, 1));
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new IllegalStateException("Failed to generate QR code", e);
        }
    }
}
