package com.company.sprintreporter.application.dto.invitation;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateInvitationRequestDto {

    @NotBlank
    @Email
    private String email;

    private UserRole role;

    /** Optional: dashboards this user is being invited to work on. */
    private List<UUID> dashboardIds;
}
