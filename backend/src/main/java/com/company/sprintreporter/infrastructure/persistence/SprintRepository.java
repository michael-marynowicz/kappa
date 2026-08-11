package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.Sprint;
import com.company.sprintreporter.domain.entity.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    List<Sprint> findByOrganizationIdOrderByStartDateDesc(UUID organizationId);

    List<Sprint> findByOrganizationIdAndStatus(UUID organizationId, SprintStatus status);

    Optional<Sprint> findByOrganizationIdAndExternalId(UUID organizationId, String externalId);
}
