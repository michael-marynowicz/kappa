package com.company.sprintreporter.application.dto.jira;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoardDto {
    private Integer id;
    private String name;
    private String projectKey;
    private String projectName;
    private String type;
}
