package com.company.sprintreporter.config.jwt;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.isTokenValid(token)) {
            UUID userId = jwtTokenProvider.getUserId(token);

            // Reject tokens belonging to deleted users immediately, without waiting for expiry.
            if (!userRepository.existsById(userId)) {
                filterChain.doFilter(request, response);
                return;
            }

            UUID orgId = jwtTokenProvider.getOrganizationId(token);
            String email = jwtTokenProvider.getEmail(token);
            UserRole role = jwtTokenProvider.getRole(token);

            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, orgId, email, role);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
