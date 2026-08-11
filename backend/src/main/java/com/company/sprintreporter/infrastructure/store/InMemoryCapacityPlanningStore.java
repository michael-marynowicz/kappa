package com.company.sprintreporter.infrastructure.store;

import com.company.sprintreporter.domain.model.CapacityEntry;
import com.company.sprintreporter.domain.model.TeamMember;
import com.company.sprintreporter.domain.port.CapacityPlanningStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation kept for tests only. Not a Spring component in production.
 */
@Slf4j
public class InMemoryCapacityPlanningStore implements CapacityPlanningStore {

    private final Map<String, TeamMember> members = new ConcurrentHashMap<>();
    // key = "teamMemberId::sprintName"
    private final Map<String, CapacityEntry> entries = new ConcurrentHashMap<>();

    @Override
    public List<TeamMember> findAllMembers(UUID dashboardId) {
        return List.copyOf(members.values());
    }

    @Override
    public Optional<TeamMember> findMemberById(String id) {
        return Optional.ofNullable(members.get(id));
    }

    @Override
    public TeamMember saveMember(UUID organizationId, UUID dashboardId, TeamMember member) {
        log.debug("Saving team member: {} ({})", member.getName(), member.getRole());
        members.put(member.getId(), member);
        return member;
    }

    @Override
    public void deleteMember(String id) {
        log.debug("Deleting team member: {}", id);
        members.remove(id);
        deleteEntriesByMember(id);
    }

    @Override
    public List<CapacityEntry> findAllEntries(UUID dashboardId) {
        return List.copyOf(entries.values());
    }

    @Override
    public List<CapacityEntry> findEntriesByMember(String teamMemberId) {
        return entries.values().stream()
                .filter(e -> e.getTeamMemberId().equals(teamMemberId))
                .toList();
    }

    @Override
    public List<CapacityEntry> findEntriesBySprint(UUID dashboardId, String sprintName) {
        return entries.values().stream()
                .filter(e -> e.getSprintName().equals(sprintName))
                .toList();
    }

    @Override
    public Optional<CapacityEntry> findEntry(String teamMemberId, String sprintName) {
        return Optional.ofNullable(entries.get(entryKey(teamMemberId, sprintName)));
    }

    @Override
    public CapacityEntry saveEntry(UUID organizationId, UUID dashboardId, CapacityEntry entry) {
        log.debug("Saving capacity entry: {} / {} = {} days off",
                entry.getTeamMemberId(), entry.getSprintName(), entry.getDaysOff());
        entries.put(entryKey(entry.getTeamMemberId(), entry.getSprintName()), entry);
        return entry;
    }

    @Override
    public void deleteEntriesByMember(String teamMemberId) {
        entries.entrySet().removeIf(e -> e.getValue().getTeamMemberId().equals(teamMemberId));
    }

    private String entryKey(String teamMemberId, String sprintName) {
        return teamMemberId + "::" + sprintName;
    }
}
