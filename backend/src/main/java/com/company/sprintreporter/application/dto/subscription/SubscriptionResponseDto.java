package com.company.sprintreporter.application.dto.subscription;

import com.company.sprintreporter.domain.entity.enums.BillingProvider;
import com.company.sprintreporter.domain.entity.enums.SubscriptionStatus;
import com.company.sprintreporter.domain.entity.enums.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class SubscriptionResponseDto {

    private UUID id;
    private String planCode;
    private String planName;
    private SubscriptionStatus status;
    private BillingProvider billingProvider;
    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;
    private Boolean cancelAtPeriodEnd;

    // Billing model
    private SubscriptionType subscriptionType;
    private Instant pilotExpiresAt;

    // Frontend hints — no logic in the UI
    private boolean showPaymentPages;
    private boolean isEnterprise;
    private boolean isPilot;
}
