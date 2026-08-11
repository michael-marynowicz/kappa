package com.company.sprintreporter.domain.entity.enums;

/**
 * Determines how a subscription was acquired and how it should be billed.
 *
 * SELF_SERVICE — standard Stripe Checkout flow; payment pages visible.
 * ENTERPRISE   — manually assigned by admin; no Stripe; invoice/contract billing.
 * PILOT        — full access for a limited period without payment; expires at pilot_expires_at.
 */
public enum SubscriptionType {
    SELF_SERVICE,
    ENTERPRISE,
    PILOT
}
