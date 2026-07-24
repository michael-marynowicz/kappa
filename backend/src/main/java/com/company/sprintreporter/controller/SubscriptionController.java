package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.subscription.*;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.entity.Subscription;
import com.company.sprintreporter.domain.entity.enums.SubscriptionType;
import com.company.sprintreporter.service.subscription.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponseDto> getCurrent() {
        var auth = getAuth();
        Subscription sub = subscriptionService.getByOrganizationId(auth.getOrganizationId());
        return ResponseEntity.ok(toDto(sub));
    }

    @GetMapping("/features")
    public ResponseEntity<java.util.Set<String>> getFeatures() {
        var auth = getAuth();
        return ResponseEntity.ok(subscriptionService.getGrantedFeatures(auth.getOrganizationId()));
    }

    @PutMapping("/plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponseDto> changePlan(@Valid @RequestBody ChangePlanRequestDto request) {
        var auth = getAuth();
        Subscription sub = subscriptionService.changePlan(auth.getOrganizationId(), request.getPlanCode());
        return ResponseEntity.ok(toDto(sub));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CheckoutResponseDto> createCheckout(@Valid @RequestBody CheckoutRequestDto request) {
        var auth = getAuth();
        var result = subscriptionService.createCheckoutSession(
                auth.getOrganizationId(), request.getPlanCode(), request.getPromoCode());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancel() {
        var auth = getAuth();
        subscriptionService.cancelSubscription(auth.getOrganizationId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/portal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PortalResponseDto> getPortalUrl(@RequestParam(defaultValue = "http://localhost:4200/billing") String returnUrl) {
        var auth = getAuth();
        String url = subscriptionService.getPortalUrl(auth.getOrganizationId(), returnUrl);
        return ResponseEntity.ok(PortalResponseDto.builder().portalUrl(url).build());
    }

    @GetMapping("/features/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeatureAccessResponseDto> checkFeature(@PathVariable String code) {
        var auth = getAuth();
        boolean hasAccess = subscriptionService.hasFeature(auth.getOrganizationId(), code);
        return ResponseEntity.ok(FeatureAccessResponseDto.builder()
                .featureCode(code)
                .hasAccess(hasAccess)
                .build());
    }

    private SubscriptionResponseDto toDto(Subscription sub) {
        return SubscriptionResponseDto.builder()
                .id(sub.getId())
                .planCode(sub.getPlan().getCode())
                .planName(sub.getPlan().getDisplayName())
                .status(sub.getStatus())
                .billingProvider(sub.getBillingProvider())
                .currentPeriodStart(sub.getCurrentPeriodStart())
                .currentPeriodEnd(sub.getCurrentPeriodEnd())
                .cancelAtPeriodEnd(sub.getCancelAtPeriodEnd())
                .subscriptionType(sub.getSubscriptionType())
                .pilotExpiresAt(sub.getPilotExpiresAt())
                .showPaymentPages(sub.showPaymentPages())
                .isEnterprise(sub.isEnterprise())
                .isPilot(sub.isPilot())
                .build();
    }

    // ── Admin endpoints ──

    /**
     * Assign an Enterprise plan to an organization.
     * Body: { "planCode": "BUSINESS" }
     */
    @PostMapping("/admin/enterprise")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponseDto> assignEnterprise(@Valid @RequestBody ChangePlanRequestDto request) {
        var auth = getAuth();
        Subscription sub = subscriptionService.assignEnterprise(auth.getOrganizationId(), request.getPlanCode());
        return ResponseEntity.ok(toDto(sub));
    }

    /**
     * Assign a Pilot plan to an organization.
     * Body: { "planCode": "BUSINESS", "pilotExpiresAt": "2026-12-31T23:59:59Z" }
     */
    @PostMapping("/admin/pilot")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponseDto> assignPilot(@Valid @RequestBody AssignPilotRequestDto request) {
        var auth = getAuth();
        Subscription sub = subscriptionService.assignPilot(
                auth.getOrganizationId(), request.getPlanCode(), request.getPilotExpiresAt());
        return ResponseEntity.ok(toDto(sub));
    }

    /**
     * Convert a Pilot organization into self-service (ready for Stripe checkout).
     */
    @PostMapping("/admin/convert-pilot")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponseDto> convertPilot() {
        var auth = getAuth();
        Subscription sub = subscriptionService.convertPilotToSelfService(auth.getOrganizationId());
        return ResponseEntity.ok(toDto(sub));
    }

    /**
     * Manually activate or deactivate an organization's subscription.
     * Query param: active=true|false
     */
    @PostMapping("/admin/activation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponseDto> setActive(@RequestParam boolean active) {
        var auth = getAuth();
        Subscription sub = subscriptionService.setActive(auth.getOrganizationId(), active);
        return ResponseEntity.ok(toDto(sub));
    }

    private JwtAuthenticationToken getAuth() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
}
