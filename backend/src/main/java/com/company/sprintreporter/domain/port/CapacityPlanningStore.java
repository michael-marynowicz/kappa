package com.company.sprintreporter.domain.port;

import com.company.sprintreporter.domain.model.CapacityEntry;
import com.company.sprintreporter.domain.model.TeamMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapacityPlanningStore {

    // Team members (scoped by dashboard)
    List<TeamMember> findAllMembers(UUID dashboardId);
    Optional<TeamMember> findMemberById(String id);
    TeamMember saveMember(UUID organizationId, UUID dashboardId, TeamMember member);
    void deleteMember(String id);

    // Capacity entries (days off per member per sprint, scoped by dashboard)
    List<CapacityEntry> findAllEntries(UUID dashboardId);
    List<CapacityEntry> findEntriesByMember(String teamMemberId);
    List<CapacityEntry> findEntriesBySprint(UUID dashboardId, String sprintName);
    Optional<CapacityEntry> findEntry(String teamMemberId, String sprintName);
    CapacityEntry saveEntry(UUID organizationId, UUID dashboardId, CapacityEntry entry);
    void deleteEntriesByMember(String teamMemberId);
}
