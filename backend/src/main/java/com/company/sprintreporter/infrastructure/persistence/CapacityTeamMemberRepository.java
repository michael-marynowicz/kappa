package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.CapacityTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CapacityTeamMemberRepository extends JpaRepository<CapacityTeamMember, UUID> {

    List<CapacityTeamMember> findByDashboardId(UUID dashboardId);

    List<CapacityTeamMember> findByOrganizationId(UUID organizationId);
}
