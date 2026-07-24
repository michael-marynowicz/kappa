package com.company.sprintreporter.infrastructure.store;

import com.company.sprintreporter.domain.entity.CapacityDayOffEntry;
import com.company.sprintreporter.domain.entity.CapacityTeamMember;
import com.company.sprintreporter.domain.entity.Dashboard;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.model.CapacityEntry;
import com.company.sprintreporter.domain.model.TeamMember;
import com.company.sprintreporter.domain.port.CapacityPlanningStore;
import com.company.sprintreporter.infrastructure.persistence.CapacityDayOffEntryRepository;
import com.company.sprintreporter.infrastructure.persistence.CapacityTeamMemberRepository;
import com.company.sprintreporter.infrastructure.persistence.DashboardRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class JpaCapacityPlanningStore implements CapacityPlanningStore {

    private final CapacityTeamMemberRepository memberRepository;
    private final CapacityDayOffEntryRepository entryRepository;
    private final DashboardRepository dashboardRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<TeamMember> findAllMembers(UUID dashboardId) {
        return memberRepository.findByDashboardId(dashboardId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamMember> findMemberById(String id) {
        return memberRepository.findById(UUID.fromString(id))
                .map(this::toDomain);
    }

    @Override
    public TeamMember saveMember(UUID organizationId, UUID dashboardId, TeamMember member) {
        log.debug("Saving team member: {} ({}) for dashboard {}", member.getName(), member.getRole(), dashboardId);
        Organization orgRef = entityManager.getReference(Organization.class, organizationId);
        Dashboard dashboardRef = entityManager.getReference(Dashboard.class, dashboardId);

        CapacityTeamMember entity;
        if (member.getId() != null) {
            Optional<CapacityTeamMember> existing = memberRepository.findById(UUID.fromString(member.getId()));
            if (existing.isPresent()) {
                entity = existing.get();
                entity.setName(member.getName());
                entity.setRole(member.getRole().name());
                entity.setTimeOverride(member.getTimeOverride());
            } else {
                entity = toEntity(member, orgRef, dashboardRef);
                entity.setId(UUID.fromString(member.getId()));
            }
        } else {
            entity = toEntity(member, orgRef, dashboardRef);
        }

        CapacityTeamMember saved = memberRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteMember(String id) {
        log.debug("Deleting team member: {}", id);
        UUID memberId = UUID.fromString(id);
        entryRepository.deleteByTeamMemberId(memberId);
        memberRepository.deleteById(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CapacityEntry> findAllEntries(UUID dashboardId) {
        return entryRepository.findByDashboardId(dashboardId).stream()
                .map(this::toEntryDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CapacityEntry> findEntriesByMember(String teamMemberId) {
        return entryRepository.findByTeamMemberId(UUID.fromString(teamMemberId)).stream()
                .map(this::toEntryDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CapacityEntry> findEntriesBySprint(UUID dashboardId, String sprintName) {
        return entryRepository.findByDashboardIdAndSprintName(dashboardId, sprintName).stream()
                .map(this::toEntryDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CapacityEntry> findEntry(String teamMemberId, String sprintName) {
        return entryRepository.findByTeamMemberIdAndSprintName(UUID.fromString(teamMemberId), sprintName)
                .map(this::toEntryDomain);
    }

    @Override
    public CapacityEntry saveEntry(UUID organizationId, UUID dashboardId, CapacityEntry entry) {
        log.debug("Saving capacity entry: {} / {} = {} days off",
                entry.getTeamMemberId(), entry.getSprintName(), entry.getDaysOff());

        UUID memberId = UUID.fromString(entry.getTeamMemberId());
        Organization orgRef = entityManager.getReference(Organization.class, organizationId);
        CapacityTeamMember memberRef = entityManager.getReference(CapacityTeamMember.class, memberId);

        CapacityDayOffEntry entity = entryRepository
                .findByTeamMemberIdAndSprintName(memberId, entry.getSprintName())
                .orElseGet(() -> CapacityDayOffEntry.builder()
                        .organization(orgRef)
                        .teamMember(memberRef)
                        .sprintName(entry.getSprintName())
                        .build());

        entity.setDaysOff(entry.getDaysOff());
        CapacityDayOffEntry saved = entryRepository.save(entity);
        return toEntryDomain(saved);
    }

    @Override
    public void deleteEntriesByMember(String teamMemberId) {
        entryRepository.deleteByTeamMemberId(UUID.fromString(teamMemberId));
    }

    // ── Mappers ──

    private TeamMember toDomain(CapacityTeamMember entity) {
        return TeamMember.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .role(TeamMember.Role.valueOf(entity.getRole()))
                .timeOverride(entity.getTimeOverride())
                .build();
    }

    private CapacityTeamMember toEntity(TeamMember member, Organization org, Dashboard dashboard) {
        return CapacityTeamMember.builder()
                .organization(org)
                .dashboard(dashboard)
                .name(member.getName())
                .role(member.getRole().name())
                .timeOverride(member.getTimeOverride())
                .build();
    }

    private CapacityEntry toEntryDomain(CapacityDayOffEntry entity) {
        return CapacityEntry.builder()
                .teamMemberId(entity.getTeamMember().getId().toString())
                .sprintName(entity.getSprintName())
                .daysOff(entity.getDaysOff())
                .build();
    }
}
