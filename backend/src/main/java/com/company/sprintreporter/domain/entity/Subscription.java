package com.company.sprintreporter.domain.entity;

import com.company.sprintreporter.domain.entity.enums.BillingProvider;
import com.company.sprintreporter.domain.entity.enums.SubscriptionStatus;
import com.company.sprintreporter.domain.entity.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.TRIALING;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_provider", nullable = false, length = 30)
    @Builder.Default
    private BillingProvider billingProvider = BillingProvider.NONE;

    @Column(name = "provider_subscription_id", length = 255)
    private String providerSubscriptionId;

    @Column(name = "current_period_start", nullable = false)
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    @Column(name = "cancel_at_period_end", nullable = false)
    @Builder.Default
    private Boolean cancelAtPeriodEnd = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionType subscriptionType = SubscriptionType.SELF_SERVICE;

    @Column(name = "pilot_expires_at")
    private Instant pilotExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIALING;
    }

    /** Enterprise and Pilot subscriptions never show Stripe payment pages. */
    public boolean showPaymentPages() {
        return subscriptionType == SubscriptionType.SELF_SERVICE;
    }

    public boolean isEnterprise() {
        return subscriptionType == SubscriptionType.ENTERPRISE;
    }

    public boolean isPilot() {
        return subscriptionType == SubscriptionType.PILOT;
    }
}
