package com.company.sprintreporter.application.dto.jira;

import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import lombok.Data;

@Data
public class BoardDiscoveryRequestDto {
    private String baseUrl;
    private JiraAuthType authType;
    private String userEmail;
    private String token;
    /** Optional: filter boards by name (e.g. "ROC") to reduce results on large instances */
    private String nameFilter;
}
