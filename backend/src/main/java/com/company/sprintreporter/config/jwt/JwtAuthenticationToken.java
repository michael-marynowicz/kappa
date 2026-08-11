package com.company.sprintreporter.config.jwt;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID userId;
    private final UUID organizationId;
    private final String email;
    private final UserRole role;

    public JwtAuthenticationToken(UUID userId, UUID organizationId, String email, UserRole role) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        this.userId = userId;
        this.organizationId = organizationId;
        this.email = email;
        this.role = role;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }
}
