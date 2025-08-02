package dev.erosende.init.config.auth;

import dev.erosende.billaton.application.domain.model.auth.JwtAuthenticationToken;
import dev.erosende.billaton.application.domain.model.auth.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SupabaseJwtService supabaseJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extract the Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token provided, continue with the filter chain
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract the token from the header
            String token = supabaseJwtService.extractTokenFromHeader(authHeader);
            
            if (token == null) {
                log.warn("Invalid Authorization header format");
                filterChain.doFilter(request, response);
                return;
            }

            // Validate and parse the token
            UserDto userDto = supabaseJwtService.validateAndParseToken(token);
            
            if (userDto == null) {
                log.warn("Invalid or expired JWT token");
                filterChain.doFilter(request, response);
                return;
            }

            // Check if token is expired
            if (supabaseJwtService.isTokenExpired(userDto)) {
                log.warn("JWT token is expired for user: {}", userDto.getEmail());
                filterChain.doFilter(request, response);
                return;
            }

            // Create authorities based on user role
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + userDto.getRole().toUpperCase())
            );

            // Create authentication token
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(userDto, token, authorities);
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.debug("Successfully authenticated user: {} with role: {}", userDto.getEmail(), userDto.getRole());

        } catch (Exception e) {
            log.error("Error processing JWT token", e);
            // Clear security context in case of error
            SecurityContextHolder.clearContext();
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Skip filter for health check and other public endpoints
        String path = request.getRequestURI();
        return path.startsWith("/api/actuator/") || 
               path.equals("/api/actuator/health") ||
               path.equals("/api/actuator/info");
    }
}