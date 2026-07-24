package com.company.sprintreporter.application.dto.jira;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveMyJiraCredentialsRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
