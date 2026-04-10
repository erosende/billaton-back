package dev.erosende.billaton.application.domain.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class QrOverlayService {

    private static final float PAGE_WIDTH  = PDRectangle.A4.getWidth();   // 595 pt
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();  // 842 pt

    private static final float QR_SIZE_PT   = 200f;
    private static final float QR_X         = (PAGE_WIDTH - QR_SIZE_PT) / 2f;
    private static final float QR_Y         = PAGE_HEIGHT - 160f - QR_SIZE_PT; // ~482 pt from bottom

    private static final float FONT_SIZE_TITLE = 16f;
    private static final float FONT_SIZE_LABEL = 9f;
    private static final float FONT_SIZE_VALUE = 9f;
    private static final float FONT_SIZE_URL   = 7f;
    private static final float FONT_SIZE_FOOTER = 8f;

    private static final float MARGIN_LEFT  = 80f;
    private static final float DATA_START_Y = QR_Y - 30f;
    private static final float LINE_HEIGHT  = 16f;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final QrCodeService qrCodeService;

    public byte[] addQrOverlay(byte[] pdfBytes, String issuerNif, String numSerie,
                                LocalDate fecha, BigDecimal importeTotal) throws IOException {
        String qrUrl = qrCodeService.buildVerifactuUrl(issuerNif, numSerie, fecha, importeTotal);
        BufferedImage qrImage = qrCodeService.generateQrImage(qrUrl);

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDPage verifactuPage = new PDPage(PDRectangle.A4);
            document.addPage(verifactuPage);

            PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);
            PDType1Font fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(document, verifactuPage)) {

                // Title
                cs.beginText();
                cs.setFont(fontBold, FONT_SIZE_TITLE);
                float titleWidth = fontBold.getStringWidth("VERI*FACTU") / 1000f * FONT_SIZE_TITLE;
                cs.newLineAtOffset((PAGE_WIDTH - titleWidth) / 2f, PAGE_HEIGHT - 80f);
                cs.showText("VERI*FACTU");
                cs.endText();

                // QR code
                cs.drawImage(pdImage, QR_X, QR_Y, QR_SIZE_PT, QR_SIZE_PT);

                // Data rows
                float y = DATA_START_Y;
                y = drawDataRow(cs, fontBold, fontRegular, "NIF Emisor:", issuerNif, y);
                y = drawDataRow(cs, fontBold, fontRegular, "Num. serie:", numSerie, y);
                y = drawDataRow(cs, fontBold, fontRegular, "Fecha:", fecha.format(DATE_FORMATTER), y);
                y = drawDataRow(cs, fontBold, fontRegular, "Importe total:", importeTotal.toPlainString() + " EUR", y);

                // Verification URL
                y -= 4f;
                cs.beginText();
                cs.setFont(fontBold, FONT_SIZE_LABEL);
                cs.newLineAtOffset(MARGIN_LEFT, y);
                cs.showText("URL verificacion:");
                cs.endText();

                y -= LINE_HEIGHT;
                cs.beginText();
                cs.setFont(fontRegular, FONT_SIZE_URL);
                cs.newLineAtOffset(MARGIN_LEFT, y);
                cs.showText(qrUrl);
                cs.endText();

                // Footer
                cs.beginText();
                cs.setFont(fontRegular, FONT_SIZE_FOOTER);
                float footerWidth = fontRegular.getStringWidth("Verificable en sede electronica de la Agencia Tributaria") / 1000f * FONT_SIZE_FOOTER;
                cs.newLineAtOffset((PAGE_WIDTH - footerWidth) / 2f, 50f);
                cs.showText("Verificable en sede electronica de la Agencia Tributaria");
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private float drawDataRow(PDPageContentStream cs, PDType1Font fontBold, PDType1Font fontRegular,
                               String label, String value, float y) throws IOException {
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_LABEL);
        cs.newLineAtOffset(MARGIN_LEFT, y);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.setFont(fontRegular, FONT_SIZE_VALUE);
        cs.newLineAtOffset(MARGIN_LEFT + 110f, y);
        cs.showText(value);
        cs.endText();

        return y - LINE_HEIGHT;
    }
}
