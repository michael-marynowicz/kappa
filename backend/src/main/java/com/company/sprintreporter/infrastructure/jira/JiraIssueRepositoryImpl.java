package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.entity.JiraConfiguration;
import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import com.company.sprintreporter.domain.model.SprintInfo;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import com.company.sprintreporter.service.jira.DashboardService;
import com.company.sprintreporter.service.jira.JiraConfigService;
import com.company.sprintreporter.service.jira.JiraOAuthService;
import com.company.sprintreporter.service.exception.JiraAuthenticationException;
import com.company.sprintreporter.service.exception.JiraConnectionException;
import com.company.sprintreporter.service.exception.JiraNotConnectedException;
import com.company.sprintreporter.service.exception.JiraPermissionException;
import com.company.sprintreporter.service.exception.JiraApiException;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of JiraIssueRepository.
 * Communicates with Jira REST API v3.
 *
 * In mock mode (jira.mock-mode=true), delegates to MockJiraIssueRepository.
 * In production mode, makes real HTTP calls to Jira Cloud/Server.
 *
 * This class is infrastructure — it knows about HTTP, Jira API format, auth headers.
 * The service layer is completely unaware of these details.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class JiraIssueRepositoryImpl implements JiraIssueRepository {

    private final JiraProperties jiraProperties;
    private final JiraIssueDomainMapper mapper;
    private final MockJiraIssueRepository mockRepository;
    private final JiraConfigService jiraConfigService;
    private final JiraOAuthService jiraOAuthService;
    private final DashboardService dashboardService;

    @Override
    public List<SprintIssue> fetchSprintIssues() {
        if (jiraProperties.isMockMode()) {
            log.info("Mock mode active — returning mock Jira data");
            return mockRepository.fetchSprintIssues();
        }

        return fetchFromJiraApi();
    }

    private List<SprintIssue> fetchFromJiraApi() {
        String boardId = getEffectiveBoardId();
        log.info("Fetching User Stories from active sprint for board {}", boardId);

        WebClient client = buildWebClient();

        String sprintJson = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/agile/1.0/board/{boardId}/sprint")
                        .queryParam("state", "active")
                        .build(boardId))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.warn("Agile sprint endpoint not available ({}), falling back to openSprints() JQL", e.getMessage());
                    return Mono.empty();
                })
                .block();

        Long sprintId = resolveSprintId(sprintJson);
        List<JiraApiResponse.Issue> allIssues = fetchAllPages(client, sprintId);

        // Resolve epic mapping via Greenhopper API, then fetch epic details
        Map<String, String> issueToEpicKey = fetchEpicKeyMapping(client);
        Map<String, String> epicNames = resolveEpicNames(client, issueToEpicKey, allIssues);

        // Detect issues added mid-sprint via Sprint Report API
        SprintReportData reportData = fetchSprintReportData(client, sprintId);

        log.info("Received {} issues from board {} ({} added mid-sprint)",
                allIssues.size(), getEffectiveBoardId(), reportData.addedKeys().size());

        List<SprintIssue> domainIssues = mapper.toDomainList(allIssues, epicNames, issueToEpicKey);

        // Mark issues that were added after sprint start
        return domainIssues.stream()
                .map(issue -> reportData.addedKeys().contains(issue.getIssueKey())
                        ? issue.withAddedAfterSprintStart(true)
                        : issue)
                .toList();
    }

    private WebClient buildWebClient() {
        // Priority 1: per-user Jira credentials (username + password)
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            var userWithCreds = jiraConfigService.findUserWithJiraCredentials(jwt.getUserId());
            if (userWithCreds.isPresent()) {
                var user = userWithCreds.get();
                String baseUrl = user.getJiraBaseUrl();
                if (baseUrl == null || baseUrl.isBlank()) {
                    throw new JiraNotConnectedException();
                }
                String password = jiraConfigService.decryptToken(user.getJiraEncryptedPassword());
                String credentials = user.getJiraUsername() + ":" + password;
                String headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
                log.info("Using per-user Jira credentials for user [{}] on baseUrl={}", jwt.getUserId(), baseUrl);
                return WebClient.builder()
                        .baseUrl(baseUrl)
                        .defaultHeader("Authorization", headerValue)
                        .defaultHeader("Accept", "application/json")
                        .build();
            }

            // Non-admin users MUST have personal credentials — block fallback to org-level (admin) credentials.
            // Admins without personal credentials are allowed to fall through to Priority 2 (org-level config).
            if (jwt.getRole() != UserRole.ADMIN) {
                throw new JiraNotConnectedException();
            }
        }

        // Priority 2: org-level config (legacy / admin-only setup)
        Optional<JiraConfiguration> dbConfig = resolveDbConfig();

        String baseUrl;
        String headerName;
        String headerValue;

        if (dbConfig.isPresent()) {
            JiraConfiguration config = dbConfig.get();
            baseUrl = config.getBaseUrl();
            String token = jiraConfigService.decryptToken(config.getEncryptedToken());
            log.info("Using Jira config from DB for org [{}]: baseUrl={}, authType={}, projectKey={}",
                    config.getOrganization().getId(), baseUrl, config.getAuthType(), config.getProjectKey());

            if (config.getAuthType() == JiraAuthType.OAUTH2) {
                headerName = "Authorization";
                headerValue = "Bearer " + jiraOAuthService.getValidAccessToken(config);
                log.debug("DB config: OAuth2 authentication (cloudId={})", config.getOauthCloudId());
            } else if (config.getAuthType() == JiraAuthType.PAT) {
                headerName = "Authorization";
                headerValue = "Bearer " + token;
                log.debug("DB config: PAT authentication");
            } else {
                String credentials = config.getUserEmail() + ":" + token;
                headerName = "Authorization";
                headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
                log.debug("DB config: Basic authentication");
            }
        } else {
            log.info("No DB Jira config found, falling back to application.yml (baseUrl={})", jiraProperties.getBaseUrl());
            baseUrl = jiraProperties.getBaseUrl();

            if (jiraProperties.getPat() != null && !jiraProperties.getPat().isBlank()) {
                headerName = "Authorization";
                headerValue = "Bearer " + jiraProperties.getPat();
                log.debug("YAML config: PAT authentication");
            } else if (jiraProperties.getApiToken() != null && jiraProperties.getApiToken().contains("JSESSIONID=")) {
                headerName = "Cookie";
                headerValue = jiraProperties.getApiToken();
                log.debug("YAML config: Cookie authentication");
            } else {
                String credentials = jiraProperties.getUserEmail() + ":" + jiraProperties.getApiToken();
                headerName = "Authorization";
                headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
                log.debug("YAML config: Basic authentication");
            }
        }

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(headerName, headerValue)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /**
     * Resolve the effective board ID from the active dashboard.
     * Throws NoDashboardSelectedException when no dashboard is active.
     */
    private String getEffectiveBoardId() {
        return resolveActiveDashboardBoardId()
                .orElseThrow(com.company.sprintreporter.service.exception.NoDashboardSelectedException::new);
    }

    /**
     * Resolve the effective project key from the active dashboard.
     * Throws NoDashboardSelectedException when no dashboard is active.
     */
    private String getEffectiveProjectKey() {
        return resolveActiveDashboardProjectKey()
                .orElseThrow(com.company.sprintreporter.service.exception.NoDashboardSelectedException::new);
    }

    private java.util.Optional<String> resolveActiveDashboardBoardId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwt) {
                return dashboardService.findActiveDashboard(jwt.getOrganizationId())
                        .map(d -> String.valueOf(d.getBoardId()));
            }
        } catch (Exception e) {
            log.debug("Could not resolve active dashboard boardId: {}", e.getMessage());
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<String> resolveActiveDashboardProjectKey() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwt) {
                return dashboardService.findActiveDashboard(jwt.getOrganizationId())
                        .map(d -> d.getProjectKey());
            }
        } catch (Exception e) {
            log.debug("Could not resolve active dashboard projectKey: {}", e.getMessage());
        }
        return java.util.Optional.empty();
    }

    private Optional<JiraConfiguration> resolveDbConfig() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwt) {
                UUID orgId = jwt.getOrganizationId();
                return jiraConfigService.findByOrganizationId(orgId);
            }
        } catch (Exception e) {
            log.debug("Could not resolve DB Jira config from security context: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private List<JiraApiResponse.Issue> fetchAllPages(WebClient client, Long sprintId) {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<JiraApiResponse.Issue> allIssues = new java.util.ArrayList<>();
        int startAt = 0;
        int maxResults = 100;

        JiraApiResponse.SearchResult page = fetchPage(client, objectMapper, sprintId, startAt, maxResults);
        while (page != null && page.getIssues() != null && !page.getIssues().isEmpty()) {
            allIssues.addAll(page.getIssues());
            startAt += page.getIssues().size();
            log.debug("Fetched {}/{} issues", allIssues.size(), page.getTotal());
            if (startAt >= page.getTotal() || allIssues.size() >= 500) {
                break;
            }
            page = fetchPage(client, objectMapper, sprintId, startAt, maxResults);
        }
        return allIssues;
    }

    private JiraApiResponse.SearchResult fetchPage(WebClient client,
                                                    com.fasterxml.jackson.databind.ObjectMapper objectMapper,
                                                    Long sprintId, int startAt, int maxResults) {
        String rawJson = sprintId != null
                ? fetchBoardSprintPage(client, sprintId, startAt, maxResults)
                : fetchJqlPage(client, startAt, maxResults);

        if (rawJson == null) {
            log.warn("Jira API returned null response");
            return null;
        }
        try {
            return objectMapper.readValue(rawJson, JiraApiResponse.SearchResult.class);
        } catch (Exception e) {
            log.error("Failed to parse Jira response: {}", e.getMessage());
            return null;
        }
    }

    private String fetchBoardSprintPage(WebClient client, long sprintId, int startAt, int maxResults) {
        return callJiraApi(
                client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/agile/1.0/board/{boardId}/sprint/{sprintId}/issue")
                        .queryParam("jql", "issuetype = Story ORDER BY created ASC")
                        .queryParam("fields", "summary,assignee,status,issuetype,labels,customfield_10002,customfield_20896,customfield_16701")
                        .queryParam("maxResults", maxResults)
                        .queryParam("startAt", startAt)
                        .build(getEffectiveBoardId(), sprintId))
                .retrieve()
                .bodyToMono(String.class),
                "/rest/agile/1.0/board/" + getEffectiveBoardId() + "/sprint/" + sprintId + "/issue"
        );
    }

    private String fetchJqlPage(WebClient client, int startAt, int maxResults) {
        String jql = "project = " + getEffectiveProjectKey()
                + " AND sprint in openSprints() AND issuetype = Story ORDER BY created ASC";
        log.info("Sprint not found, using JQL fallback: {}", jql);
        return callJiraApi(
                client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/2/search")
                        .queryParam("jql", jql)
                        .queryParam("fields", "summary,assignee,status,issuetype,labels,parent,customfield_10002,customfield_10014,customfield_20896,customfield_16701")
                        .queryParam("maxResults", maxResults)
                        .queryParam("startAt", startAt)
                        .build())
                .retrieve()
                .bodyToMono(String.class),
                "/rest/api/2/search?jql=" + jql
        );
    }

    private Long resolveSprintId(String sprintJson) {
        if (sprintJson == null) return null;
        try {
            com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(sprintJson);
            com.fasterxml.jackson.databind.JsonNode values = root.path("values");
            if (!values.isArray() || values.size() == 0) {
                log.warn("No active sprint found for board {}, falling back to openSprints()", getEffectiveBoardId());
                return null;
            }
            // When multiple sprints are active, pick the one with the latest startDate —
            // this matches what Jira shows as the "current sprint" on the board view.
            com.fasterxml.jackson.databind.JsonNode best = null;
            String bestStart = null;
            for (com.fasterxml.jackson.databind.JsonNode sprint : values) {
                String startDate = sprint.path("startDate").asText("");
                if (best == null || startDate.compareTo(bestStart) > 0) {
                    best = sprint;
                    bestStart = startDate;
                }
            }
            long sprintId = best.path("id").asLong();
            String name = best.path("name").asText("");
            log.info("Active sprint for board {}: '{}' (id={}, startDate={})", getEffectiveBoardId(), name, sprintId, bestStart);
            return sprintId;
        } catch (Exception e) {
            log.warn("Failed to parse sprint response: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, List<SprintIssue>> fetchClosedSprintIssues() {
        if (jiraProperties.isMockMode()) {
            log.info("Mock mode active — returning mock closed sprint data");
            return mockRepository.fetchClosedSprintIssues();
        }

        return fetchClosedSprintsFromApi();
    }

    @Override
    public List<SprintInfo> fetchSprintInfos() {
        if (jiraProperties.isMockMode()) {
            return mockRepository.fetchSprintInfos();
        }
        return fetchSprintInfosFromApi();
    }

    private Map<String, List<SprintIssue>> fetchClosedSprintsFromApi() {
        log.info("Fetching closed sprints for board {}", getEffectiveBoardId());
        WebClient client = buildWebClient();

        String sprintsJson = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/agile/1.0/board/{boardId}/sprint")
                        .queryParam("state", "closed")
                        .queryParam("maxResults", 10)
                        .build(getEffectiveBoardId()))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch closed sprints: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

        if (sprintsJson == null) {
            return Map.of();
        }

        Map<String, List<SprintIssue>> result = new LinkedHashMap<>();
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(sprintsJson);
            com.fasterxml.jackson.databind.JsonNode values = root.path("values");

            if (values.isArray()) {
                Map<String, String> issueToEpicKey = fetchEpicKeyMapping(client);
                for (com.fasterxml.jackson.databind.JsonNode sprintNode : values) {
                    long sprintId = sprintNode.path("id").asLong();
                    String sprintName = sprintNode.path("name").asText("Sprint " + sprintId)
                            .replaceAll("\\s*\\(.*", "").trim();

                    List<JiraApiResponse.Issue> issues = fetchAllPages(client, sprintId);
                    Map<String, String> epicNames = resolveEpicNames(client, issueToEpicKey, issues);
                    SprintReportData reportData = fetchSprintReportData(client, sprintId);

                    List<SprintIssue> domainIssues = mapper.toDomainList(issues, epicNames, issueToEpicKey);
                    List<SprintIssue> markedIssues = domainIssues.stream()
                            .map(issue -> {
                                SprintIssue marked = issue;
                                if (reportData.addedKeys().contains(issue.getIssueKey())) {
                                    marked = marked.withAddedAfterSprintStart(true);
                                }
                                // For closed sprints, use the sprint report's completed list
                                // as the authoritative source — avoids relying on current status names
                                if (!reportData.completedKeys().isEmpty()
                                        && reportData.completedKeys().contains(issue.getIssueKey())) {
                                    marked = marked.withStatus("Done");
                                }
                                return marked;
                            })
                            .toList();

                    result.put(sprintName, markedIssues);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse closed sprints response: {}", e.getMessage());
        }

        log.info("Fetched {} closed sprints", result.size());
        return result;
    }

    private List<SprintInfo> fetchSprintInfosFromApi() {
        log.info("Fetching sprint infos (closed + active + future) for board {}", getEffectiveBoardId());
        WebClient client = buildWebClient();
        List<SprintInfo> closedSprints = fetchSprintsByState(client, "closed", 100);
        List<SprintInfo> activeSprints = fetchSprintsByState(client, "active", 20);
        List<SprintInfo> futureSprints = fetchSprintsByState(client, "future", 100);

        String currentPiPrefix = activeSprints.stream()
                .map(SprintInfo::getName)
                .map(this::extractPiPrefix)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);

        List<SprintInfo> combined = new ArrayList<>();
        combined.addAll(closedSprints);
        combined.addAll(activeSprints);
        combined.addAll(futureSprints);

        // Deduplicate by sprint name in case Jira returns overlapping entries between states.
        Map<String, SprintInfo> dedupedByName = new LinkedHashMap<>();
        for (SprintInfo info : combined) {
            dedupedByName.putIfAbsent(info.getName(), info);
        }

        combined = new ArrayList<>(dedupedByName.values());

        if (currentPiPrefix != null) {
            combined = combined.stream()
                    .filter(s -> extractPiPrefix(s.getName())
                            .map(currentPiPrefix::equals)
                            .orElse(false))
                    .toList();
        }

        List<SprintInfo> sorted = combined.stream()
                .sorted((a, b) -> {
                    Integer ai = extractIterationNumber(a.getName());
                    Integer bi = extractIterationNumber(b.getName());
                    if (ai != null && bi != null) {
                        return Integer.compare(ai, bi);
                    }
                    if (a.getStartDate() != null && b.getStartDate() != null) {
                        return a.getStartDate().compareTo(b.getStartDate());
                    }
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .toList();

        log.info("Resolved {} sprint infos with dates", sorted.size());
        return sorted;
    }

    private List<SprintInfo> fetchSprintsByState(WebClient client, String state, int maxResults) {
        String json = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/agile/1.0/board/{boardId}/sprint")
                        .queryParam("state", state)
                        .queryParam("maxResults", maxResults)
                        .build(getEffectiveBoardId()))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch {} sprints: {}", state, e.getMessage());
                    return Mono.empty();
                })
                .block();

        if (json == null) {
            return List.of();
        }

        List<SprintInfo> infos = new ArrayList<>();
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            com.fasterxml.jackson.databind.JsonNode values = om.readTree(json).path("values");

            if (values.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : values) {
                    String name = node.path("name").asText("")
                            .replaceAll("\\s*\\(.*", "").trim();
                    String startStr = node.path("startDate").asText("");
                    String endStr = node.path("endDate").asText("");

                    LocalDate start = null;
                    LocalDate end = null;
                    try {
                        if (!startStr.isEmpty()) start = LocalDate.parse(startStr.substring(0, 10));
                        if (!endStr.isEmpty()) end = LocalDate.parse(endStr.substring(0, 10));
                    } catch (Exception e) {
                        log.debug("Could not parse dates for sprint {}: {} / {}", name, startStr, endStr);
                    }

                    infos.add(SprintInfo.builder().name(name).startDate(start).endDate(end).build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse {} sprints response: {}", state, e.getMessage());
        }

        return infos;
    }

    private Optional<String> extractPiPrefix(String sprintName) {
        Matcher m = Pattern.compile("PI#?(\\d+\\.\\d+)").matcher(sprintName == null ? "" : sprintName);
        return m.find() ? Optional.of(m.group(1)) : Optional.empty();
    }

    private Integer extractIterationNumber(String sprintName) {
        if (sprintName == null) return null;
        // "PI#26.3.1" or "PI26.3.1" → 1
        Matcher m = Pattern.compile("PI#?\\d+\\.\\d+\\.(\\d+)").matcher(sprintName);
        if (m.find()) return Integer.parseInt(m.group(1));
        // "Sprint 1" or "IT1" fallback
        m = Pattern.compile("(?i)(?:Sprint|IT)\\s*(\\d+)").matcher(sprintName);
        if (m.find()) return Integer.parseInt(m.group(1));
        return null;
    }

    /**
     * Fetch issue → epic key mapping from the Greenhopper API.
     * This is the only Jira API that reliably returns epic associations for board issues.
     */
    private Map<String, String> fetchEpicKeyMapping(WebClient client) {
        try {
            String rawJson = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/greenhopper/1.0/xboard/plan/backlog/data.json")
                            .queryParam("rapidViewId", getEffectiveBoardId())
                            .queryParam("selectedProjectKey", getEffectiveProjectKey())
                            .queryParam("issueLimit", 500)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        log.warn("Greenhopper API not available: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (rawJson == null) return Map.of();

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(rawJson);
            com.fasterxml.jackson.databind.JsonNode issuesNode = root.path("issues");

            Map<String, String> mapping = new LinkedHashMap<>();
            if (issuesNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode issue : issuesNode) {
                    String key = issue.path("key").asText("");
                    // Try "epic" field, then "epicField.epicKey"
                    String epicKey = issue.path("epic").asText(null);
                    if (epicKey == null || epicKey.isEmpty()) {
                        epicKey = issue.path("epicField").path("epicKey").asText(null);
                    }
                    if (!key.isEmpty() && epicKey != null && !epicKey.isEmpty()) {
                        mapping.put(key, epicKey);
                    }
                }
            }
            log.info("Greenhopper epic mapping: {} issues mapped to epics", mapping.size());

            // Debug: log first mapping for verification
            mapping.entrySet().stream().findFirst().ifPresent(e ->
                    log.info("Example epic mapping: {} → {}", e.getKey(), e.getValue()));

            return mapping;
        } catch (Exception e) {
            log.warn("Failed to fetch epic mapping from Greenhopper: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Uses the Sprint Report API to detect which issues were added after sprint start.
     * Returns a set of issue keys that were added mid-sprint.
     */
    private record SprintReportData(Set<String> addedKeys, Set<String> completedKeys) {}

    private SprintReportData fetchSprintReportData(WebClient client, Long sprintId) {
        if (sprintId == null) {
            return new SprintReportData(Set.of(), Set.of());
        }

        try {
            String rawJson = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/greenhopper/1.0/rapid/charts/sprintreport")
                            .queryParam("rapidViewId", getEffectiveBoardId())
                            .queryParam("sprintId", sprintId)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        log.warn("Sprint Report API not available: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (rawJson == null) return new SprintReportData(Set.of(), Set.of());

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(rawJson);
            com.fasterxml.jackson.databind.JsonNode contents = root.path("contents");

            // Issues added mid-sprint
            Set<String> addedKeys = new java.util.HashSet<>();
            com.fasterxml.jackson.databind.JsonNode addedDuringNode = contents.path("issueKeysAddedDuringSprint");
            if (addedDuringNode.isObject()) {
                addedDuringNode.fieldNames().forEachRemaining(addedKeys::add);
            }

            // Issues completed at sprint close time (authoritative for closed sprints)
            Set<String> completedKeys = new java.util.HashSet<>();
            com.fasterxml.jackson.databind.JsonNode completedNode = contents.path("completedIssues");
            if (completedNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode issueNode : completedNode) {
                    String key = issueNode.path("key").asText(null);
                    if (key != null) completedKeys.add(key);
                }
            }

            log.debug("Sprint Report: {} added mid-sprint, {} completed at close",
                    addedKeys.size(), completedKeys.size());
            return new SprintReportData(addedKeys, completedKeys);
        } catch (Exception e) {
            log.warn("Failed to fetch sprint report: {}", e.getMessage());
            return new SprintReportData(Set.of(), Set.of());
        }
    }

    /**
     * Batch-fetch epic details (summary + parent) for all epic keys found via Greenhopper mapping.
     * Returns epicKey → topic name. If the epic has a parent, uses parent's summary.
     */
    private Map<String, String> resolveEpicNames(WebClient client, Map<String, String> issueToEpicKey, List<JiraApiResponse.Issue> issues) {
        Set<String> epicKeys = issues.stream()
                .map(i -> issueToEpicKey.get(i.getKey()))
                .filter(k -> k != null && !k.isBlank())
                .collect(Collectors.toSet());

        if (epicKeys.isEmpty()) {
            log.debug("No epic keys to resolve");
            return Map.of();
        }

        log.debug("Resolving {} epic names", epicKeys.size());
        String jql = "key in (" + String.join(",", epicKeys) + ")";

        try {
            String rawJson = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/api/2/search")
                            .queryParam("jql", jql)
                            .queryParam("fields", "summary,parent")
                            .queryParam("maxResults", epicKeys.size())
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (rawJson == null) return Map.of();

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(rawJson);
            com.fasterxml.jackson.databind.JsonNode issuesNode = root.path("issues");

            Map<String, String> epicNames = new LinkedHashMap<>();
            if (issuesNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode epic : issuesNode) {
                    String key = epic.path("key").asText();
                    String epicSummary = epic.path("fields").path("summary").asText("");

                    // If the epic has a parent, use the parent's name as the topic
                    String parentSummary = epic.path("fields").path("parent")
                            .path("fields").path("summary").asText("");

                    String topicName = !parentSummary.isBlank() ? parentSummary : epicSummary;

                    if (!key.isEmpty() && !topicName.isBlank()) {
                        epicNames.put(key, topicName);
                    }
                }
            }
            log.info("Resolved {} epic names", epicNames.size());
            return epicNames;
        } catch (Exception e) {
            log.warn("Failed to resolve epic names: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Helper to wrap WebClient calls and map errors to domain exceptions.
     * Transforms WebClientResponseException into specific Jira exceptions.
     */
    private String callJiraApi(Mono<String> apiCall, String endpoint) {
        try {
            return apiCall.block();
        } catch (WebClientResponseException.Unauthorized e) {
            log.error("Jira authentication failed on {}: 401 Unauthorized", endpoint);
            Optional<JiraConfiguration> config = resolveDbConfig();
            if (config.isPresent()) {
                throw new JiraAuthenticationException("invalid token", config.get().getBaseUrl());
            }
            throw new JiraAuthenticationException("Authentication failed");
        } catch (WebClientResponseException.Forbidden e) {
            log.error("Jira permission denied on {}: 403 Forbidden", endpoint);
            Optional<JiraConfiguration> config = resolveDbConfig();
            if (config.isPresent()) {
                throw new JiraPermissionException(config.get().getProjectKey(), String.valueOf(config.get().getBoardId()));
            }
            throw new JiraPermissionException("Permission denied");
        } catch (WebClientResponseException e) {
            log.error("Jira API error on {}: HTTP {}", endpoint, e.getStatusCode().value());
            throw new JiraApiException(e.getStatusCode().value(), e.getStatusText(), endpoint);
        } catch (Exception e) {
            log.error("Jira connection error on {}: {}", endpoint, e.getMessage());
            Optional<JiraConfiguration> config = resolveDbConfig();
            throw new JiraConnectionException(
                    config.map(JiraConfiguration::getBaseUrl).orElse("unknown"),
                    e
            );
        }
    }
}