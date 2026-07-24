package com.company.sprintreporter.application.dto.organization;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteMemberRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private UserRole role;

    @NotBlank
    private String tempPassword;
}
