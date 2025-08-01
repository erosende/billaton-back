package dev.erosende.billaton.application.domain.util;

import dev.erosende.billaton.application.domain.constant.CrimsonTemplate;
import dev.erosende.billaton.application.domain.exception.JasperReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportGenerator {

  private static final String REPORT_TEMPLATE_PATH = "classpath:templates/%s.jasper";

  private final ResourceLoader resourceLoader;

  public byte[] generatePdfReport(String templateName, Map<String, Object> parameters) {
    byte[] pdfBytes = null;
    try {
      String templatePath = String.format(REPORT_TEMPLATE_PATH, templateName);
      log.info("Generating PDF report with template: {}", templateName);

      JasperReport jasperReport = loadJasperReport(templatePath);
      JRBeanCollectionDataSource finalTableCollection =
          ((JRBeanCollectionDataSource) parameters.get(CrimsonTemplate.CONCEPTS)).cloneDataSource();

      JasperPrint firstPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

      parameters.put(CrimsonTemplate.CONCEPTS, finalTableCollection);
      parameters.put(CrimsonTemplate.TOTAL_PAGES, firstPrint.getPages().size());
      JasperPrint finalPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

      pdfBytes = JasperExportManager.exportReportToPdf(finalPrint);
      log.info("PDF report generated successfully, size: {} bytes", pdfBytes.length);
    } catch (Exception e) {
      log.error("Unexpected error generating PDF report with template: {}", templateName, e);
    }

    return pdfBytes;
  }

  private JasperReport loadJasperReport(String templatePath) throws JRException, JasperReportException {
    try {
      Resource resource = resourceLoader.getResource(templatePath);

      if (!resource.exists()) {
        throw new JasperReportException("Jasper template not found: " + templatePath);
      }

      log.info("Loading Jasper report template from: {}", templatePath);

      try (InputStream inputStream = resource.getInputStream()) {
        return (JasperReport) JRLoader.loadObject(inputStream);
      }

    } catch (IOException e) {
      throw new JasperReportException("Failed to load Jasper template: " + templatePath);
    }
  }

}
