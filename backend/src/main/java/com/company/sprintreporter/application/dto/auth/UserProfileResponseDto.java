package com.company.sprintreporter.application.dto.auth;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

/**
 * Full user profile response returned by GET /api/v1/auth/me.
 * Includes granted feature permissions derived from the organization's active subscription plan.
 * The frontend uses this as the single source of truth for UI feature gating.
 */
@Data
@Builder
@AllArgsConstructor
public class UserProfileResponseDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UUID organizationId;
    private String organizationName;
    private UserRole role;
    private SubscriptionInfo subscription;
    private Set<String> permissions;
    private boolean jiraConnected;

    @Data
    @Builder
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private String planCode;
        private String planName;
        private String status;
    }
}
