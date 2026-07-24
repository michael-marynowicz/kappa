package com.company.sprintreporter.application.dto.auth;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserDto user;

    @Data
    @Builder
    @AllArgsConstructor
    public static class UserDto {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private UUID organizationId;
        private UserRole role;
    }
}
