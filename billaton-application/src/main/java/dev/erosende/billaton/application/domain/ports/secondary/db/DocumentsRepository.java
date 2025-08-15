package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.DocumentBackupDto;
import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.billaton.application.domain.model.generic.Page;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;

import java.util.List;
import java.util.Optional;

public interface DocumentsRepository {

  Page<DocumentDto> findDocuments(String userId, PagingParams pagingParams);

  List<DocumentBackupDto> findDocumentsForBackup();

  Optional<DocumentDto> findDocumentById(Integer documentId);

  Integer saveDocument(String userId, DocumentDto document);

  void updateDocument(DocumentDto document);

  void updateDocumentResourcePath(Integer documentId, String resourcePath);

  void updateDocumentBackupStatus(Integer documentId, boolean backedUp);

  void deleteDocumentLogically(String userId, Integer documentId);

  int softDeleteDocumentByRecipient(String userId, Integer recipientId);

  int softDeleteDocumentByIssuer(String userId, Integer issuerId);

}
