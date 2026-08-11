package com.company.sprintreporter.service.jira;

import com.company.sprintreporter.config.AtlassianOAuth2Properties;
import com.company.sprintreporter.domain.entity.JiraConfiguration;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import com.company.sprintreporter.infrastructure.persistence.JiraConfigurationRepository;
import com.company.sprintreporter.infrastructure.persistence.OrganizationRepository;
import com.company.sprintreporter.service.exception.JiraConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class JiraOAuthService {

    private static final String AUTH_URL = "https://auth.atlassian.com/authorize";
    private static final String TOKEN_URL = "https://auth.atlassian.com/oauth/token";
    private static final String RESOURCES_URL = "https://api.atlassian.com/oauth/token/accessible-resources";

    private final AtlassianOAuth2Properties props;
    private final JiraConfigurationRepository jiraConfigRepository;
    private final OrganizationRepository organizationRepository;
    private final WebClient webClient;

    // state -> pending state entry (orgId + expiry), used for CSRF protection
    private final ConcurrentHashMap<String, OAuthPendingState> pendingStates = new ConcurrentHashMap<>();

    public JiraOAuthService(AtlassianOAuth2Properties props,
                             JiraConfigurationRepository jiraConfigRepository,
                             OrganizationRepository organizationRepository) {
        this.props = props;
        this.jiraConfigRepository = jiraConfigRepository;
        this.organizationRepository = organizationRepository;
        this.webClient = WebClient.builder().build();
    }

    /**
     * Builds the Atlassian authorization URL and stores state for CSRF validation.
     * The admin's browser should be redirected to the returned URL.
     */
    public String buildAuthorizationUrl(UUID orgId) {
        String state = UUID.randomUUID().toString();
        pendingStates.put(state, new OAuthPendingState(orgId, Instant.now().plus(10, ChronoUnit.MINUTES)));
        cleanExpiredStates();

        return UriComponentsBuilder.fromHttpUrl(AUTH_URL)
                .queryParam("audience", "api.atlassian.com")
                .queryParam("client_id", props.getClientId())
                .queryParam("scope", "read:jira-work read:jira-user offline_access")
                .queryParam("redirect_uri", props.getRedirectUri())
                .queryParam("state", state)
                .queryParam("response_type", "code")
                .queryParam("prompt", "consent")
                .toUriString();
    }

    /**
     * Handles the OAuth callback from Atlassian: exchanges code for tokens,
     * fetches the cloud instance ID, and persists everything in DB.
     */
    @Transactional
    public void handleCallback(String code, String state) {
        OAuthPendingState pending = pendingStates.remove(state);
        if (pending == null || Instant.now().isAfter(pending.expiry())) {
            throw new IllegalStateException("Invalid or expired OAuth state parameter. Please restart the connection flow.");
        }

        Map<String, Object> tokenResponse = exchangeCodeForTokens(code);
        String accessToken = (String) tokenResponse.get("access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");
        Integer expiresIn = tokenResponse.get("expires_in") instanceof Integer i ? i : 3600;

        String cloudId = fetchCloudId(accessToken);
        String cloudBaseUrl = "https://api.atlassian.com/ex/jira/" + cloudId;

        Organization org = organizationRepository.getReferenceById(pending.orgId());
        JiraConfiguration config = jiraConfigRepository.findByOrganizationId(pending.orgId())
                .orElseGet(() -> JiraConfiguration.builder()
                        .organization(org)
                        .projectKey("PENDING")
                        .boardId(null)
                        .build());

        config.setAuthType(JiraAuthType.OAUTH2);
        config.setBaseUrl(cloudBaseUrl);
        config.setEncryptedToken(encryptToken(accessToken));
        config.setOauthRefreshToken(encryptToken(refreshToken));
        config.setOauthTokenExpiry(Instant.now().plusSeconds(expiresIn));
        config.setOauthCloudId(cloudId);
        config.setActive(true);

        jiraConfigRepository.save(config);
        log.info("Atlassian OAuth tokens stored for org {} (cloudId={})", pending.orgId(), cloudId);
    }

    /**
     * Returns a valid access token for the given config, refreshing it if expired or close to expiry.
     */
    @Transactional
    public String getValidAccessToken(JiraConfiguration config) {
        boolean nearExpiry = config.getOauthTokenExpiry() != null
                && Instant.now().isAfter(config.getOauthTokenExpiry().minus(5, ChronoUnit.MINUTES));

        if (nearExpiry) {
            log.info("OAuth access token near expiry for cloudId={}, refreshing", config.getOauthCloudId());
            return doRefreshAccessToken(config);
        }
        return decryptToken(config.getEncryptedToken());
    }

    /**
     * Removes all OAuth tokens from the org's Jira config and marks it inactive.
     */
    @Transactional
    public void disconnect(UUID orgId) {
        jiraConfigRepository.findByOrganizationId(orgId).ifPresent(config -> {
            config.setAuthType(JiraAuthType.PAT);
            config.setEncryptedToken(encryptToken(""));
            config.setOauthRefreshToken(null);
            config.setOauthTokenExpiry(null);
            config.setOauthCloudId(null);
            config.setActive(false);
            jiraConfigRepository.save(config);
            log.info("Disconnected Atlassian OAuth for org {}", orgId);
        });
    }

    private String doRefreshAccessToken(JiraConfiguration config) {
        String refreshToken = decryptToken(config.getOauthRefreshToken());

        Map<String, Object> tokenResponse = webClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "grant_type", "refresh_token",
                        "client_id", props.getClientId(),
                        "client_secret", props.getClientSecret(),
                        "refresh_token", refreshToken
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        String newAccessToken = (String) tokenResponse.get("access_token");
        String newRefreshToken = tokenResponse.get("refresh_token") instanceof String s ? s : refreshToken;
        Integer expiresIn = tokenResponse.get("expires_in") instanceof Integer i ? i : 3600;

        config.setEncryptedToken(encryptToken(newAccessToken));
        config.setOauthRefreshToken(encryptToken(newRefreshToken));
        config.setOauthTokenExpiry(Instant.now().plusSeconds(expiresIn));
        jiraConfigRepository.save(config);

        return newAccessToken;
    }

    private Map<String, Object> exchangeCodeForTokens(String code) {
        return webClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "grant_type", "authorization_code",
                        "client_id", props.getClientId(),
                        "client_secret", props.getClientSecret(),
                        "code", code,
                        "redirect_uri", props.getRedirectUri()
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));
    }

    private String fetchCloudId(String accessToken) {
        List<Map<String, Object>> resources = webClient.get()
                .uri(RESOURCES_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block(Duration.ofSeconds(10));

        if (resources == null || resources.isEmpty()) {
            throw new JiraConnectionException("https://api.atlassian.com",
                    new RuntimeException("No accessible Atlassian Cloud resources found for this account"));
        }

        String cloudId = (String) resources.get(0).get("id");
        log.info("Atlassian accessible resources: {} instance(s), using cloudId={}", resources.size(), cloudId);
        return cloudId;
    }

    private void cleanExpiredStates() {
        Instant now = Instant.now();
        pendingStates.entrySet().removeIf(e -> now.isAfter(e.getValue().expiry()));
    }

    private String encryptToken(String rawToken) {
        return Base64.getEncoder().encodeToString(rawToken.getBytes());
    }

    private String decryptToken(String encryptedToken) {
        return new String(Base64.getDecoder().decode(encryptedToken));
    }

    private record OAuthPendingState(UUID orgId, Instant expiry) {}
}
