package dev.erosende.secadapter.repository.cloud;

import dev.erosende.billaton.application.domain.ports.secondary.cloud.SupabaseStorageRepository;
import dev.erosende.secadapter.config.cloud.SupabaseStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Repository
public class SupabaseStorageRepositoryImpl implements SupabaseStorageRepository {

  @Autowired
  @Qualifier("supabaseS3Client")
  private S3Client s3Client;

  @Autowired
  private SupabaseStorageConfig config;

  @Override
  public void uploadBackup(String key, byte[] fileContent, String contentType) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(config.getBucketName())
        .key(key)
        .contentType(contentType)
        .contentLength((long) fileContent.length)
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));
  }

}
