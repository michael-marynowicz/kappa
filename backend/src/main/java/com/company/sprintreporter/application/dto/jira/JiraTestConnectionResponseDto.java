package com.company.sprintreporter.application.dto.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JiraTestConnectionResponseDto {

    private boolean success;
    private String message;
}
