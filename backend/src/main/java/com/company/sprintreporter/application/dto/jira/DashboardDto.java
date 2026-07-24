package com.company.sprintreporter.application.dto.jira;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DashboardDto {
    private UUID id;
    private String name;
    private Integer boardId;
    private String projectKey;
    private Boolean active;
    private Integer position;
    private Instant createdAt;
}
