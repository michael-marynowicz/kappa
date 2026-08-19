package com.company.sprintreporter.service.jira;

import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.JiraConfiguration;
import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import com.company.sprintreporter.infrastructure.jira.JiraProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Verifies that a board/project can actually be reached and returns data on
 * Jira before a dashboard is created for it, so admins get a clear error
 * ("these dashboard details are not valid") instead of a silently broken
 * dashboard that never loads any sprint data.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JiraDashboardValidationService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final JiraConfigService jiraConfigService;
    private final JiraOAuthService jiraOAuthService;
    private final JiraProperties jiraProperties;

    /**
     * Calls Jira for the given board and checks that it exists, is accessible,
     * and belongs to the given project key. Throws a 400 ResponseStatusException
     * with a user-facing message when validation fails.
     */
    public void validateBoardAccess(UUID organizationId, UUID adminUserId, Integer boardId, String projectKey) {
        if (jiraProperties.isMockMode()) {
            // No real Jira to call against in mock mode — skip validation.
            return;
        }

        WebClient client = buildWebClient(organizationId, adminUserId);

        String boardJson;
        try {
            boardJson = client.get()
                    .uri("/rest/agile/1.0/board/{boardId}", boardId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(15));
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to retrieve data for board " + boardId + ": this dashboard's information is not valid "
                            + "(the board does not exist or is not accessible with the connected Jira account).");
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to retrieve data for board " + boardId + ": Jira returned " + e.getStatusCode()
                            + ". Please check the board ID and project key.");
        } catch (Exception e) {
            log.warn("Dashboard validation failed for board {} / project {}", boardId, projectKey, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to retrieve data for board " + boardId + ". This dashboard's information does not "
                            + "seem valid — please check the board ID and project key and try again.");
        }

        String actualProjectKey = extractProjectKey(boardJson);
        if (actualProjectKey != null && projectKey != null
                && !actualProjectKey.equalsIgnoreCase(projectKey.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Board " + boardId + " belongs to project \"" + actualProjectKey + "\", not \"" + projectKey
                            + "\". This dashboard's information is not valid — please check the project key.");
        }
    }

    /**
     * Builds a WebClient using the same priority order as sprint-issue fetching:
     * the admin's own personal Jira credentials first, then the org-level config.
     */
    private WebClient buildWebClient(UUID organizationId, UUID adminUserId) {
        var userWithCreds = jiraConfigService.findUserWithJiraCredentials(adminUserId);
        if (userWithCreds.isPresent()) {
            AppUser user = userWithCreds.get();
            String baseUrl = user.getJiraBaseUrl();
            if (baseUrl != null && !baseUrl.isBlank()) {
                String password = jiraConfigService.decryptToken(user.getJiraEncryptedPassword());
                String credentials = user.getJiraUsername() + ":" + password;
                String headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
                return WebClient.builder()
                        .baseUrl(baseUrl)
                        .defaultHeader("Authorization", headerValue)
                        .defaultHeader("Accept", "application/json")
                        .build();
            }
        }

        JiraConfiguration config = jiraConfigService.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No Jira credentials configured for this organization yet. Please connect Jira first."));

        String token = jiraConfigService.decryptToken(config.getEncryptedToken());
        String headerValue;
        if (config.getAuthType() == JiraAuthType.OAUTH2) {
            headerValue = "Bearer " + jiraOAuthService.getValidAccessToken(config);
        } else if (config.getAuthType() == JiraAuthType.PAT) {
            headerValue = "Bearer " + token;
        } else {
            String credentials = config.getUserEmail() + ":" + token;
            headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        }

        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", headerValue)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @SuppressWarnings("unchecked")
    private String extractProjectKey(String boardJson) {
        try {
            Map<String, Object> board = MAPPER.readValue(boardJson, Map.class);
            Map<String, Object> location = (Map<String, Object>) board.get("location");
            return location != null ? (String) location.get("projectKey") : null;
        } catch (Exception e) {
            log.debug("Could not parse Jira board response to extract project key: {}", e.getMessage());
            return null;
        }
    }
}
