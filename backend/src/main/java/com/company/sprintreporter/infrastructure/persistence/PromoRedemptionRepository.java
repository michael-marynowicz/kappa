package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.PromoRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PromoRedemptionRepository extends JpaRepository<PromoRedemption, UUID> {

    boolean existsByPromoCodeIdAndOrganizationId(UUID promoCodeId, UUID organizationId);
}
