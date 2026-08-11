package com.company.sprintreporter.service.auth;

import com.company.sprintreporter.domain.entity.*;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import com.company.sprintreporter.infrastructure.persistence.EmailVerificationRepository;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import com.company.sprintreporter.service.email.EmailService;
import com.company.sprintreporter.service.exception.BusinessException;
import com.company.sprintreporter.service.organization.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final InvitationService invitationService;
    private final EmailService emailService;

    @Value("${app.email-verification.expiry-hours:24}")
    private long expiryHours;

    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    /**
     * Generates a verification token for the user and sends verification email.
     */
    @Transactional
    public String createVerificationToken(AppUser user) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(Instant.now().plus(expiryHours, ChronoUnit.HOURS))
                .build();
        emailVerificationRepository.save(verification);

        // Send verification email
        String verificationUrl = buildVerificationUrl(rawToken);
        emailService.sendVerificationEmail(user, verificationUrl);

        return rawToken;
    }

    /**
     * Returns the verification URL to be sent via email.
     */
    public String buildVerificationUrl(String rawToken) {
        return baseUrl + "/verify-email?token=" + rawToken;
    }

    /**
     * Verifies the email token. If a pending invitation exists for this email,
     * the user is reassigned to the inviting organization with the specified role.
     */
    @Transactional
    public VerificationResult verifyEmail(String rawToken) {
        String tokenHash = hashToken(rawToken);
        EmailVerification verification = emailVerificationRepository.findByTokenHashAndVerifiedAtIsNull(tokenHash)
                .orElseThrow(() -> new BusinessException("Invalid or already used verification token", HttpStatus.BAD_REQUEST));

        if (verification.isExpired()) {
            throw new BusinessException("Verification token has expired", HttpStatus.GONE);
        }

        verification.setVerifiedAt(Instant.now());
        emailVerificationRepository.save(verification);

        AppUser user = verification.getUser();
        user.setEmailVerified(true);

        // Check if there's a pending invitation for this email
        Optional<Invitation> invitation = invitationService.acceptInvitationForEmail(user.getEmail());
        boolean joinedOrganization = false;
        UUID dashboardId = null;
        if (invitation.isPresent()) {
            Invitation inv = invitation.get();
            // Reassign user to the inviting organization
            user.setOrganization(inv.getOrganization());
            user.setRole(inv.getRole());
            user.setDashboards(inv.getDashboards());
            joinedOrganization = true;
            dashboardId = inv.getDashboards().stream()
                    .map(Dashboard::getId)
                    .sorted(Comparator.comparing(UUID::toString))
                    .findFirst()
                    .orElse(null);
        }

        userRepository.save(user);

        return new VerificationResult(user.getId(), user.getEmail(), joinedOrganization,
                user.getOrganization().getId(), user.getRole(), dashboardId);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public record VerificationResult(
            UUID userId, String email, boolean joinedOrganization,
            UUID organizationId, UserRole role, UUID dashboardId) {}
}
