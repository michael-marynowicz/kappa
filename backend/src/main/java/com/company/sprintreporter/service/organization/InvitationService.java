package com.company.sprintreporter.service.organization;

import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.Dashboard;
import com.company.sprintreporter.domain.entity.Invitation;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.entity.enums.InvitationStatus;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import com.company.sprintreporter.application.dto.invitation.BulkInviteResultDto;
import com.company.sprintreporter.infrastructure.persistence.DashboardRepository;
import com.company.sprintreporter.infrastructure.persistence.InvitationRepository;
import com.company.sprintreporter.infrastructure.persistence.SubscriptionRepository;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import com.company.sprintreporter.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final DashboardRepository dashboardRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Optional<Invitation> createInvitation(UUID orgId, String email, UserRole role, UUID invitedById, UserRole inviterRole, List<UUID> dashboardIds) {
        UserRole effectiveRole = normalizeRequestedRole(role);
        assertInviterCanAssignRole(inviterRole, effectiveRole);

        var subscription = subscriptionRepository.findByOrganizationIdWithPlan(orgId)
                .orElseThrow(() -> new BusinessException("No subscription found", HttpStatus.INTERNAL_SERVER_ERROR));

        long currentMembers = userRepository.countByOrganizationId(orgId);
        long pendingInvitations = invitationRepository.countByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING);
        int maxMembers = subscription.getPlan().getMaxMembers();

        if (maxMembers != -1 && currentMembers + pendingInvitations >= maxMembers) {
            throw new BusinessException(
                    "Member limit reached for your plan (" + maxMembers + " max). Upgrade to add more members.",
                    HttpStatus.PAYMENT_REQUIRED);
        }

        if (invitationRepository.existsByOrganizationIdAndEmailAndStatus(orgId, email, InvitationStatus.PENDING)) {
            throw new BusinessException("This email has already been invited", HttpStatus.CONFLICT);
        }

        Organization orgRef = new Organization();
        orgRef.setId(orgId);

        AppUser inviterRef = new AppUser();
        inviterRef.setId(invitedById);

        Set<Dashboard> dashboards = resolveDashboards(orgId, dashboardIds);

        Optional<AppUser> existingMember = userRepository.findByEmailAndOrganizationId(email, orgId);
        if (existingMember.isPresent()) {
            AppUser member = existingMember.get();
            member.getDashboards().addAll(dashboards);
            userRepository.save(member);
            return Optional.empty();
        }

        Invitation invitation = Invitation.builder()
                .organization(orgRef)
                .email(email.toLowerCase().trim())
                .role(effectiveRole)
                .status(InvitationStatus.PENDING)
                .invitedBy(inviterRef)
            .dashboards(dashboards)
                .build();

        return Optional.of(invitationRepository.save(invitation));
    }

    /**
     * Creates invitations in bulk. Skips invalid, already-pending, and already-member emails
     * instead of throwing — returns a summary of what happened.
     * Callers are responsible for sending invitation emails for the returned list.
     */
    @Transactional
    public BulkInviteResult createBulkInvitations(UUID orgId, List<String> rawEmails, UserRole role, UUID invitedById, UserRole inviterRole, List<UUID> dashboardIds) {
        Organization orgRef = new Organization();
        orgRef.setId(orgId);

        AppUser inviterRef = new AppUser();
        inviterRef.setId(invitedById);

        Set<Dashboard> dashboards = resolveDashboards(orgId, dashboardIds);

        UserRole effectiveRole = normalizeRequestedRole(role);
        assertInviterCanAssignRole(inviterRole, effectiveRole);

        int invited = 0, alreadyPending = 0, alreadyMember = 0, invalid = 0;
        List<Invitation> created = new ArrayList<>();

        for (String raw : rawEmails) {
            if (raw == null || raw.isBlank() || !raw.contains("@")) {
                invalid++;
                continue;
            }
            String email = raw.toLowerCase().trim();

            if (invitationRepository.existsByOrganizationIdAndEmailAndStatus(orgId, email, InvitationStatus.PENDING)) {
                alreadyPending++;
                continue;
            }
            Optional<AppUser> existingMember = userRepository.findByEmailAndOrganizationId(email, orgId);
            if (existingMember.isPresent()) {
                AppUser member = existingMember.get();
                member.getDashboards().addAll(new LinkedHashSet<>(dashboards));
                userRepository.save(member);
                alreadyMember++;
                continue;
            }

            Invitation invitation = Invitation.builder()
                    .organization(orgRef)
                    .email(email)
                    .role(effectiveRole)
                    .status(InvitationStatus.PENDING)
                    .invitedBy(inviterRef)
                    .dashboards(new LinkedHashSet<>(dashboards))
                    .build();

            created.add(invitationRepository.save(invitation));
            invited++;
        }

        return new BulkInviteResult(created,
                BulkInviteResultDto.builder()
                        .invited(invited)
                        .alreadyPending(alreadyPending)
                        .alreadyMember(alreadyMember)
                        .invalid(invalid)
                        .build());
    }

    private UserRole normalizeRequestedRole(UserRole role) {
        return role != null ? role : UserRole.MEMBER;
    }

    private void assertInviterCanAssignRole(UserRole inviterRole, UserRole targetRole) {
        if (inviterRole == UserRole.ADMIN) {
            return;
        }
        if (inviterRole == UserRole.MEMBER && targetRole != UserRole.ADMIN) {
            return;
        }

        throw new BusinessException("Members cannot invite admins", HttpStatus.FORBIDDEN);
    }

    /** Carrier for both the saved invitations (for email sending) and the summary DTO. */
    public record BulkInviteResult(List<Invitation> invitations, BulkInviteResultDto summary) {}

    public List<Invitation> getPendingInvitations(UUID orgId) {
        return invitationRepository.findByOrganizationIdAndStatusWithDashboards(orgId, InvitationStatus.PENDING);
    }

    @Transactional
    public void revokeInvitation(UUID orgId, UUID invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException("Invitation not found", HttpStatus.NOT_FOUND));

        if (!invitation.getOrganization().getId().equals(orgId)) {
            throw new BusinessException("Invitation does not belong to this organization", HttpStatus.FORBIDDEN);
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("Only pending invitations can be revoked", HttpStatus.BAD_REQUEST);
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);
    }

    /**
     * Called during email verification to check if a pending invitation exists
     * and accept it (associate user with the inviting organization).
     */
    @Transactional
    public Optional<Invitation> acceptInvitationForEmail(String email) {
        Optional<Invitation> pendingInvitation = invitationRepository.findByEmailAndStatusWithDashboards(email, InvitationStatus.PENDING);
        pendingInvitation.ifPresent(invitation -> {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(Instant.now());
            invitationRepository.save(invitation);
        });
        return pendingInvitation;
    }

    private Set<Dashboard> resolveDashboards(UUID orgId, List<UUID> dashboardIds) {
        if (dashboardIds == null || dashboardIds.isEmpty()) {
            return new LinkedHashSet<>(dashboardRepository.findByOrganizationIdOrderByPositionAsc(orgId));
        }

        List<UUID> uniqueIds = dashboardIds.stream().distinct().toList();
        List<Dashboard> dashboards = dashboardRepository.findByOrganizationIdAndIdIn(orgId, uniqueIds);
        if (dashboards.size() != uniqueIds.size()) {
            throw new BusinessException("One or more dashboards were not found in your organization", HttpStatus.BAD_REQUEST);
        }

        return new LinkedHashSet<>(dashboards);
    }
}
