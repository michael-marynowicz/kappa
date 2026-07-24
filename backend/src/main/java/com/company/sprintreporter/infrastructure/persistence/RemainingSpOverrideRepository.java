package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.RemainingSpOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RemainingSpOverrideRepository extends JpaRepository<RemainingSpOverride, UUID> {

    List<RemainingSpOverride> findByOrganizationId(UUID organizationId);

    Optional<RemainingSpOverride> findByOrganizationIdAndIssueKey(UUID organizationId, String issueKey);

    void deleteByOrganizationId(UUID organizationId);
}
