package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.PaymentCustomerReference;
import com.company.sprintreporter.domain.entity.enums.BillingProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentCustomerReferenceRepository extends JpaRepository<PaymentCustomerReference, UUID> {

    Optional<PaymentCustomerReference> findByOrganizationIdAndProvider(UUID organizationId, BillingProvider provider);
}
