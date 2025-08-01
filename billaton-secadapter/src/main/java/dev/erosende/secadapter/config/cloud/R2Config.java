package dev.erosende.secadapter.config.cloud;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cloudflare.r2")
public class R2Config {

  private String accessKey;
  private String secretKey;
  private String documentsBucket;
  private String imagesBucket;
  private String endpoint;
  private String region = "auto";

}