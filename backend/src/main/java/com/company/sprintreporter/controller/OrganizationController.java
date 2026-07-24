package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.organization.InviteMemberRequestDto;
import com.company.sprintreporter.application.dto.organization.MemberResponseDto;
import com.company.sprintreporter.application.dto.organization.OrganizationResponseDto;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.service.organization.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/current")
    public ResponseEntity<OrganizationResponseDto> getCurrent() {
        var auth = getAuth();
        Organization org = organizationService.getById(auth.getOrganizationId());
        return ResponseEntity.ok(OrganizationResponseDto.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .email(org.getEmail())
                .logoUrl(org.getLogoUrl())
                .active(org.getActive())
                .build());
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponseDto>> getMembers() {
        var auth = getAuth();
        List<AppUser> members = organizationService.getMembers(auth.getOrganizationId());
        return ResponseEntity.ok(members.stream().map(this::toDto).toList());
    }

    @PostMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponseDto> inviteMember(@Valid @RequestBody InviteMemberRequestDto request) {
        var auth = getAuth();
        AppUser user = organizationService.inviteMember(
                auth.getOrganizationId(),
                request.getEmail(), request.getFirstName(), request.getLastName(),
                request.getRole(), request.getTempPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(user));
    }

    @DeleteMapping("/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeMember(@PathVariable UUID userId) {
        var auth = getAuth();
        organizationService.removeMember(auth.getOrganizationId(), userId);
        return ResponseEntity.noContent().build();
    }

    private MemberResponseDto toDto(AppUser user) {
        return MemberResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .dashboardIds(user.getDashboards().stream().map(d -> d.getId()).toList())
                .build();
    }

    private JwtAuthenticationToken getAuth() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
}
