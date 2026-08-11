package com.company.sprintreporter.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamMemberDto {
    private final String id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private final String name;

    @NotBlank(message = "Role must not be blank")
    @Pattern(regexp = "DEV|PDA|QA|SM", message = "Role must be DEV, PDA, QA or SM")
    private final String role;

    @Builder.Default
    private final double timeOverride = 1.0;
}
