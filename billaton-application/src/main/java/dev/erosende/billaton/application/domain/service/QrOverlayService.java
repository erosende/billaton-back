package dev.erosende.billaton.application.domain.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class QrOverlayService {

    private static final float QR_SIZE_PT = 85f; // ~30mm
    private static final float MARGIN_PT = 14f;
    private static final float FONT_SIZE_LEGEND = 7f;
    private static final float FONT_SIZE_VERIFACTU = 6f;

    private final QrCodeService qrCodeService;

    public byte[] addQrOverlay(byte[] pdfBytes, String issuerNif, String numSerie,
                                LocalDate fecha, BigDecimal importeTotal) throws IOException {
        String qrUrl = qrCodeService.buildVerifactuUrl(issuerNif, numSerie, fecha, importeTotal);
        BufferedImage qrImage = qrCodeService.generateQrImage(qrUrl);

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDPage page = document.getPage(0);
            PDRectangle mediaBox = page.getMediaBox();
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);

            float qrX = mediaBox.getWidth() - QR_SIZE_PT - MARGIN_PT;
            float qrY = mediaBox.getHeight() - QR_SIZE_PT - MARGIN_PT;

            try (PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                cs.drawImage(pdImage, qrX, qrY, QR_SIZE_PT, QR_SIZE_PT);

                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                cs.beginText();
                cs.setFont(fontBold, FONT_SIZE_LEGEND);
                cs.newLineAtOffset(qrX, qrY - 10f);
                cs.showText("VERI*FACTU");
                cs.endText();

                cs.beginText();
                cs.setFont(fontRegular, FONT_SIZE_VERIFACTU);
                cs.newLineAtOffset(qrX, qrY - 18f);
                cs.showText("Verificable en sede electronica AEAT");
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
