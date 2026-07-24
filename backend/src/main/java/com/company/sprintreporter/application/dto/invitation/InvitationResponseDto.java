package com.company.sprintreporter.application.dto.invitation;

import com.company.sprintreporter.domain.entity.enums.InvitationStatus;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class InvitationResponseDto {

    private UUID id;
    private String email;
    private UserRole role;
    private InvitationStatus status;
    private List<DashboardSummary> dashboards;
    private Instant createdAt;

    public record DashboardSummary(UUID id, String name) {}
}
