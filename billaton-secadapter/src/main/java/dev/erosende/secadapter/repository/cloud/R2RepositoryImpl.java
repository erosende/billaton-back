package dev.erosende.secadapter.repository.cloud;

import dev.erosende.billaton.application.domain.ports.secondary.cloud.R2Repository;
import dev.erosende.secadapter.config.cloud.R2Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Repository
public class R2RepositoryImpl implements R2Repository {

  @Autowired
  @Qualifier("r2S3Client")
  private S3Client s3Client;

  @Autowired
  private R2Config r2Config;

  @Override
  public String uploadDocument(String key, byte[] fileContent, String contentType) {
    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(r2Config.getDocumentsBucket())
          .key(key)
          .contentType(contentType)
          .contentLength((long) fileContent.length)
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(fileContent));

      log.info("File uploaded successfully: {}", key);

      // TODO in the future it would be nice to have a public URL to share certain files, such as images.
      return key;

    } catch (Exception e) {
      log.error("Error uploading file: {}", key, e);
      throw new RuntimeException("Failed to upload file", e);
    }
  }

  @Override
  public byte[] downloadDocument(String key) {
    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(r2Config.getDocumentsBucket())
          .key(key)
          .build();

      ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
      return response.asByteArray();

    } catch (Exception e) {
      log.error("Error downloading file: {}", key, e);
      throw new RuntimeException("Failed to download file", e);
    }
  }

}
