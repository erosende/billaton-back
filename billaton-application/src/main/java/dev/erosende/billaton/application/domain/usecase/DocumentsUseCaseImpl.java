package dev.erosende.billaton.application.domain.usecase;

import dev.erosende.billaton.application.domain.constant.CrimsonTemplate;
import dev.erosende.billaton.application.domain.enums.TipoFactura;
import dev.erosende.billaton.application.domain.enums.VerifactuStatus;
import dev.erosende.billaton.application.domain.exception.ResourceNotFoundException;
import dev.erosende.billaton.application.domain.mapper.CrimsonReportMapper;
import dev.erosende.billaton.application.domain.model.ConceptDto;
import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.billaton.application.domain.model.DocumentFileDto;
import dev.erosende.billaton.application.domain.model.ReportDataDto;
import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import dev.erosende.billaton.application.domain.model.generic.Page;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;
import dev.erosende.billaton.application.domain.ports.primary.DocumentsUseCase;
import dev.erosende.billaton.application.domain.ports.secondary.cloud.R2Repository;
import dev.erosende.billaton.application.domain.ports.secondary.db.ConceptsRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.DocumentsRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.InvoiceSeriesRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.IssuerConfigRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.ParticipantsRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.VerifactuRepository;
import dev.erosende.billaton.application.domain.service.HashChainService;
import dev.erosende.billaton.application.domain.service.QrOverlayService;
import dev.erosende.billaton.application.domain.service.VerifactuRecordBuilder;
import dev.erosende.billaton.application.domain.util.ReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentsUseCaseImpl implements DocumentsUseCase {

  private static final String DOCUMENT_RESOURCE_PATH_TEMPLATE = "/documents/{0}/{1}";
  private static final String DOCUMENT_FILE_NAME_TEMPLATE = "{0}_{1}_{2}.pdf";

  private final DocumentsRepository documentsRepository;
  private final ParticipantsRepository participantsRepository;
  private final ConceptsRepository conceptsRepository;
  private final IssuerConfigRepository issuerConfigRepository;
  private final R2Repository r2Repository;

  private final ReportGenerator reportGenerator;
  private final CrimsonReportMapper crimsonReportMapper;

  private final InvoiceSeriesRepository invoiceSeriesRepository;
  private final VerifactuRepository verifactuRepository;
  private final HashChainService hashChainService;
  private final VerifactuRecordBuilder verifactuRecordBuilder;
  private final QrOverlayService qrOverlayService;

  @Override
  @Transactional(readOnly = true)
  public Page<DocumentDto> getDocuments(String userId, PagingParams pagingParams) {
    return documentsRepository.findDocuments(userId, pagingParams);
  }

  @Override
  public List<ConceptDto> getConcepts(Integer documentId) {
    return conceptsRepository.findConcepts(documentId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Integer createDocument(String userId, DocumentDto document) {
    TipoFactura tipo = TipoFactura.fromCode(document.getTipoFactura());
    String prefix = tipo.getSeriesPrefix();
    int year = document.getDocumentDate().getYear();
    String documentCode = invoiceSeriesRepository.getNextDocumentCode(document.getIssuerId(), prefix, year);
    document.setDocumentCode(documentCode);
    return documentsRepository.saveDocument(userId, document);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Integer createConcept(ConceptDto concept) {
    return conceptsRepository.saveConcept(concept);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateDocument(DocumentDto document) {
    documentsRepository.updateDocument(document);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateConcept(ConceptDto concept) {
    conceptsRepository.updateConcept(concept);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteDocument(String userId, Integer documentId) {
    // Check if a SENT verifactu_record exists — if so, create an ANULACION record
    verifactuRepository.findByDocumentId(documentId).ifPresent(existingRecord -> {
      if (VerifactuStatus.SENT.name().equals(existingRecord.getStatus())) {
        DocumentDto document = documentsRepository.findDocumentById(documentId).orElse(null);
        if (document != null) {
          participantsRepository.findParticipantById(document.getIssuerId()).ifPresent(issuer -> {
            VerifactuRecordDto anulacion = verifactuRecordBuilder.buildAnulacion(document, issuer, userId);
            String previousHuella = verifactuRepository.findLastHuellaByIssuerNif(anulacion.getIssuerNif()).orElse(null);
            String huella = hashChainService.calculateHuellaAnulacion(anulacion, previousHuella);
            anulacion.setHuella(huella);
            anulacion.setHuellaAnterior(previousHuella);
            verifactuRepository.save(anulacion);
          });
        }
      } else if (VerifactuStatus.PENDING.name().equals(existingRecord.getStatus())
          || VerifactuStatus.ERROR.name().equals(existingRecord.getStatus())) {
        verifactuRepository.markAsCancelled(existingRecord.getVerifactuRecordId());
      }
    });

    documentsRepository.deleteDocumentLogically(userId, documentId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteConcept(Integer documentId, Integer conceptId) {
    conceptsRepository.deleteConcept(documentId, conceptId);
  }

  // TODO This needs data validation before attempting to generate the report
  @Override
  @Transactional(rollbackFor = Exception.class)
  public DocumentFileDto generateDocumentAsPdf(Integer documentId) throws ResourceNotFoundException {
    ReportDataDto reportData = new ReportDataDto();
    fillDocumentData(documentId, reportData);
    fillIssuerData(reportData);
    fillRecipientData(reportData);
    fillConceptsData(documentId, reportData);

    Map<String, Object> reportParams = crimsonReportMapper.mapToJasperParameters(reportData);

    byte[] report = reportGenerator.generatePdfReport(CrimsonTemplate.TEMPLATE_NAME, reportParams);

    // Create VeriFactu record and overlay QR on PDF
    DocumentDto document = reportData.getDocument();
    try {
      VerifactuRecordDto verifactuRecord = verifactuRecordBuilder.buildAlta(
          document, reportData.getIssuer(), reportData.getIssuerConfig(),
          reportData.getConcepts(), null // userId set below
      );

      String previousHuella = verifactuRepository.findLastHuellaByIssuerNif(verifactuRecord.getIssuerNif()).orElse(null);
      String huella = hashChainService.calculateHuella(verifactuRecord, previousHuella);
      verifactuRecord.setHuella(huella);
      verifactuRecord.setHuellaAnterior(previousHuella);

      verifactuRepository.save(verifactuRecord);

      report = qrOverlayService.addQrOverlay(report,
          verifactuRecord.getIssuerNif(), document.getDocumentCode(),
          document.getDocumentDate(), verifactuRecord.getImporteTotal());
    } catch (Exception e) {
      // Log but don't fail PDF generation if VeriFactu overlay fails
      log.error("Failed to create VeriFactu record or QR overlay for document {}: {}", documentId, e.getMessage());
    }

    String fileName = buildDocumentFileName(document);
    String resourcePath = buildDocumentResourcePath(reportData.getRecipient().getParticipantId(), fileName);
    r2Repository.uploadDocument(resourcePath, report, "application/pdf");
    documentsRepository.updateDocumentResourcePath(documentId, resourcePath);

    return new DocumentFileDto(fileName, report);
  }

  @Override
  public DocumentFileDto downloadDocumentAsPdf(Integer documentId) throws ResourceNotFoundException {
    DocumentDto document = documentsRepository.findDocumentById(documentId)
        .orElseThrow(() -> new ResourceNotFoundException("Document", "documentId", documentId));

    if (document.getResourcePath() == null) {
      throw new ResourceNotFoundException("Document file", "documentId", documentId);
    }
    byte[] report = r2Repository.downloadDocument(document.getResourcePath());
    String fileName = buildDocumentFileName(document);

    return new DocumentFileDto(fileName, report);
  }

  private void fillDocumentData(Integer documentId, ReportDataDto reportData) throws ResourceNotFoundException {
    reportData.setDocument(documentsRepository.findDocumentById(documentId)
        .orElseThrow(() -> new ResourceNotFoundException("Document", "documentId", documentId))
    );
  }

  private void fillIssuerData(ReportDataDto reportData) throws ResourceNotFoundException {
    Integer issuerId = reportData.getDocument().getIssuerId();
    reportData.setIssuer(participantsRepository.findParticipantById(issuerId)
        .orElseThrow(() -> new ResourceNotFoundException("Issuer", "participantId", issuerId))
    );

    reportData.setIssuerConfig(issuerConfigRepository.findIssuerConfig(issuerId)
        .orElseThrow(() -> new ResourceNotFoundException("IssuerConfig", "participantId", issuerId))
    );
  }

  private void fillRecipientData(ReportDataDto reportData) throws ResourceNotFoundException {
    Integer recipientId = reportData.getDocument().getRecipientId();
    reportData.setRecipient(participantsRepository.findParticipantById(recipientId)
        .orElseThrow(() -> new ResourceNotFoundException("Recipient", "participantId", recipientId))
    );
  }

  private void fillConceptsData(Integer documentId, ReportDataDto reportData) {
    reportData.setConcepts(conceptsRepository.findConcepts(documentId));
  }

  private String buildDocumentFileName(DocumentDto document) {
    return MessageFormat.format(
        DOCUMENT_FILE_NAME_TEMPLATE,
        document.getDocumentType(),
        document.getDocumentId(),
        document.getDocumentDate()
    );
  }

  private String buildDocumentResourcePath(Integer recipientId, String filename) {
    return MessageFormat.format(DOCUMENT_RESOURCE_PATH_TEMPLATE, recipientId, filename);
  }

}
