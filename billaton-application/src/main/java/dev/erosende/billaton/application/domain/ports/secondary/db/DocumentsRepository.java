package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.billaton.application.domain.model.generic.Page;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;

import java.util.Optional;

public interface DocumentsRepository {

  Page<DocumentDto> findDocuments(PagingParams pagingParams);

  Optional<DocumentDto> findDocumentById(Integer documentId);

  Integer saveDocument(DocumentDto document);

  void updateDocument(DocumentDto document);

  void updateDocumentResourcePath(Integer documentId, String resourcePath);

  void deleteDocument(Integer documentId);

  void softDeleteParticipantDocuments(Integer participantId);

}
