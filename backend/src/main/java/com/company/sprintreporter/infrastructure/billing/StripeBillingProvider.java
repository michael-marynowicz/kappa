package com.company.sprintreporter.infrastructure.billing;

import com.company.sprintreporter.config.StripeProperties;
import com.company.sprintreporter.domain.port.BillingProviderPort;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "true")
public class StripeBillingProvider implements BillingProviderPort {

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        log.info("Stripe billing provider initialized");
    }

    @Override
    public String createCustomer(String email, String organizationName) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(organizationName)
                    .build();
            Customer customer = Customer.create(params);
            return customer.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe customer: {}", e.getMessage());
            throw new RuntimeException("Billing provider error: " + e.getMessage(), e);
        }
    }

    @Override
    public CheckoutSessionResult createCheckoutSession(String customerId, String planCode, String stripePriceId, String promoCode) {
        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customerId)
                    .setSuccessUrl(stripeProperties.getSuccessUrl())
                    .setCancelUrl(stripeProperties.getCancelUrl())
                    .putMetadata("plan_code", planCode)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(stripePriceId)
                            .setQuantity(1L)
                            .build());

            if (promoCode != null && !promoCode.isBlank()) {
                List<String> codes = new ArrayList<>();
                codes.add(promoCode);
                builder.setAllowPromotionCodes(true);
            }

            Session session = Session.create(builder.build());

            return new CheckoutSessionResult(session.getUrl(), session.getSubscription());
        } catch (StripeException e) {
            log.error("Failed to create checkout session: {}", e.getMessage());
            throw new RuntimeException("Billing provider error: " + e.getMessage(), e);
        }
    }

    @Override
    public SubscriptionStatusResult getSubscriptionStatus(String providerSubscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(providerSubscriptionId);
            String status = subscription.getStatus().toUpperCase();
            Instant periodEnd = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
            return new SubscriptionStatusResult(status, periodEnd);
        } catch (StripeException e) {
            log.error("Failed to get subscription status: {}", e.getMessage());
            throw new RuntimeException("Billing provider error: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelSubscription(String providerSubscriptionId, boolean atPeriodEnd) {
        try {
            Subscription subscription = Subscription.retrieve(providerSubscriptionId);
            if (atPeriodEnd) {
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();
                subscription.update(params);
            } else {
                subscription.cancel();
            }
        } catch (StripeException e) {
            log.error("Failed to cancel subscription: {}", e.getMessage());
            throw new RuntimeException("Billing provider error: " + e.getMessage(), e);
        }
    }

    @Override
    public String createPortalSession(String customerId, String returnUrl) {
        try {
            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(customerId)
                            .setReturnUrl(returnUrl)
                            .build();
            com.stripe.model.billingportal.Session portalSession =
                    com.stripe.model.billingportal.Session.create(params);
            return portalSession.getUrl();
        } catch (StripeException e) {
            log.error("Failed to create portal session: {}", e.getMessage());
            throw new RuntimeException("Billing provider error: " + e.getMessage(), e);
        }
    }
}
