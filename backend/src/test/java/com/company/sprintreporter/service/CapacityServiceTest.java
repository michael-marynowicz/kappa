package com.company.sprintreporter.service;

import com.company.sprintreporter.application.dto.CapacityDto;
import com.company.sprintreporter.application.dto.TeamAvailabilityDto;
import com.company.sprintreporter.domain.entity.Dashboard;
import com.company.sprintreporter.domain.model.CapacityEntry;
import com.company.sprintreporter.domain.model.SprintInfo;
import com.company.sprintreporter.domain.model.TeamMember;
import com.company.sprintreporter.domain.port.CapacityPlanningStore;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import com.company.sprintreporter.service.jira.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CapacityService")
class CapacityServiceTest {

    @Mock
    private CapacityPlanningStore store;
    @Mock
    private JiraIssueRepository jiraIssueRepository;
    @Mock
    private DashboardService dashboardService;

    private CapacityService capacityService;

    private UUID orgId;
    private UUID dashboardId;

    @BeforeEach
    void setUp() {
        capacityService = new CapacityService(store, jiraIssueRepository, dashboardService);
        orgId = UUID.randomUUID();
        dashboardId = UUID.randomUUID();

        when(dashboardService.findActiveDashboard(orgId))
                .thenReturn(Optional.of(Dashboard.builder().id(dashboardId).build()));
        lenient().when(jiraIssueRepository.fetchSprintInfos()).thenReturn(List.of(
                SprintInfo.builder()
                        .name("ROC - PI26.3.1")
                        .startDate(LocalDate.of(2026, 6, 15))
                        .endDate(LocalDate.of(2026, 7, 10))
                        .build()
        ));
    }

    @Test
    @DisplayName("computeCapacity should use days off and time override")
    void computeCapacityUsesDaysOffAndTimeOverride() {
        TeamMember celine = TeamMember.create("m1", "Celine MARIN", TeamMember.Role.DEV, 0.9);
        TeamMember anna = TeamMember.create("m2", "Anna STIHLOVA", TeamMember.Role.DEV, 0.8);

        when(store.findAllMembers(dashboardId)).thenReturn(List.of(celine, anna));
        when(store.findEntriesBySprint(dashboardId, "ROC - PI26.3.1")).thenReturn(List.of(
                CapacityEntry.create("m1", "ROC - PI26.3.1", 6.0),
                CapacityEntry.create("m2", "ROC - PI26.3.1", 4.0)
        ));

        CapacityDto result = capacityService.computeCapacity(orgId, "ROC - PI26.3.1");

        assertThat(result.getPlannedCapacity()).isEqualTo(34.0); // 20*0.9 + 20*0.8
        assertThat(result.getRealCapacity()).isEqualTo(25.4);    // (20-6)*0.9 + (20-4)*0.8
    }

    @Test
    @DisplayName("computeTeamAvailability should clamp days off greater than sprint days")
    void computeTeamAvailabilityClampsDaysOff() {
        TeamMember dev = TeamMember.create("m1", "Dev User", TeamMember.Role.DEV, 1.0);
        TeamMember pda = TeamMember.create("m2", "Pda User", TeamMember.Role.PDA, 0.5);
        TeamMember qa = TeamMember.create("m3", "Qa User", TeamMember.Role.QA, 0.8);

        when(store.findAllMembers(dashboardId)).thenReturn(List.of(dev, pda, qa));
        when(store.findEntriesBySprint(dashboardId, "ROC - PI26.3.1")).thenReturn(List.of(
                CapacityEntry.create("m1", "ROC - PI26.3.1", 30.0),
                CapacityEntry.create("m2", "ROC - PI26.3.1", 2.0),
                CapacityEntry.create("m3", "ROC - PI26.3.1", 5.0)
        ));

        TeamAvailabilityDto result = capacityService.computeTeamAvailability(orgId, "ROC - PI26.3.1");

        assertThat(result.getDev()).isEqualTo(0.0);   // clamped: max(20-30, 0) * 1.0
        assertThat(result.getPda()).isEqualTo(9.0);   // (20-2) * 0.5
        assertThat(result.getQa()).isEqualTo(12.0);   // (20-5) * 0.8
    }

    @Test
    @DisplayName("updateMemberTimeOverride should update only override and preserve name/role")
    void updateMemberTimeOverridePreservesNameAndRole() {
        TeamMember existing = TeamMember.create("m1", "Susana DURAN", TeamMember.Role.SM, 0.5);
        when(store.findMemberById("m1")).thenReturn(Optional.of(existing));
        when(store.saveMember(eq(orgId), eq(dashboardId), any(TeamMember.class)))
                .thenAnswer(inv -> inv.getArgument(2));

        var updated = capacityService.updateMemberTimeOverride(orgId, "m1", 0.7);

        assertThat(updated.getName()).isEqualTo("Susana DURAN");
        assertThat(updated.getRole()).isEqualTo("SM");
        assertThat(updated.getTimeOverride()).isEqualTo(0.7);
        verify(store).saveMember(eq(orgId), eq(dashboardId), any(TeamMember.class));
    }
}
