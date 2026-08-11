package com.company.sprintreporter.application.dto.organization;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MemberResponseDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Boolean enabled;
    private List<UUID> dashboardIds;
}
