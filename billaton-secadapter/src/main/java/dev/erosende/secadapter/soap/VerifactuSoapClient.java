package dev.erosende.secadapter.soap;

import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import dev.erosende.billaton.application.domain.model.VerifactuSoapResponseDto;
import dev.erosende.billaton.application.domain.ports.secondary.soap.VerifactuSoapPort;
import dev.erosende.secadapter.config.VerifactuConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerifactuSoapClient implements VerifactuSoapPort {

    private final VerifactuConfig verifactuConfig;
    private final SSLSocketFactory verifactuSslSocketFactory;
    private final VerifactuXmlBuilder xmlBuilder;

    @Override
    public VerifactuSoapResponseDto sendAlta(VerifactuRecordDto record) {
        String xml = xmlBuilder.buildAltaXml(record);
        return sendSoapRequest(xml);
    }

    @Override
    public VerifactuSoapResponseDto sendAnulacion(VerifactuRecordDto record) {
        String xml = xmlBuilder.buildAnulacionXml(record);
        return sendSoapRequest(xml);
    }

    private VerifactuSoapResponseDto sendSoapRequest(String soapXml) {
        try {
            URL url = new URL(verifactuConfig.getSoapEndpoint());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(verifactuSslSocketFactory);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            conn.setRequestProperty("SOAPAction", "");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(60_000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(soapXml.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String responseBody = readStream(
                    responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
            );

            log.info("AEAT SOAP response code: {}", responseCode);
            return parseResponse(responseBody);
        } catch (Exception e) {
            log.error("SOAP request failed: {}", e.getMessage());
            return VerifactuSoapResponseDto.builder()
                    .success(false)
                    .errorMessage("Connection error: " + e.getMessage())
                    .build();
        }
    }

    private VerifactuSoapResponseDto parseResponse(String responseXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(responseXml)));

            String estado = getTagValue(doc, "EstadoRegistro");
            String csv = getTagValue(doc, "CSV");
            String errorCode = getTagValue(doc, "CodigoErrorRegistroFacturacion");
            String errorDesc = getTagValue(doc, "DescripcionErrorRegistroFacturacion");

            boolean success = "Correcto".equalsIgnoreCase(estado);

            return VerifactuSoapResponseDto.builder()
                    .success(success)
                    .csv(csv)
                    .errorCode(errorCode)
                    .errorMessage(errorDesc)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse AEAT response: {}", e.getMessage());
            return VerifactuSoapResponseDto.builder()
                    .success(false)
                    .errorMessage("Failed to parse response: " + e.getMessage())
                    .build();
        }
    }

    private String getTagValue(Document doc, String localName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() > 0 && nodes.item(0).getTextContent() != null) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
