package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.Dashboard;
import com.company.sprintreporter.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

    List<Dashboard> findByOrganizationIdOrderByPositionAsc(UUID organizationId);

    Optional<Dashboard> findByOrganizationIdAndActiveTrue(UUID organizationId);

    List<Dashboard> findByOrganizationIdAndIdIn(UUID organizationId, List<UUID> ids);

    @Query("SELECT d FROM Dashboard d WHERE d.organization.id = :orgId AND EXISTS (SELECT u FROM AppUser u WHERE u.id = :userId AND d MEMBER OF u.dashboards) ORDER BY d.position ASC")
    List<Dashboard> findByOrganizationIdAndUserAccess(UUID orgId, UUID userId);

    boolean existsByOrganizationIdAndBoardId(UUID organizationId, Integer boardId);

    @Modifying
    @Query("UPDATE Dashboard d SET d.active = false WHERE d.organization.id = :orgId")
    void deactivateAllForOrg(UUID orgId);
}
