package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.Subscription;
import com.company.sprintreporter.domain.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByOrganizationId(UUID organizationId);

    Optional<Subscription> findByProviderSubscriptionId(String providerSubscriptionId);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.plan p LEFT JOIN FETCH p.features WHERE s.organization.id = :orgId")
    Optional<Subscription> findByOrganizationIdWithPlan(UUID orgId);

    List<Subscription> findByStatus(SubscriptionStatus status);
}
