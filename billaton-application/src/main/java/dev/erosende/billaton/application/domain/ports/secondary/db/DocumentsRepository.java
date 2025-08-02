package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.billaton.application.domain.model.generic.Page;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;

import java.util.Optional;

public interface DocumentsRepository {

  Page<DocumentDto> findDocuments(String userId, PagingParams pagingParams);

  Optional<DocumentDto> findDocumentById(Integer documentId);

  Integer saveDocument(String userId, DocumentDto document);

  void updateDocument(DocumentDto document);

  void updateDocumentResourcePath(Integer documentId, String resourcePath);

  void deleteDocumentLogically(String userId, Integer documentId);

  int softDeleteDocumentByRecipient(String userId, Integer recipientId);

  int softDeleteDocumentByIssuer(String userId, Integer issuerId);

}
