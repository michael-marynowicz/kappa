error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/controller/IssueControllerTest.java:_empty_/`<any>`#thenCallRealMethod#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/controller/IssueControllerTest.java
empty definition using pc, found symbol in pc: _empty_/`<any>`#thenCallRealMethod#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1916
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/controller/IssueControllerTest.java
text:
```scala
package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.mapper.SprintIssueMapper;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.service.SprintIssueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IssueController.class)
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

    private static final SprintIssue SAMPLE_ISSUE = SprintIssue.builder()
            .issueKey("SCRUM-1")
            .summary("Test story")
            .status("In Progress")
            .assignee("Alice")
            .issueType("Story")
            .totalStoryPoints(8)
            .remainingStoryPoints(3)
            .build();

    @Test
    @DisplayName("GET /api/v1/issues should return 200 with issue list")
    void getIssuesReturns200() throws Exception {
        when(sprintIssueService.getSprintIssues()).thenReturn(List.of(SAMPLE_ISSUE));
        when(mapper.toResponseDtoList(any())).@@thenCallRealMethod();

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

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/`<any>`#thenCallRealMethod#