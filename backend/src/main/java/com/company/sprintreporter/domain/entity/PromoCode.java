package com.company.sprintreporter.domain.entity;

import com.company.sprintreporter.domain.entity.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promo_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(name = "max_redemptions", nullable = false)
    @Builder.Default
    private Integer maxRedemptions = 1;

    @Column(name = "current_redemptions", nullable = false)
    @Builder.Default
    private Integer currentRedemptions = 0;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization restrictedToOrganization;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public boolean isUsable() {
        Instant now = Instant.now();
        return active
                && currentRedemptions < maxRedemptions
                && now.isAfter(validFrom)
                && now.isBefore(validUntil);
    }

    public boolean isRestrictedToOrg(UUID orgId) {
        return restrictedToOrganization != null
                && !restrictedToOrganization.getId().equals(orgId);
    }

    public void incrementRedemptions() {
        this.currentRedemptions++;
    }
}
