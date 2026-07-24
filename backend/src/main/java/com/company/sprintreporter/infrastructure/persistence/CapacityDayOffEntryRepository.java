package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.CapacityDayOffEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapacityDayOffEntryRepository extends JpaRepository<CapacityDayOffEntry, UUID> {

    List<CapacityDayOffEntry> findByOrganizationId(UUID organizationId);

    List<CapacityDayOffEntry> findByTeamMemberId(UUID teamMemberId);

    List<CapacityDayOffEntry> findByOrganizationIdAndSprintName(UUID organizationId, String sprintName);

    @org.springframework.data.jpa.repository.Query(
        "SELECT e FROM CapacityDayOffEntry e WHERE e.teamMember.dashboard.id = :dashboardId")
    List<CapacityDayOffEntry> findByDashboardId(@org.springframework.data.repository.query.Param("dashboardId") UUID dashboardId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT e FROM CapacityDayOffEntry e WHERE e.teamMember.dashboard.id = :dashboardId AND e.sprintName = :sprintName")
    List<CapacityDayOffEntry> findByDashboardIdAndSprintName(
        @org.springframework.data.repository.query.Param("dashboardId") UUID dashboardId,
        @org.springframework.data.repository.query.Param("sprintName") String sprintName);

    Optional<CapacityDayOffEntry> findByTeamMemberIdAndSprintName(UUID teamMemberId, String sprintName);

    void deleteByTeamMemberId(UUID teamMemberId);
}
