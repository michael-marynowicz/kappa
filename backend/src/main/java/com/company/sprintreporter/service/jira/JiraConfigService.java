package com.company.sprintreporter.service.jira;

import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.JiraConfiguration;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import com.company.sprintreporter.infrastructure.persistence.JiraConfigurationRepository;
import com.company.sprintreporter.infrastructure.persistence.OrganizationRepository;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import com.company.sprintreporter.service.exception.BusinessException;
import com.company.sprintreporter.service.exception.JiraAuthenticationException;
import com.company.sprintreporter.service.exception.JiraConnectionException;
import com.company.sprintreporter.service.exception.JiraPermissionException;
import com.company.sprintreporter.service.exception.JiraApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class JiraConfigService {

    private static final String MYSELF_ENDPOINT = "/rest/api/2/myself";

    private final JiraConfigurationRepository jiraConfigRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final JiraOAuthService jiraOAuthService;

    public JiraConfigService(JiraConfigurationRepository jiraConfigRepository,
                              OrganizationRepository organizationRepository,
                              UserRepository userRepository,
                              @Lazy JiraOAuthService jiraOAuthService) {
        this.jiraConfigRepository = jiraConfigRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.jiraOAuthService = jiraOAuthService;
    }

    public Optional<JiraConfiguration> findByOrganizationId(UUID organizationId) {
        return jiraConfigRepository.findByOrganizationId(organizationId);
    }

    public JiraConfiguration getByOrganizationId(UUID organizationId) {
        return jiraConfigRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new BusinessException("Jira configuration not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public JiraConfiguration createOrUpdate(UUID organizationId, String baseUrl, JiraAuthType authType,
                                             String userEmail, String token, String projectKey, Integer boardId) {
        Organization org = organizationRepository.getReferenceById(organizationId);

        JiraConfiguration config = jiraConfigRepository.findByOrganizationId(organizationId)
                .orElse(JiraConfiguration.builder()
                        .organization(org)
                        .build());

        config.setBaseUrl(trimTrailingSlash(baseUrl));
        config.setAuthType(authType != null ? authType : JiraAuthType.PAT);
        config.setUserEmail(userEmail);
        config.setEncryptedToken(encryptToken(token));
        config.setProjectKey(projectKey);
        config.setBoardId(boardId);
        config.setActive(true);

        return jiraConfigRepository.save(config);
    }

    /**
     * Saves only the Jira credentials (baseUrl, authType, userEmail, token).
     * Does NOT overwrite projectKey or boardId — those belong to dashboards.
     */
    @Transactional
    public JiraConfiguration saveCredentials(UUID organizationId, String baseUrl, JiraAuthType authType,
                                              String userEmail, String token) {
        Organization org = organizationRepository.getReferenceById(organizationId);

        JiraConfiguration config = jiraConfigRepository.findByOrganizationId(organizationId)
                .orElse(JiraConfiguration.builder()
                        .organization(org)
                        .build());

        config.setBaseUrl(trimTrailingSlash(baseUrl));
        config.setAuthType(authType != null ? authType : JiraAuthType.PAT);
        config.setUserEmail(userEmail);
        config.setEncryptedToken(encryptToken(token));
        // Not verified yet — testConnection() will flip this to true/false once actually checked.
        config.setActive(false);

        return jiraConfigRepository.save(config);
    }

    @Transactional
    public boolean testConnection(UUID organizationId) {
        JiraConfiguration config = getByOrganizationId(organizationId);
        
        // Build auth header
        String headerName = "Authorization";
        String headerValue;
        if (config.getAuthType() == JiraAuthType.OAUTH2) {
            headerValue = "Bearer " + jiraOAuthService.getValidAccessToken(config);
        } else if (config.getAuthType() == JiraAuthType.PAT) {
            headerValue = "Bearer " + decryptToken(config.getEncryptedToken());
        } else {
            String credentials = config.getUserEmail() + ":" + decryptToken(config.getEncryptedToken());
            headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        }

        String baseUrl = config.getBaseUrl();
        String failingEndpoint = MYSELF_ENDPOINT;

        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(headerName, headerValue)
                    .defaultHeader("Accept", "application/json")
                    .build();

            // Step 1: validate credentials only.
            webClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(MYSELF_ENDPOINT)
                    .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .blockOptional(Duration.ofSeconds(10));

            // Step 2: if dashboard context exists, validate board/project access too.
            if (config.getBoardId() != null && config.getProjectKey() != null && !config.getProjectKey().isBlank()) {
            failingEndpoint = "/rest/agile/1.0/board/" + config.getBoardId() + "/sprint";
            webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/rest/agile/1.0/board/{boardId}/sprint")
                    .queryParam("state", "active")
                    .queryParam("maxResults", 1)
                    .build(config.getBoardId()))
                .retrieve()
                .bodyToMono(String.class)
                .blockOptional(Duration.ofSeconds(10));

            failingEndpoint = "/rest/api/2/search";
            String testJql = "project = " + config.getProjectKey() + " ORDER BY created DESC";
            webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/rest/api/2/search")
                    .queryParam("jql", testJql)
                    .queryParam("maxResults", 1)
                    .queryParam("fields", "key")
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .blockOptional(Duration.ofSeconds(10));
            }

            log.info("Jira connection test successful for org {} (board={} project={})",
                organizationId, config.getBoardId(), config.getProjectKey());
            config.setActive(true);
            jiraConfigRepository.save(config);
            return true;
        } catch (WebClientResponseException.Unauthorized e) {
            log.warn("Jira authentication failed for org {}", organizationId);
            config.setActive(false);
            jiraConfigRepository.save(config);
            throw new JiraAuthenticationException(decryptToken(config.getEncryptedToken()), baseUrl);
        } catch (WebClientResponseException.Forbidden e) {
            log.warn("Jira permission denied for org {} on {}", organizationId, failingEndpoint);
            config.setActive(false);
            jiraConfigRepository.save(config);
            if (MYSELF_ENDPOINT.equals(failingEndpoint)) {
                throw new JiraPermissionException(
                        "Jira permission denied (HTTP 403) on " + failingEndpoint + ": your credentials were " +
                        "rejected before any project/board check. This usually means the auth type doesn't match " +
                        "your Jira instance (e.g. a Personal Access Token only works with Bearer auth on Jira " +
                        "Server/Data Center, while Jira Cloud requires Basic auth with email + API token), or the " +
                        "token/account lacks API access. Verify base URL, auth type and token.");
            }
            throw new JiraPermissionException(config.getProjectKey(), String.valueOf(config.getBoardId()));
        } catch (WebClientResponseException e) {
            log.error("Jira API error for org {}: HTTP {} on {}", organizationId, e.getStatusCode().value(), failingEndpoint);
            config.setActive(false);
            jiraConfigRepository.save(config);
            throw new JiraApiException(e.getStatusCode().value(), e.getStatusText(), failingEndpoint);
        } catch (Exception e) {
            log.error("Jira connection error for org {}", organizationId, e);
            config.setActive(false);
            jiraConfigRepository.save(config);
            throw new JiraConnectionException(baseUrl, e);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    @Transactional
    public void triggerSync(UUID organizationId) {
        JiraConfiguration config = getByOrganizationId(organizationId);
        // TODO: trigger actual sync job (pull sprints, issues from Jira)
        config.setLastSyncAt(Instant.now());
        jiraConfigRepository.save(config);
    }

    private String encryptToken(String rawToken) {
        // TODO: replace with proper AES encryption using a vault key
        return Base64.getEncoder().encodeToString(rawToken.getBytes());
    }

    public String decryptToken(String encryptedToken) {
        // TODO: replace with proper AES decryption
        return new String(Base64.getDecoder().decode(encryptedToken));
    }

    // -------------------------------------------------------------------------
    // Per-user Jira credentials
    // -------------------------------------------------------------------------

    /**
     * Saves the user's personal Jira credentials (username + password/token).
     * Tests the credentials against the org's Jira baseUrl before persisting.
     */
    @Transactional
    public AppUser saveMyCredentials(UUID userId, UUID orgId, String baseUrl, String username, String password) {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);

        // Test credentials before saving
        String credentials = username + ":" + password;
        String basicHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        try {
            WebClient.builder()
                    .baseUrl(normalizedBaseUrl)
                    .defaultHeader("Authorization", basicHeader)
                    .defaultHeader("Accept", "application/json")
                    .build()
                    .get()
                    .uri(MYSELF_ENDPOINT)
                    .retrieve()
                    .bodyToMono(String.class)
                    .blockOptional(Duration.ofSeconds(10));
        } catch (WebClientResponseException.Unauthorized e) {
            throw new JiraAuthenticationException(username, normalizedBaseUrl);
        } catch (WebClientResponseException.Forbidden e) {
            throw new JiraPermissionException(
                    "Jira permission denied (HTTP 403) on " + MYSELF_ENDPOINT + ": your credentials were " +
                    "rejected before any project/board check. This usually means the auth type doesn't match " +
                    "your Jira instance, or the account/token lacks API access. Verify base URL, auth type and token.");
        } catch (WebClientResponseException e) {
            throw new JiraApiException(e.getStatusCode().value(), e.getStatusText(), MYSELF_ENDPOINT);
        } catch (Exception e) {
            throw new JiraConnectionException(normalizedBaseUrl, e);
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        user.setJiraBaseUrl(normalizedBaseUrl);
        user.setJiraUsername(username);
        user.setJiraEncryptedPassword(encryptToken(password));
        user.setJiraConnected(true);
        return userRepository.save(user);
    }

    private String normalizeBaseUrl(String url) {
        if (url == null) {
            return null;
        }
        String normalized = trimTrailingSlash(url.trim());
        if (normalized.endsWith("/agile")) {
            normalized = normalized.substring(0, normalized.length() - 6);
        }
        if (normalized.endsWith("/rest")) {
            normalized = normalized.substring(0, normalized.length() - 5);
        }
        return normalized;
    }

    /**
     * Returns the decrypted Jira password for the given user (used internally by JiraIssueRepositoryImpl).
     */
    public java.util.Optional<String> getDecryptedJiraPassword(UUID userId) {
        return userRepository.findById(userId)
                .filter(u -> Boolean.TRUE.equals(u.getJiraConnected()))
                .filter(u -> u.getJiraEncryptedPassword() != null)
                .map(u -> decryptToken(u.getJiraEncryptedPassword()));
    }

    public java.util.Optional<AppUser> findUserWithJiraCredentials(UUID userId) {
        return userRepository.findById(userId)
                .filter(u -> Boolean.TRUE.equals(u.getJiraConnected()));
    }
}
