package com.company.sprintreporter.application.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FeatureAccessResponseDto {

    private String featureCode;
    private boolean hasAccess;
}
