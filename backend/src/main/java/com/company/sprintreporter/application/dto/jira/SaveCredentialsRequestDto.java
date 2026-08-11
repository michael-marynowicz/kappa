package com.company.sprintreporter.application.dto.jira;

import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveCredentialsRequestDto {

    @NotBlank
    private String baseUrl;

    private JiraAuthType authType = JiraAuthType.PAT;

    private String userEmail;

    @NotBlank
    private String token;
}
