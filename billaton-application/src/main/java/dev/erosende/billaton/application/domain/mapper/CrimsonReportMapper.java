package dev.erosende.billaton.application.domain.mapper;

import dev.erosende.billaton.application.domain.constant.CrimsonTemplate;
import dev.erosende.billaton.application.domain.model.*;
import io.micrometer.common.util.StringUtils;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CrimsonReportMapper {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.GERMAN));

  public static final String LINE_BREAK = "\n";
  public static final String COMMA_SEPARATOR = ", ";

  public Map<String, Object> mapToJasperParameters(ReportDataDto reportData) {
    Map<String, Object> parameters = new HashMap<>();

    mapIssuerParameters(parameters, reportData.getIssuer());

    mapIssuerConfigParameters(parameters, reportData.getIssuerConfig());

    mapRecipientParameters(parameters, reportData.getRecipient());

    mapDocumentParameters(parameters, reportData.getDocument());

    mapConceptsParameters(parameters, reportData.getConcepts());

    mapFinancialCalculations(parameters, reportData);

    return parameters;
  }

  private void mapIssuerParameters(Map<String, Object> parameters, ParticipantDto issuer) {
    if (issuer != null) {
      parameters.put(CrimsonTemplate.ISSUER_NAME, buildFullName(issuer.getName(), issuer.getSurnames()));
      parameters.put(CrimsonTemplate.ISSUER_IDENTIFICATION, issuer.getIdentificationNumber());
      parameters.put(CrimsonTemplate.ISSUER_ADDRESS, formatAddress(issuer.getAddress()));
      parameters.put(CrimsonTemplate.ISSUER_EMAIL, issuer.getEmail());
      parameters.put(CrimsonTemplate.ISSUER_PHONE_NUMBER, issuer.getPhoneNumber());
    }
  }

  private void mapIssuerConfigParameters(Map<String, Object> parameters, IssuerConfigDto issuerConfig) {
    if (issuerConfig != null) {
      parameters.put(CrimsonTemplate.LOGO_PATH, issuerConfig.getLogoPath());
      parameters.put(CrimsonTemplate.VAT, String.valueOf(issuerConfig.getVat()));
      parameters.put(CrimsonTemplate.ACCOUNT_NUMBER, issuerConfig.getPaymentAccountNumber());
    }
  }

  private void mapRecipientParameters(Map<String, Object> parameters, ParticipantDto recipient) {
    if (recipient != null) {
      parameters.put(CrimsonTemplate.RECIPIENT_NAME, buildFullName(recipient.getName(), recipient.getSurnames()));
      parameters.put(CrimsonTemplate.RECIPIENT_IDENTIFICATION, recipient.getIdentificationNumber());
      parameters.put(CrimsonTemplate.RECIPIENT_ADDRESS, formatAddress(recipient.getAddress()));
      parameters.put(CrimsonTemplate.RECIPIENT_EMAIL, recipient.getEmail());
      parameters.put(CrimsonTemplate.RECIPIENT_PHONE_NUMBER, recipient.getPhoneNumber());
    }
  }

  private void mapDocumentParameters(Map<String, Object> parameters, DocumentDto document) {
    if (document != null) {
      parameters.put(CrimsonTemplate.DOCUMENT_TYPE, document.getDocumentType());
      parameters.put(CrimsonTemplate.DOCUMENT_DATE, getFormattedDocumentDate(document.getDocumentDate()));
      parameters.put(CrimsonTemplate.DOCUMENT_CODE, document.getDocumentCode());
    }
  }

  private static Object getFormattedDocumentDate(LocalDate documentDate) {
    return documentDate != null ? documentDate.format(DATE_FORMATTER) : LocalDate.now().format(DATE_FORMATTER);
  }


  private void mapConceptsParameters(Map<String, Object> parameters, List<ConceptDto> concepts) {
    JRBeanCollectionDataSource conceptsDataSource = new JRBeanCollectionDataSource(concepts);
    parameters.put(CrimsonTemplate.CONCEPTS, conceptsDataSource);
  }

  private void mapFinancialCalculations(Map<String, Object> parameters, ReportDataDto reportData) {
    double subtotal = calculateSubtotal(reportData.getConcepts());
    double vatAmount = calculateVatAmount(subtotal, reportData.getIssuerConfig());
    double total = subtotal + vatAmount;

    parameters.put(CrimsonTemplate.SUBTOTAL, formatDouble(subtotal));
    parameters.put(CrimsonTemplate.TAXED_AMOUNT, formatDouble(vatAmount));
    parameters.put(CrimsonTemplate.TOTAL, formatDouble(total));
  }

  private String buildFullName(String name, String surnames) {
    StringBuilder fullName = new StringBuilder();

    if (name != null && !name.trim().isEmpty()) {
      fullName.append(name.trim());
    }

    if (surnames != null && !surnames.trim().isEmpty()) {
      if (!fullName.isEmpty()) {
        fullName.append(" ");
      }
      fullName.append(surnames.trim());
    }

    return fullName.toString();
  }

  private String formatAddress(AddressDto address) {
    if (address == null) {
      return null;
    }

    StringBuilder addressBuilder = new StringBuilder();

    String addressLineTwo = StringUtils.isBlank(address.getAddressLineTwo()) ? "" : address.getAddressLineTwo() + LINE_BREAK;
    addressBuilder.append(address.getAddressLineOne()).append(LINE_BREAK)
        .append(addressLineTwo)
        .append(address.getCity()).append(COMMA_SEPARATOR)
        .append(address.getProvince()).append(COMMA_SEPARATOR)
        .append(address.getPostalCode());

    return addressBuilder.toString();
  }

  private double calculateSubtotal(List<ConceptDto> concepts) {
    double result = Optional.ofNullable(concepts)
        .orElse(Collections.emptyList())
        .stream()
        .mapToDouble(concept -> {
          Integer amount = concept.getAmount();
          Double pricePerUnit = concept.getPricePerUnit();
          if (amount != null && pricePerUnit != null) {
            return amount * pricePerUnit;
          }
          return 0.0;
        })
        .sum();

    return Math.max(result, 0.0);
  }

  private double calculateVatAmount(double subtotal, IssuerConfigDto issuerConfig) {
    if (issuerConfig == null || issuerConfig.getVat() == null) {
      return 0.0;
    }

    double vatPercentage = issuerConfig.getVat() / 100.0;
    double vatAmount = subtotal * vatPercentage;

    return Math.max(vatAmount, 0.0);
  }

  private static String formatDouble(Double value) {
    if (value == null) {
      return null;
    }
    return decimalFormat.format(value);
  }

}