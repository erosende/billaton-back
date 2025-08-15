package dev.erosende.secadapter.config.cloud;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "supabase.storage")
public class SupabaseStorageConfig {

  private String url;
  private String accessKey;
  private String secretKey;
  private String bucketName;
  private String region;
  private String projectId;

}
