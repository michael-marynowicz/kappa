package com.company.sprintreporter.domain.port;

import java.util.UUID;

public interface BillingProviderPort {

    String createCustomer(String email, String organizationName);

    CheckoutSessionResult createCheckoutSession(String customerId, String planCode, String promoCode);

    SubscriptionStatusResult getSubscriptionStatus(String providerSubscriptionId);

    void cancelSubscription(String providerSubscriptionId, boolean atPeriodEnd);

    String createPortalSession(String customerId, String returnUrl);

    record CheckoutSessionResult(String sessionUrl, String providerSubscriptionId) {}

    record SubscriptionStatusResult(String status, java.time.Instant currentPeriodEnd) {}
}
