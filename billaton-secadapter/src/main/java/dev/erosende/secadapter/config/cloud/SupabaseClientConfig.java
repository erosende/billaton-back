package dev.erosende.secadapter.config.cloud;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class SupabaseClientConfig {

  private final SupabaseStorageConfig config;

  @Bean(name = "supabaseS3Client")
  public S3Client supabaseS3Client() {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        config.getAccessKey(),
        config.getSecretKey()
    );

    return S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .endpointOverride(URI.create(config.getUrl()))
        .region(Region.of(config.getRegion()))
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .build())
        .build();
  }

}
