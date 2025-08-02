package dev.erosende.billaton.application.domain.model.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UserDto userDto;
    private final String token;

    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.userDto = null;
        setAuthenticated(false);
    }

    public JwtAuthenticationToken(UserDto userDto, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userDto = userDto;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return userDto;
    }

    public UserDto getUserDto() {
        return userDto;
    }

    public String getToken() {
        return token;
    }
}