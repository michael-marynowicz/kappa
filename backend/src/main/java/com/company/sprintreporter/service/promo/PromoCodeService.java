package com.company.sprintreporter.service.promo;

import com.company.sprintreporter.domain.entity.PromoCode;
import com.company.sprintreporter.domain.entity.PromoRedemption;
import com.company.sprintreporter.domain.entity.Subscription;
import com.company.sprintreporter.domain.entity.enums.DiscountType;
import com.company.sprintreporter.domain.entity.enums.SubscriptionStatus;
import com.company.sprintreporter.infrastructure.persistence.*;
import com.company.sprintreporter.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoRedemptionRepository promoRedemptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public PromoRedemptionResult redeem(String code, UUID organizationId, UUID userId) {
        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Invalid promo code", HttpStatus.NOT_FOUND));

        // Validate
        if (!promo.isUsable()) {
            throw new BusinessException("Promo code is no longer valid");
        }
        if (promo.isRestrictedToOrg(organizationId)) {
            throw new BusinessException("Promo code not valid for your organization");
        }
        if (promoRedemptionRepository.existsByPromoCodeIdAndOrganizationId(promo.getId(), organizationId)) {
            throw new BusinessException("Promo code already redeemed by this organization");
        }

        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new BusinessException("No subscription found", HttpStatus.NOT_FOUND));

        // If promo is plan-restricted, verify matching plan
        if (promo.getPlan() != null && !promo.getPlan().getId().equals(subscription.getPlan().getId())) {
            throw new BusinessException("Promo code not valid for current plan");
        }

        // Apply discount
        String appliedEffect = applyDiscount(promo, subscription);

        // Record redemption
        promo.incrementRedemptions();
        promoCodeRepository.save(promo);

        var user = userRepository.getReferenceById(userId);
        var org = organizationRepository.getReferenceById(organizationId);

        PromoRedemption redemption = PromoRedemption.builder()
                .promoCode(promo)
                .organization(org)
                .user(user)
                .subscription(subscription)
                .build();
        promoRedemptionRepository.save(redemption);

        return new PromoRedemptionResult(promo.getDiscountType(), promo.getDiscountValue(), appliedEffect);
    }

    private String applyDiscount(PromoCode promo, Subscription subscription) {
        return switch (promo.getDiscountType()) {
            case TRIAL_EXTENSION -> {
                long extraDays = promo.getDiscountValue().longValue();
                Instant newEnd = subscription.getCurrentPeriodEnd() != null
                        ? subscription.getCurrentPeriodEnd().plus(extraDays, ChronoUnit.DAYS)
                        : Instant.now().plus(extraDays, ChronoUnit.DAYS);
                subscription.setCurrentPeriodEnd(newEnd);
                subscription.setStatus(SubscriptionStatus.TRIALING);
                subscriptionRepository.save(subscription);
                yield "Trial extended by " + extraDays + " days";
            }
            case PERCENT -> {
                // Store for billing provider to apply
                subscriptionRepository.save(subscription);
                yield promo.getDiscountValue() + "% discount applied";
            }
            case FIXED -> {
                subscriptionRepository.save(subscription);
                yield "$" + promo.getDiscountValue() + " discount applied";
            }
        };
    }

    public record PromoRedemptionResult(DiscountType type, java.math.BigDecimal value, String effect) {}
}
