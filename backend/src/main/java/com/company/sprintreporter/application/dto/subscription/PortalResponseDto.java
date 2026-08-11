package com.company.sprintreporter.application.dto.subscription;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class PortalResponseDto {
    private final String portalUrl;
}
