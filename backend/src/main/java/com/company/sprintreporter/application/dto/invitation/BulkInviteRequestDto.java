package com.company.sprintreporter.application.dto.invitation;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkInviteRequestDto {

    @NotEmpty
    @Size(max = 2000, message = "Cannot invite more than 2000 people at once")
    private List<String> emails;

    private UserRole role;

    /** Optional: dashboards these users are being invited to work on. */
    private List<UUID> dashboardIds;
}
