package com.company.sprintreporter.service;

import com.company.sprintreporter.domain.entity.*;
import com.company.sprintreporter.domain.entity.enums.InvitationStatus;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import com.company.sprintreporter.infrastructure.persistence.EmailVerificationRepository;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import com.company.sprintreporter.service.auth.EmailVerificationService;
import com.company.sprintreporter.service.email.EmailService;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationService")
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvitationService invitationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService service;

    private AppUser user;
    private Organization personalOrg;

    @BeforeEach
    void setUp() {
        personalOrg = Organization.builder().id(UUID.randomUUID()).name("Personal").slug("personal").build();
        user = AppUser.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .organization(personalOrg)
                .role(UserRole.ADMIN)
                .emailVerified(false)
                .build();
    }

    @Nested
    @DisplayName("createVerificationToken()")
    class CreateVerificationToken {

        @Test
        @DisplayName("should generate a token and persist hashed version")
        void generatesTokenAndPersists() {
            when(emailVerificationRepository.save(any(EmailVerification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            String rawToken = service.createVerificationToken(user);

            assertThat(rawToken).isNotBlank();
            ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
            verify(emailVerificationRepository).save(captor.capture());
            assertThat(captor.getValue().getTokenHash()).isNotEqualTo(rawToken);
            assertThat(captor.getValue().getUser()).isEqualTo(user);
            assertThat(captor.getValue().getExpiresAt()).isAfter(Instant.now().minus(1, ChronoUnit.MINUTES));
        }
    }

    @Nested
    @DisplayName("verifyEmail()")
    class VerifyEmail {

        @Test
        @DisplayName("should verify email and keep user in personal org when no invitation")
        void verifiesWithoutInvitation() {
            EmailVerification verification = EmailVerification.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .tokenHash("hashed")
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();

            when(emailVerificationRepository.findByTokenHashAndVerifiedAtIsNull(anyString()))
                    .thenReturn(Optional.of(verification));
            when(invitationService.acceptInvitationForEmail("user@example.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.verifyEmail("some-raw-token");

            assertThat(result.joinedOrganization()).isFalse();
            assertThat(result.organizationId()).isEqualTo(personalOrg.getId());
            assertThat(user.getEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("should verify email and reassign user to inviting org when invitation exists")
        void verifiesAndJoinsOrganization() {
            Organization acmeOrg = Organization.builder().id(UUID.randomUUID()).name("Acme").slug("acme").build();
            Invitation invitation = Invitation.builder()
                    .id(UUID.randomUUID())
                    .organization(acmeOrg)
                    .email("user@example.com")
                    .role(UserRole.MEMBER)
                    .status(InvitationStatus.ACCEPTED)
                    .build();

            EmailVerification verification = EmailVerification.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .tokenHash("hashed")
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();

            when(emailVerificationRepository.findByTokenHashAndVerifiedAtIsNull(anyString()))
                    .thenReturn(Optional.of(verification));
            when(invitationService.acceptInvitationForEmail("user@example.com"))
                    .thenReturn(Optional.of(invitation));
            when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.verifyEmail("some-raw-token");

            assertThat(result.joinedOrganization()).isTrue();
            assertThat(result.organizationId()).isEqualTo(acmeOrg.getId());
            assertThat(result.role()).isEqualTo(UserRole.MEMBER);
            assertThat(user.getOrganization()).isEqualTo(acmeOrg);
        }

        @Test
        @DisplayName("should reject expired token")
        void rejectsExpiredToken() {
            EmailVerification verification = EmailVerification.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .tokenHash("hashed")
                    .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .build();

            when(emailVerificationRepository.findByTokenHashAndVerifiedAtIsNull(anyString()))
                    .thenReturn(Optional.of(verification));

            assertThatThrownBy(() -> service.verifyEmail("some-raw-token"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("should reject invalid token")
        void rejectsInvalidToken() {
            when(emailVerificationRepository.findByTokenHashAndVerifiedAtIsNull(anyString()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.verifyEmail("invalid-token"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid");
        }
    }
}
