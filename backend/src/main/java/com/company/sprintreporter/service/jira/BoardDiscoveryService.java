package com.company.sprintreporter.service.jira;

import com.company.sprintreporter.application.dto.jira.BoardDiscoveryRequestDto;
import com.company.sprintreporter.application.dto.jira.BoardDto;
import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import com.company.sprintreporter.service.exception.JiraAuthenticationException;
import com.company.sprintreporter.service.exception.JiraConnectionException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BoardDiscoveryService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Calls /rest/agile/1.0/board on the given Jira instance and returns all boards
     * accessible with the provided credentials.
     */
    public List<BoardDto> discoverBoards(BoardDiscoveryRequestDto request) {
        String authHeader = buildAuthHeader(request);

        // Normalize baseUrl: strip trailing /agile or /rest/... suffix if present
        String baseUrl = normalizeBaseUrl(request.getBaseUrl());

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", authHeader)
                .defaultHeader("Accept", "application/json")
                .build();

        try {
            return fetchAllBoards(client, baseUrl, request.getNameFilter());
        } catch (WebClientResponseException.Unauthorized e) {
            throw new JiraAuthenticationException(request.getToken(), baseUrl);
        } catch (WebClientResponseException e) {
            throw new JiraConnectionException(baseUrl, e);
        } catch (IllegalStateException e) {
            // .block() timeout — server unreachable or too slow
            throw new JiraConnectionException(baseUrl,
                    new RuntimeException("Request timed out after 10s — check that the Jira URL is correct and reachable: " + baseUrl));
        } catch (Exception e) {
            throw new JiraConnectionException(baseUrl, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<BoardDto> fetchAllBoards(WebClient client, String baseUrl, String nameFilter) {
        List<BoardDto> result = new ArrayList<>();
        int startAt = 0;
        int maxResults = 50;
        boolean isLast = false;

        while (!isLast) {
            final int start = startAt;
            final String filter = nameFilter;
            String json = client.get()
                    .uri(u -> {
                        var builder = u.path("/rest/agile/1.0/board")
                                .queryParam("startAt", start)
                                .queryParam("maxResults", maxResults)
                                .queryParam("type", "scrum");
                        if (filter != null && !filter.isBlank()) {
                            builder = builder.queryParam("name", filter);
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(30));

            Map<String, Object> page = parseJson(json);
            List<Map<String, Object>> values = (List<Map<String, Object>>) page.get("values");
            if (values == null || values.isEmpty()) break;

            for (Map<String, Object> board : values) {
                Map<String, Object> location = (Map<String, Object>) board.get("location");
                String projectKey = location != null ? (String) location.get("projectKey") : null;
                String projectName = location != null ? (String) location.get("projectName") : null;
                result.add(BoardDto.builder()
                        .id((Integer) board.get("id"))
                        .name((String) board.get("name"))
                        .type((String) board.get("type"))
                        .projectKey(projectKey)
                        .projectName(projectName)
                        .build());
            }

            Boolean last = (Boolean) page.get("isLast");
            isLast = Boolean.TRUE.equals(last) || values.size() < maxResults;
            startAt += values.size();
        }

        log.info("Discovered {} scrum boards at {}", result.size(), baseUrl);
        return result;
    }

    private String buildAuthHeader(BoardDiscoveryRequestDto request) {
        if (request.getAuthType() == JiraAuthType.BASIC) {
            String credentials = request.getUserEmail() + ":" + request.getToken();
            return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        }
        return "Bearer " + request.getToken();
    }

    /**
     * Strips known Jira API path suffixes so we always call from the root.
     * e.g. "https://jira.company.com/agile" -> "https://jira.company.com"
     */
    private String normalizeBaseUrl(String url) {
        if (url == null) return url;
        url = url.stripTrailing();
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        if (url.endsWith("/agile")) url = url.substring(0, url.length() - 6);
        if (url.endsWith("/rest")) url = url.substring(0, url.length() - 5);
        return url;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Jira board response", e);
        }
    }
}
