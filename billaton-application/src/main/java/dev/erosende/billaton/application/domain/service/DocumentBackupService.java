package dev.erosende.billaton.application.domain.service;

import dev.erosende.billaton.application.domain.model.DocumentBackupDto;
import dev.erosende.billaton.application.domain.ports.secondary.cloud.R2Repository;
import dev.erosende.billaton.application.domain.ports.secondary.cloud.SupabaseStorageRepository;
import dev.erosende.billaton.application.domain.ports.secondary.db.DocumentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentBackupService {

  private static final String ZIP_FILE_NAME_TEMPLATE = "backup_{0}.zip";
  private static final String BACKUP_FILE_NAME_TEMPLATE = "{0}/{1}/{2}/{3}_{4}_{5}.pdf";

  private final SupabaseStorageRepository supabaseStorageRepository;
  private final DocumentsRepository documentsRepository;
  private final R2Repository r2Repository;

  @Transactional(rollbackFor = Exception.class)
  @Scheduled(cron = "${scheduled-services.document-backup}")
  public void backupDocuments() throws IOException {
    List<DocumentBackupDto> documents = documentsRepository.findDocumentsForBackup();
    String zipName = MessageFormat.format(ZIP_FILE_NAME_TEMPLATE, LocalDate.now().toString());
    if (CollectionUtils.isNotEmpty(documents)) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
        documents.forEach(document -> {
          String filename = buildFileName(document);
          byte[] fileContent = r2Repository.downloadDocument(document.getResourcePath());
          try {
            addFileToZip(filename, fileContent, zos);
            documentsRepository.updateDocumentBackupStatus(document.getDocumentId(), true);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });

        zos.finish();
        zos.flush();

        byte[] zipBytes = baos.toByteArray();

        supabaseStorageRepository.uploadBackup(zipName, zipBytes, "application/zip");
      }
    }
  }

  private void addFileToZip(String filename, byte[] fileContent, ZipOutputStream zos) throws IOException {
    ZipEntry zipEntry = new ZipEntry(filename);
    zipEntry.setSize(fileContent.length);
    zipEntry.setTime(System.currentTimeMillis());

    zos.putNextEntry(zipEntry);
    zos.write(fileContent);
    zos.closeEntry();
  }

  private String buildFileName(DocumentBackupDto document) {
    return MessageFormat.format(
        BACKUP_FILE_NAME_TEMPLATE,
        document.getIssuerFullName(),
        document.getRecipientFullName(),
        document.getDocumentType(),
        document.getDocumentType(),
        document.getDocumentCode(),
        document.getDocumentDate()
    );
  }

}
