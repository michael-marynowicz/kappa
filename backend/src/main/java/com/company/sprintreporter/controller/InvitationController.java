package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.invitation.BulkInviteRequestDto;
import com.company.sprintreporter.application.dto.invitation.BulkInviteResultDto;
import com.company.sprintreporter.application.dto.invitation.CreateInvitationRequestDto;
import com.company.sprintreporter.application.dto.invitation.InvitationResponseDto;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.entity.Invitation;
import com.company.sprintreporter.service.email.EmailService;
import java.util.Optional;
import com.company.sprintreporter.service.organization.InvitationService;
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
@RequestMapping("/api/v1/organization/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final EmailService emailService;
    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<InvitationResponseDto> create(@Valid @RequestBody CreateInvitationRequestDto request) {
        var auth = getAuth();
        Optional<Invitation> invitationOpt = invitationService.createInvitation(
                auth.getOrganizationId(),
                request.getEmail(),
                request.getRole(),
                auth.getUserId(),
                auth.getRole(),
            request.getDashboardIds());

        if (invitationOpt.isEmpty()) {
            // User already a member — dashboards updated directly, no email needed
            return ResponseEntity.noContent().build();
        }

        Invitation invitation = invitationOpt.get();
        // Send invitation email
        String orgName = organizationService.getById(auth.getOrganizationId()).getName();
        emailService.sendInvitationEmail(invitation, orgName);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(invitation));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationResponseDto>> listPending() {
        var auth = getAuth();
        List<Invitation> invitations = invitationService.getPendingInvitations(auth.getOrganizationId());
        return ResponseEntity.ok(invitations.stream().map(this::toDto).toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        var auth = getAuth();
        invitationService.revokeInvitation(auth.getOrganizationId(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk invite: accepts a list of emails and sends invitations to all of them.
     * Skips duplicates and already-members instead of failing.
     * Emails are sent asynchronously so the response is immediate even for large lists.
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<BulkInviteResultDto> bulkInvite(@Valid @RequestBody BulkInviteRequestDto request) {
        var auth = getAuth();
        String orgName = organizationService.getById(auth.getOrganizationId()).getName();

        InvitationService.BulkInviteResult result = invitationService.createBulkInvitations(
                auth.getOrganizationId(),
                request.getEmails(),
                request.getRole(),
            auth.getUserId(),
            auth.getRole(),
            request.getDashboardIds());

        // Send emails asynchronously — @Async in EmailService so this returns immediately
        result.invitations().forEach(inv -> emailService.sendInvitationEmail(inv, orgName));

        return ResponseEntity.status(HttpStatus.CREATED).body(result.summary());
    }

    private InvitationResponseDto toDto(Invitation invitation) {
        return InvitationResponseDto.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .dashboards(invitation.getDashboards().stream()
                        .map(d -> new InvitationResponseDto.DashboardSummary(d.getId(), d.getName()))
                        .toList())
                .createdAt(invitation.getCreatedAt())
                .build();
    }

    private JwtAuthenticationToken getAuth() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
}
