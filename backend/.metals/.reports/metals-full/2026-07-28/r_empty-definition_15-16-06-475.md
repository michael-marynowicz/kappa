error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/config/jwt/JwtTokenProvider.java:org/springframework/core/env/Environment#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/config/jwt/JwtTokenProvider.java
empty definition using pc, found symbol in pc: org/springframework/core/env/Environment#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 271
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/config/jwt/JwtTokenProvider.java
text:
```scala
package com.company.sprintreporter.config.jwt;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.@@Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenMinutes;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes:60}") long accessTokenMinutes,
            Environment environment) {
        if (secret == null || secret.isBlank()
 secret.equals("ThisIsADevelopmentSecretKeyThatMustBeAtLeast32CharsLong!")) {
            boolean isProd = Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(p -> p.contains("prod") || p.contains("production"));
            if (isProd) {
                throw new IllegalStateException(
                        "JWT_SECRET must be set to a secure value in production. "
                        + "Do NOT use the default development key.");
            }
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenMinutes * 60;
    }

    public String generateAccessToken(UUID userId, String email, UUID orgId, UserRole role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("org_id", orgId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    public UUID getOrganizationId(String token) {
        return UUID.fromString(parseToken(token).get("org_id", String.class));
    }

    public UserRole getRole(String token) {
        return UserRole.valueOf(parseToken(token).get("role", String.class));
    }

    public String getEmail(String token) {
        return parseToken(token).get("email", String.class);
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/core/env/Environment#