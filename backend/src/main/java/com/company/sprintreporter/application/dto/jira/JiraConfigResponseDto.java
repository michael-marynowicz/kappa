package com.company.sprintreporter.application.dto.jira;

import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class JiraConfigResponseDto {

    private UUID id;
    private String baseUrl;
    private JiraAuthType authType;
    private String userEmail;
    private String projectKey;
    private Integer boardId;
    private Boolean active;
    private Instant lastSyncAt;
}
