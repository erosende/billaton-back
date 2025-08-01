package dev.erosende.billaton.application.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class ReportDataDto {

  private ParticipantDto issuer;
  private IssuerConfigDto issuerConfig;
  private ParticipantDto recipient;
  private DocumentDto document;
  List<ConceptDto> concepts;

}
