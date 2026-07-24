package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByOrganizationId(UUID organizationId);

    boolean existsByOrganizationIdAndName(UUID organizationId, String name);
}
