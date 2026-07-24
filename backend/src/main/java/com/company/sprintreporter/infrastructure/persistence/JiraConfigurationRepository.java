package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.JiraConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JiraConfigurationRepository extends JpaRepository<JiraConfiguration, UUID> {

    Optional<JiraConfiguration> findByOrganizationId(UUID organizationId);
}
