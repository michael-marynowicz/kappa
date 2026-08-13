package com.company.sprintreporter.service.auth;

import com.company.sprintreporter.config.jwt.JwtTokenProvider;
import com.company.sprintreporter.domain.entity.*;
import com.company.sprintreporter.domain.entity.enums.InvitationStatus;
import com.company.sprintreporter.domain.entity.enums.SubscriptionStatus;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import com.company.sprintreporter.infrastructure.persistence.*;
import com.company.sprintreporter.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;

    @Value("${jwt.refresh-token-days:30}")
    private long refreshTokenDays;

    @Transactional
    public AuthResult register(String email, String password, String firstName, String lastName, String organizationName) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT);
        }

        // Check if there's a pending invitation for this email
        Optional<Invitation> pendingInvitation = invitationRepository.findByEmailAndStatus(
                email.toLowerCase().trim(), InvitationStatus.PENDING);

        Organization org;
        UserRole userRole;

        if (pendingInvitation.isPresent()) {
            // User was invited: join the inviting organization directly
            Invitation inv = pendingInvitation.get();
            org = inv.getOrganization();
            userRole = inv.getRole();

            // Mark invitation as accepted
            inv.setStatus(InvitationStatus.ACCEPTED);
            inv.setAcceptedAt(Instant.now());
            invitationRepository.save(inv);
        } else {
            // No invitation: create a new organization + free plan
            String slug = generateSlug(organizationName);
            org = Organization.builder()
                    .name(organizationName)
                    .slug(slug)
                    .email(email)
                    .build();
            org = organizationRepository.save(org);
            userRole = UserRole.ADMIN;

            // Create free plan subscription
            var freePlan = planRepository.findByCode("free")
                    .orElseThrow(() -> new BusinessException("Free plan not found", HttpStatus.INTERNAL_SERVER_ERROR));

            Subscription subscription = Subscription.builder()
                    .organization(org)
                    .plan(freePlan)
                    .status(SubscriptionStatus.ACTIVE)
                    .currentPeriodStart(Instant.now())
                    .build();
            subscriptionRepository.save(subscription);
        }

        // Create user
        AppUser user = AppUser.builder()
                .organization(org)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(userRole)
                .build();
        user = userRepository.save(user);

        // Generate email verification token (sends email)
        emailVerificationService.createVerificationToken(user);

        // Do NOT return tokens — user must verify email first
        return null;
    }

    @Transactional
    public AuthResult login(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (!user.getEnabled()) {
            throw new BusinessException("Account disabled", HttpStatus.FORBIDDEN);
        }

        if (!user.getEmailVerified()) {
            throw new BusinessException("Please verify your email before logging in. Check your inbox for the confirmation link.", HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getOrganization().getId(), user.getRole());
        String refreshToken = generateRefreshToken(user);

        return buildResult(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (stored.isExpired()) {
            throw new BusinessException("Refresh token expired", HttpStatus.UNAUTHORIZED);
        }

        // Revoke old token (rotation)
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        AppUser user = stored.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getOrganization().getId(), user.getRole());
        String newRefreshToken = generateRefreshToken(user);

        return buildResult(accessToken, newRefreshToken, user);
    }

    public AppUser getMe(UUID userId) {
        return userRepository.findByIdWithOrganization(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }

    public AppUser getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private String generateRefreshToken(AppUser user) {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(entity);

        return rawToken;
    }

    private String hashToken(String rawToken) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String generateSlug(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        String slug = base;
        int counter = 1;
        while (organizationRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    private AuthResult buildResult(String accessToken, String refreshToken, AppUser user) {
        return new AuthResult(
                accessToken, refreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                user.getId(), user.getOrganization().getId(),
                user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole());
    }

    public record AuthResult(
            String accessToken, String refreshToken, long expiresIn,
            UUID userId, UUID organizationId,
            String email, String firstName, String lastName,
            UserRole role) {}
}
