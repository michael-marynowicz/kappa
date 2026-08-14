package com.company.sprintreporter.service.subscription;

import com.company.sprintreporter.application.dto.subscription.CheckoutResponseDto;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.entity.Plan;
import com.company.sprintreporter.domain.entity.Subscription;
import com.company.sprintreporter.domain.entity.enums.BillingProvider;
import com.company.sprintreporter.domain.entity.enums.SubscriptionStatus;
import com.company.sprintreporter.domain.entity.enums.SubscriptionType;
import com.company.sprintreporter.domain.port.BillingProviderPort;
import com.company.sprintreporter.infrastructure.persistence.OrganizationRepository;
import com.company.sprintreporter.infrastructure.persistence.PlanRepository;
import com.company.sprintreporter.infrastructure.persistence.SubscriptionRepository;
import com.company.sprintreporter.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final OrganizationRepository organizationRepository;
    private final BillingProviderPort billingProvider;

    @Value("${stripe.enabled:false}")
    private boolean stripeEnabled;

    public Subscription getByOrganizationId(UUID organizationId) {
        return subscriptionRepository.findByOrganizationIdWithPlan(organizationId)
                .orElseThrow(() -> new BusinessException("Subscription not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Subscription changePlan(UUID organizationId, String planCode) {
        Subscription subscription = getByOrganizationId(organizationId);
        Plan newPlan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new BusinessException("Plan not found: " + planCode, HttpStatus.NOT_FOUND));

        subscription.setPlan(newPlan);
        subscription.setStatus(newPlan.getTrialDays() > 0 ? SubscriptionStatus.TRIALING : SubscriptionStatus.ACTIVE);
        subscription.setCurrentPeriodStart(Instant.now());
        subscription.setCurrentPeriodEnd(Instant.now().plus(
                newPlan.getTrialDays() > 0 ? newPlan.getTrialDays() : 30, ChronoUnit.DAYS));

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void cancelSubscription(UUID organizationId) {
        Subscription subscription = getByOrganizationId(organizationId);
        subscription.setCancelAtPeriodEnd(true);

        if (subscription.getProviderSubscriptionId() != null) {
            billingProvider.cancelSubscription(subscription.getProviderSubscriptionId(), true);
        }

        subscriptionRepository.save(subscription);
    }

    public List<Plan> getAvailablePlans() {
        return planRepository.findAllActiveWithFeatures();
    }

    public boolean hasFeature(UUID organizationId, String featureCode) {
        Subscription subscription = getByOrganizationId(organizationId);
        if (!subscription.isActive()) {
            return false;
        }
        return subscription.getPlan().getFeatures().stream()
                .anyMatch(f -> f.getCode().equals(featureCode));
    }

    /**
     * Returns all feature codes granted to an organization based on their active subscription.
     * Returns empty set if subscription is inactive/expired.
     */
    public Set<String> getGrantedFeatures(UUID organizationId) {
        Subscription subscription = getByOrganizationId(organizationId);
        if (!subscription.isActive()) {
            return Collections.emptySet();
        }
        return subscription.getPlan().getFeatures().stream()
                .map(f -> f.getCode())
                .collect(Collectors.toSet());
    }

    @Transactional
    public CheckoutResponseDto createCheckoutSession(UUID organizationId, String planCode, String promoCode) {
        Plan plan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new BusinessException("Plan not found: " + planCode, HttpStatus.NOT_FOUND));

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("Organization not found", HttpStatus.NOT_FOUND));

        // Create or retrieve billing customer
        String customerId = org.getStripeCustomerId();
        if (customerId == null || customerId.isBlank()) {
            customerId = billingProvider.createCustomer(org.getEmail(), org.getName());
            org.setStripeCustomerId(customerId);
            organizationRepository.save(org);
        }

        // Create checkout session via billing provider
        if (stripeEnabled && (plan.getStripePriceId() == null || plan.getStripePriceId().isBlank())) {
            throw new BusinessException("Plan is not configured for billing: " + planCode, HttpStatus.CONFLICT);
        }
        var result = billingProvider.createCheckoutSession(customerId, planCode, plan.getStripePriceId(), promoCode);

        // Store provider subscription ID on the subscription
        Subscription subscription = getByOrganizationId(organizationId);
        subscription.setProviderSubscriptionId(result.providerSubscriptionId());
        subscriptionRepository.save(subscription);

        // If mock billing (session URL is localhost), activate plan immediately
        if (result.sessionUrl().contains("mock-billing.local")) {
            subscription.setPlan(plan);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setCurrentPeriodStart(Instant.now());
            subscription.setCurrentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
            subscriptionRepository.save(subscription);

            return CheckoutResponseDto.builder()
                    .checkoutUrl(null)
                    .sessionId(result.providerSubscriptionId())
                    .build();
        }

        return CheckoutResponseDto.builder()
                .checkoutUrl(result.sessionUrl())
                .sessionId(result.providerSubscriptionId())
                .build();
    }

    public String getPortalUrl(UUID organizationId, String returnUrl) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("Organization not found", HttpStatus.NOT_FOUND));

        if (org.getStripeCustomerId() == null || org.getStripeCustomerId().isBlank()) {
            throw new BusinessException("No billing account found. Please subscribe first.", HttpStatus.BAD_REQUEST);
        }

        return billingProvider.createPortalSession(org.getStripeCustomerId(), returnUrl);
    }

    @Transactional
    public void activateFromWebhook(String providerSubscriptionId, String planCode) {
        Plan plan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new BusinessException("Plan not found: " + planCode));

        // Find subscription by provider ID or create logic based on webhook data
        // This will be called by the Stripe webhook controller
        subscriptionRepository.findByProviderSubscriptionId(providerSubscriptionId)
                .ifPresent(sub -> {
                    sub.setPlan(plan);
                    sub.setStatus(SubscriptionStatus.ACTIVE);
                    sub.setBillingProvider(BillingProvider.STRIPE);
                    sub.setProviderSubscriptionId(providerSubscriptionId);
                    sub.setCurrentPeriodStart(Instant.now());
                    sub.setCurrentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
                    subscriptionRepository.save(sub);
                });
    }

    @Transactional
    public void renewFromWebhook(String providerSubscriptionId) {
        subscriptionRepository.findByProviderSubscriptionId(providerSubscriptionId)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.ACTIVE);
                    sub.setCurrentPeriodStart(Instant.now());
                    sub.setCurrentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
                    sub.setCancelAtPeriodEnd(false);
                    subscriptionRepository.save(sub);
                });
    }

    @Transactional
    public void markPastDueFromWebhook(String providerSubscriptionId) {
        subscriptionRepository.findByProviderSubscriptionId(providerSubscriptionId)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.PAST_DUE);
                    subscriptionRepository.save(sub);
                });
    }

    @Transactional
    public void syncStatusFromWebhook(String providerSubscriptionId, String stripeStatus) {
        subscriptionRepository.findByProviderSubscriptionId(providerSubscriptionId)
                .ifPresent(sub -> {
                    SubscriptionStatus mapped = mapStripeStatus(stripeStatus);
                    sub.setStatus(mapped);
                    subscriptionRepository.save(sub);
                });
    }

    @Transactional
    public void cancelFromWebhook(String providerSubscriptionId) {
        subscriptionRepository.findByProviderSubscriptionId(providerSubscriptionId)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.CANCELED);
                    sub.setCancelAtPeriodEnd(false);
                    subscriptionRepository.save(sub);
                });
    }

    private SubscriptionStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled", "unpaid" -> SubscriptionStatus.CANCELED;
            default -> SubscriptionStatus.ACTIVE;
        };
    }

    // ── Admin operations ──

    /**
     * Assigns an Enterprise subscription to an organization.
     * Billing is handled externally — no Stripe involved.
     */
    @Transactional
    public Subscription assignEnterprise(UUID organizationId, String planCode) {
        Subscription sub = getByOrganizationId(organizationId);
        Plan plan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new BusinessException("Plan not found: " + planCode, HttpStatus.NOT_FOUND));

        sub.setPlan(plan);
        sub.setSubscriptionType(SubscriptionType.ENTERPRISE);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setBillingProvider(BillingProvider.MANUAL);
        sub.setProviderSubscriptionId(null);
        sub.setCancelAtPeriodEnd(false);
        sub.setCurrentPeriodStart(Instant.now());
        sub.setCurrentPeriodEnd(null);
        sub.setPilotExpiresAt(null);
        return subscriptionRepository.save(sub);
    }

    /**
     * Assigns a Pilot subscription to an organization.
     * Full plan access until pilotExpiresAt; no payment required.
     */
    @Transactional
    public Subscription assignPilot(UUID organizationId, String planCode, Instant pilotExpiresAt) {
        Subscription sub = getByOrganizationId(organizationId);
        Plan plan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new BusinessException("Plan not found: " + planCode, HttpStatus.NOT_FOUND));

        sub.setPlan(plan);
        sub.setSubscriptionType(SubscriptionType.PILOT);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setBillingProvider(BillingProvider.NONE);
        sub.setProviderSubscriptionId(null);
        sub.setCancelAtPeriodEnd(false);
        sub.setCurrentPeriodStart(Instant.now());
        sub.setCurrentPeriodEnd(pilotExpiresAt);
        sub.setPilotExpiresAt(pilotExpiresAt);
        return subscriptionRepository.save(sub);
    }

    /**
     * Converts a Pilot organization into a self-service paid organization.
     * Clears pilot metadata; the org goes through normal Stripe checkout next.
     */
    @Transactional
    public Subscription convertPilotToSelfService(UUID organizationId) {
        Subscription sub = getByOrganizationId(organizationId);
        sub.setSubscriptionType(SubscriptionType.SELF_SERVICE);
        sub.setStatus(SubscriptionStatus.TRIALING);
        sub.setBillingProvider(BillingProvider.NONE);
        sub.setPilotExpiresAt(null);
        sub.setCurrentPeriodEnd(Instant.now().plus(14, java.time.temporal.ChronoUnit.DAYS));
        return subscriptionRepository.save(sub);
    }

    /**
     * Manually activates or deactivates an organization's subscription.
     */
    @Transactional
    public Subscription setActive(UUID organizationId, boolean active) {
        Subscription sub = getByOrganizationId(organizationId);
        sub.setStatus(active ? SubscriptionStatus.ACTIVE : SubscriptionStatus.CANCELED);
        return subscriptionRepository.save(sub);
    }
}
