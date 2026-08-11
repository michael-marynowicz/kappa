package com.company.sprintreporter.application.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePlanRequestDto {

    @NotBlank
    private String planCode;
}
