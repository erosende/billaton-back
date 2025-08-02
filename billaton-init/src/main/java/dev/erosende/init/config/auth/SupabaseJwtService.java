package dev.erosende.init.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.erosende.billaton.application.domain.model.auth.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SupabaseJwtService {

    private final String jwtSecret;
    private final ObjectMapper objectMapper;

    public SupabaseJwtService(@Value("${supabase.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Validates and parses a Supabase JWT token
     * @param token The JWT token to validate
     * @return UserDto if token is valid, null otherwise
     */
    public UserDto validateAndParseToken(String token) {
        try {
            // Create the signing key from the Supabase JWT secret
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            // Parse and validate the token
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extract user information from claims
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            
            // Get token timestamps
            Instant issuedAt = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null;
            Instant expiresAt = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;

            // Extract user metadata if present
            Map<String, Object> metadata = new HashMap<>();
            Object userMetadata = claims.get("user_metadata");
            if (userMetadata != null) {
                if (userMetadata instanceof Map) {
                    metadata.putAll((Map<String, Object>) userMetadata);
                }
            }

            // Build and return UserDto
            return UserDto.builder()
                    .id(userId)
                    .email(email)
                    .role(role != null ? role : "authenticated")
                    .tokenIssuedAt(issuedAt)
                    .tokenExpiresAt(expiresAt)
                    .metadata(metadata)
                    .build();

        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
            return null;
        }
    }

    /**
     * Extracts the token from the Authorization header
     * @param authHeader The Authorization header value
     * @return The token without the "Bearer " prefix, or null if invalid
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Checks if a token is expired
     * @param userDto The user DTO containing token information
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(UserDto userDto) {
        if (userDto == null || userDto.getTokenExpiresAt() == null) {
            return true;
        }
        return Instant.now().isAfter(userDto.getTokenExpiresAt());
    }
}