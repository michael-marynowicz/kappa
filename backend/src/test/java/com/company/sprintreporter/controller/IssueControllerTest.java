package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.SprintIssueResponseDto;
import com.company.sprintreporter.application.mapper.SprintIssueMapper;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.service.SprintIssueService;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IssueController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("IssueController")
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SprintIssueService sprintIssueService;

    @MockBean
    private SprintIssueMapper mapper;

    @MockBean
    private com.company.sprintreporter.config.jwt.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    private static final UUID ORG_ID = UUID.randomUUID();

    private static final SprintIssue SAMPLE_ISSUE = SprintIssue.builder()
            .issueKey("SCRUM-1")
            .summary("Test story")
            .status("In Progress")
            .assignee("Alice")
            .issueType("Story")
            .totalStoryPoints(8)
            .remainingStoryPoints(3)
            .build();

    @BeforeEach
    void setUp() {
        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(token.getOrganizationId()).thenReturn(ORG_ID);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("GET /api/v1/issues should return 200 with issue list")
    void getIssuesReturns200() throws Exception {
        when(sprintIssueService.getSprintIssues(any(UUID.class))).thenReturn(List.of(SAMPLE_ISSUE));
        when(mapper.toResponseDtoList(any())).thenReturn(List.of(
                SprintIssueResponseDto.builder()
                        .issueKey(SAMPLE_ISSUE.getIssueKey())
                        .summary(SAMPLE_ISSUE.getSummary())
                        .status(SAMPLE_ISSUE.getStatus())
                        .assignee(SAMPLE_ISSUE.getAssignee())
                        .issueType(SAMPLE_ISSUE.getIssueType())
                        .totalStoryPoints(SAMPLE_ISSUE.getTotalStoryPoints())
                        .remainingStoryPoints(SAMPLE_ISSUE.getRemainingStoryPoints())
                        .doneStoryPoints(SAMPLE_ISSUE.getDoneStoryPoints())
                        .build()));

        mockMvc.perform(get("/api/v1/issues"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/v1/issues/update should return 422 when issueKey is blank")
    void updateReturns422WhenIssueKeyBlank() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("issueKey", "", "remainingStoryPoints", 3));

        mockMvc.perform(post("/api/v1/issues/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/v1/issues/update should return 422 when remainingStoryPoints is negative")
    void updateReturns422WhenRemainingNegative() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("issueKey", "SCRUM-1", "remainingStoryPoints", -1));

        mockMvc.perform(post("/api/v1/issues/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }
}
