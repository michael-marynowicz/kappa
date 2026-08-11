package com.company.sprintreporter.service;

import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.Dashboard;
import com.company.sprintreporter.domain.entity.Invitation;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.entity.Subscription;
import com.company.sprintreporter.domain.entity.enums.InvitationStatus;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import com.company.sprintreporter.infrastructure.persistence.DashboardRepository;
import com.company.sprintreporter.infrastructure.persistence.InvitationRepository;
import com.company.sprintreporter.infrastructure.persistence.SubscriptionRepository;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import com.company.sprintreporter.service.exception.BusinessException;
import com.company.sprintreporter.service.organization.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvitationService")
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InvitationService service;

    private UUID orgId;
    private UUID adminUserId;
    private Organization org;
    private AppUser adminUser;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        org = Organization.builder().id(orgId).name("Acme Corp").slug("acme-corp").build();
        adminUser = AppUser.builder().id(adminUserId).organization(org).email("admin@acme.com").role(UserRole.ADMIN).build();

        var plan = com.company.sprintreporter.domain.entity.Plan.builder()
                .id(UUID.randomUUID()).code("pro").maxMembers(20).build();
        subscription = Subscription.builder()
                .id(UUID.randomUUID()).organization(org).plan(plan).build();
    }

    @Nested
    @DisplayName("createInvitation()")
    class CreateInvitation {

        @Test
        @DisplayName("should create an invitation for a valid email")
        void createsInvitation() {
            when(subscriptionRepository.findByOrganizationIdWithPlan(orgId)).thenReturn(Optional.of(subscription));
            when(userRepository.countByOrganizationId(orgId)).thenReturn(2L);
            when(invitationRepository.countByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING)).thenReturn(0L);
            when(invitationRepository.existsByOrganizationIdAndEmailAndStatus(orgId, "user@acme.com", InvitationStatus.PENDING)).thenReturn(false);
            when(dashboardRepository.findByOrganizationIdOrderByPositionAsc(orgId)).thenReturn(List.of());
            when(userRepository.findByEmailAndOrganizationId("user@acme.com", orgId)).thenReturn(Optional.empty());
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<Invitation> result = service.createInvitation(orgId, "user@acme.com", UserRole.MEMBER, adminUserId, UserRole.ADMIN, null);

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("user@acme.com");
            assertThat(result.get().getRole()).isEqualTo(UserRole.MEMBER);
            assertThat(result.get().getStatus()).isEqualTo(InvitationStatus.PENDING);
        }

        @Test
        @DisplayName("should reject if member limit reached (members + pending invitations)")
        void rejectWhenLimitReached() {
            when(subscriptionRepository.findByOrganizationIdWithPlan(orgId)).thenReturn(Optional.of(subscription));
            when(userRepository.countByOrganizationId(orgId)).thenReturn(18L);
            when(invitationRepository.countByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING)).thenReturn(2L);

                assertThatThrownBy(() -> service.createInvitation(orgId, "user@acme.com", UserRole.MEMBER, adminUserId, UserRole.ADMIN, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Member limit reached");
        }

        @Test
        @DisplayName("should reject if email already has a pending invitation for this org")
        void rejectDuplicatePendingInvitation() {
            when(subscriptionRepository.findByOrganizationIdWithPlan(orgId)).thenReturn(Optional.of(subscription));
            when(userRepository.countByOrganizationId(orgId)).thenReturn(2L);
            when(invitationRepository.countByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING)).thenReturn(0L);
            when(invitationRepository.existsByOrganizationIdAndEmailAndStatus(orgId, "user@acme.com", InvitationStatus.PENDING)).thenReturn(true);

                assertThatThrownBy(() -> service.createInvitation(orgId, "user@acme.com", UserRole.MEMBER, adminUserId, UserRole.ADMIN, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already been invited");
        }

        @Test
        @DisplayName("should grant dashboard access when email already belongs to the org")
        void grantsAccessWhenAlreadyMember() {
            AppUser existingMember = AppUser.builder().id(UUID.randomUUID()).organization(org)
                    .email("user@acme.com").role(UserRole.MEMBER).build();
            when(subscriptionRepository.findByOrganizationIdWithPlan(orgId)).thenReturn(Optional.of(subscription));
            when(userRepository.countByOrganizationId(orgId)).thenReturn(2L);
            when(invitationRepository.countByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING)).thenReturn(0L);
            when(invitationRepository.existsByOrganizationIdAndEmailAndStatus(orgId, "user@acme.com", InvitationStatus.PENDING)).thenReturn(false);
            when(dashboardRepository.findByOrganizationIdOrderByPositionAsc(orgId)).thenReturn(List.of());
            when(userRepository.findByEmailAndOrganizationId("user@acme.com", orgId)).thenReturn(Optional.of(existingMember));
            when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<Invitation> result = service.createInvitation(orgId, "user@acme.com", UserRole.MEMBER, adminUserId, UserRole.ADMIN, null);

            assertThat(result).isEmpty();
            verify(userRepository).save(existingMember);
        }

        @Test
        @DisplayName("should allow a member to invite a viewer")
        void memberCanInviteViewer() {
            when(subscriptionRepository.findByOrganizationIdWithPlan(orgId)).thenReturn(Optional.of(subscription));
            when(userRepository.countByOrganizationId(orgId)).thenReturn(2L);
            when(invitationRepository.countByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING)).thenReturn(0L);
            when(invitationRepository.existsByOrganizationIdAndEmailAndStatus(orgId, "viewer@acme.com", InvitationStatus.PENDING)).thenReturn(false);
            when(dashboardRepository.findByOrganizationIdOrderByPositionAsc(orgId)).thenReturn(List.of());
            when(userRepository.findByEmailAndOrganizationId("viewer@acme.com", orgId)).thenReturn(Optional.empty());
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<Invitation> result = service.createInvitation(orgId, "viewer@acme.com", UserRole.VIEWER, adminUserId, UserRole.MEMBER, null);

            assertThat(result).isPresent();
            assertThat(result.get().getRole()).isEqualTo(UserRole.VIEWER);
        }

        @Test
        @DisplayName("should reject when a member tries to invite an admin")
        void memberCannotInviteAdmin() {
            assertThatThrownBy(() -> service.createInvitation(orgId, "admin2@acme.com", UserRole.ADMIN, adminUserId, UserRole.MEMBER, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Members cannot invite admins");
        }
    }

    @Nested
    @DisplayName("revokeInvitation()")
    class RevokeInvitation {

        @Test
        @DisplayName("should revoke a pending invitation")
        void revokesInvitation() {
            Invitation invitation = Invitation.builder()
                    .id(UUID.randomUUID()).organization(org).email("user@acme.com")
                    .status(InvitationStatus.PENDING).build();
            when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

            service.revokeInvitation(orgId, invitation.getId());

            ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
            verify(invitationRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.REVOKED);
        }

        @Test
        @DisplayName("should reject revoking if invitation belongs to another org")
        void rejectWrongOrg() {
            UUID otherOrgId = UUID.randomUUID();
            Organization otherOrg = Organization.builder().id(otherOrgId).build();
            Invitation invitation = Invitation.builder()
                    .id(UUID.randomUUID()).organization(otherOrg).email("user@other.com")
                    .status(InvitationStatus.PENDING).build();
            when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

            assertThatThrownBy(() -> service.revokeInvitation(orgId, invitation.getId()))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getPendingInvitations()")
    class GetPendingInvitations {

        @Test
        @DisplayName("should return pending invitations for org")
        void returnsPendingInvitations() {
            Invitation inv1 = Invitation.builder().email("a@acme.com").status(InvitationStatus.PENDING).build();
            Invitation inv2 = Invitation.builder().email("b@acme.com").status(InvitationStatus.PENDING).build();
            when(invitationRepository.findByOrganizationIdAndStatusWithDashboards(orgId, InvitationStatus.PENDING))
                    .thenReturn(List.of(inv1, inv2));

            List<Invitation> result = service.getPendingInvitations(orgId);

            assertThat(result).hasSize(2);
        }
    }
}
