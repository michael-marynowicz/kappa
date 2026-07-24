package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.auth.*;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.Subscription;
import com.company.sprintreporter.service.auth.AuthService;
import com.company.sprintreporter.service.auth.EmailVerificationService;
import com.company.sprintreporter.service.subscription.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SubscriptionService subscriptionService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDto request) {
        authService.register(
                request.getEmail(), request.getPassword(),
                request.getFirstName(), request.getLastName(),
                request.getOrganizationName());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Account created successfully. Please check your email and click the confirmation link to activate your account."
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        var result = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(toDto(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshRequestDto request) {
        var result = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(toDto(result));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> me() {
        var auth = getAuth();
        AppUser user = authService.getMe(auth.getUserId());
        Subscription subscription = subscriptionService.getByOrganizationId(auth.getOrganizationId());
        var permissions = subscriptionService.getGrantedFeatures(auth.getOrganizationId());

        return ResponseEntity.ok(UserProfileResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganization().getId())
                .organizationName(user.getOrganization().getName())
                .role(user.getRole())
                .subscription(UserProfileResponseDto.SubscriptionInfo.builder()
                        .planCode(subscription.getPlan().getCode())
                        .planName(subscription.getPlan().getDisplayName())
                        .status(subscription.getStatus().name())
                        .build())
                .permissions(permissions)
                .jiraConnected(Boolean.TRUE.equals(user.getJiraConnected()))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        var auth = getAuth();
        authService.logout(auth.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        var result = emailVerificationService.verifyEmail(token);
        var response = new java.util.LinkedHashMap<String, Object>();
        response.put("email", result.email());
        response.put("joinedOrganization", result.joinedOrganization());
        response.put("organizationId", result.organizationId());
        response.put("role", result.role());
        if (result.dashboardId() != null) {
            response.put("dashboardId", result.dashboardId());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification() {
        var auth = getAuth();
        AppUser user = authService.getMe(auth.getUserId());
        if (user.getEmailVerified()) {
            return ResponseEntity.noContent().build();
        }
        emailVerificationService.createVerificationToken(user);
        return ResponseEntity.accepted().build();
    }

    private AuthResponseDto toDto(AuthService.AuthResult result) {
        return AuthResponseDto.builder()
                .accessToken(result.accessToken())
                .refreshToken(result.refreshToken())
                .tokenType("Bearer")
                .expiresIn(result.expiresIn())
                .user(AuthResponseDto.UserDto.builder()
                        .id(result.userId())
                        .email(result.email())
                        .firstName(result.firstName())
                        .lastName(result.lastName())
                        .organizationId(result.organizationId())
                        .role(result.role())
                        .build())
                .build();
    }

    private JwtAuthenticationToken getAuth() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
}
