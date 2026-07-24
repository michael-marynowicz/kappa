package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Optional<Plan> findByCode(String code);

    @Query("SELECT p FROM Plan p LEFT JOIN FETCH p.features WHERE p.active = true ORDER BY p.priceMonthly")
    List<Plan> findAllActiveWithFeatures();
}
