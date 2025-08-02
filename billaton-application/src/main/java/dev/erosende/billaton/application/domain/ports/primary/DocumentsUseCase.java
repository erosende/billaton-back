package dev.erosende.billaton.application.domain.ports.primary;

import dev.erosende.billaton.application.domain.exception.ResourceNotFoundException;
import dev.erosende.billaton.application.domain.model.ConceptDto;
import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.billaton.application.domain.model.DocumentFileDto;
import dev.erosende.billaton.application.domain.model.generic.Page;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;

import java.util.List;

public interface DocumentsUseCase {

  Page<DocumentDto> getDocuments(String userId, PagingParams pagingParams);

  List<ConceptDto> getConcepts(Integer documentId);

  Integer createDocument(String userId, DocumentDto document);

  Integer createConcept(ConceptDto concept);

  void updateDocument(DocumentDto document);

  void updateConcept(ConceptDto concept);

  void deleteDocument(String userId, Integer documentId);

  void deleteConcept(Integer documentId, Integer conceptId);

  DocumentFileDto generateDocumentAsPdf(Integer documentId) throws ResourceNotFoundException;

  DocumentFileDto downloadDocumentAsPdf(Integer documentId) throws ResourceNotFoundException;

}
