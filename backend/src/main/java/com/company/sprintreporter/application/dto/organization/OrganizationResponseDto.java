package com.company.sprintreporter.application.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class OrganizationResponseDto {

    private UUID id;
    private String name;
    private String slug;
    private String email;
    private String logoUrl;
    private Boolean active;
}
