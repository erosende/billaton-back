package dev.erosende.billaton.application.domain.model;

import lombok.Data;

@Data
public class DocumentBackupDto {

  private Integer documentId;
  private String issuerFullName;
  private String recipientFullName;
  private String documentCode;
  private String documentType;
  private String documentDate;
  private String resourcePath;

}
