package dev.erosende.billaton.application.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  private String id;
  private String email;
  private String role;
  private Instant tokenIssuedAt;
  private Instant tokenExpiresAt;
  private Map<String, Object> metadata;

}
