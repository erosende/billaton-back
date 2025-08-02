package dev.erosende.billaton.application.domain.usecase;

import dev.erosende.billaton.application.domain.constant.CrimsonTemplate;
import dev.erosende.billaton.application.domain.exception.ResourceNotFoundException;
import dev.erosende.billaton.application.domain.mapper.CrimsonReportMapper;
import dev.erosende.billaton.application.domain.model.ConceptDto;
import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.billaton.application.domain.model.DocumentFileDto;
import dev.erosende.billaton.application.domain.model.ReportDataDto;
import dev.erosende.billaton.application.domain.model.generic.Page;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;
import dev.erosende.billaton.application.domain.ports.primary.DocumentsUseCase;
import dev.erosende.billaton.application.domain.ports.secondary.cloud.R2Repository;
import dev.erosende.billaton.application.domain.ports.secondary.db.ConceptsRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.DocumentsRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.IssuerConfigRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.ParticipantsRepository;
import dev.erosende.billaton.application.domain.util.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

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

  @Override
  @Transactional(readOnly = true)
  public Page<DocumentDto> getDocuments(PagingParams pagingParams) {
    return documentsRepository.findDocuments(pagingParams);
  }

  @Override
  public List<ConceptDto> getConcepts(Integer documentId) {
    return conceptsRepository.findConcepts(documentId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Integer createDocument(DocumentDto document) {
    return documentsRepository.saveDocument(document);
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
  public void deleteDocument(Integer documentId) {
    documentsRepository.deleteDocumentLogically(documentId);
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

    byte [] report = reportGenerator.generatePdfReport(CrimsonTemplate.TEMPLATE_NAME, reportParams);

    String fileName = buildDocumentFileName(reportData.getDocument());
    String resourcePath = buildDocumentResourcePath(reportData.getRecipient().getParticipantId(), fileName);
    r2Repository.uploadDocument(resourcePath, report, "application/pdf" );
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
    byte [] report = r2Repository.downloadDocument(document.getResourcePath());
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
