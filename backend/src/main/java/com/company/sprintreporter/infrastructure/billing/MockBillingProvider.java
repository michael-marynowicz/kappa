package com.company.sprintreporter.infrastructure.billing;

import com.company.sprintreporter.domain.port.BillingProviderPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "false", matchIfMissing = true)
public class MockBillingProvider implements BillingProviderPort {

    @Override
    public String createCustomer(String email, String organizationName) {
        return "mock_cus_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public CheckoutSessionResult createCheckoutSession(String customerId, String planCode, String promoCode) {
        String subscriptionId = "mock_sub_" + UUID.randomUUID().toString().substring(0, 8);
        return new CheckoutSessionResult("https://mock-billing.local/checkout/" + subscriptionId, subscriptionId);
    }

    @Override
    public SubscriptionStatusResult getSubscriptionStatus(String providerSubscriptionId) {
        return new SubscriptionStatusResult("ACTIVE", Instant.now().plus(30, ChronoUnit.DAYS));
    }

    @Override
    public void cancelSubscription(String providerSubscriptionId, boolean atPeriodEnd) {
        // no-op for mock
    }

    @Override
    public String createPortalSession(String customerId, String returnUrl) {
        return "https://mock-billing.local/portal/" + customerId;
    }
}
