error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/service/CapacityService.java:_empty_/CapacityPlanningResponseDto#builder#members#sprints#daysPerSprint#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/service/CapacityService.java
empty definition using pc, found symbol in pc: _empty_/CapacityPlanningResponseDto#builder#members#sprints#daysPerSprint#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 6257
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/service/CapacityService.java
text:
```scala
package com.company.sprintreporter.service;

import com.company.sprintreporter.application.dto.*;
import com.company.sprintreporter.application.dto.SprintDetailDto;
import com.company.sprintreporter.domain.entity.Dashboard;
import com.company.sprintreporter.domain.model.CapacityEntry;
import com.company.sprintreporter.domain.model.SprintInfo;
import com.company.sprintreporter.domain.model.TeamMember;
import com.company.sprintreporter.domain.port.CapacityPlanningStore;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import com.company.sprintreporter.service.jira.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapacityService {

    private final CapacityPlanningStore store;
    private final JiraIssueRepository jiraIssueRepository;
    private final DashboardService dashboardService;

    @Value("${capacity.days-per-sprint:10}")
    private int defaultDaysPerSprint;

    @Value("${capacity.holidays:}")
    private List<String> holidayDates;

    // ── Team Members ──

    public List<TeamMemberDto> getAllMembers(UUID organizationId) {
        UUID dashboardId = resolveActiveDashboardId(organizationId);
        return store.findAllMembers(dashboardId).stream()
                .map(this::toDto)
                .toList();
    }

    public TeamMemberDto addMember(UUID organizationId, TeamMemberDto dto) {
        UUID dashboardId = resolveActiveDashboardId(organizationId);
        TeamMember member = TeamMember.create(null, dto.getName(), TeamMember.Role.valueOf(dto.getRole()), dto.getTimeOverride());
        return toDto(store.saveMember(organizationId, dashboardId, member));
    }

    public TeamMemberDto updateMember(UUID organizationId, String id, TeamMemberDto dto) {
        store.findMemberById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found: " + id));
        UUID dashboardId = resolveActiveDashboardId(organizationId);
        TeamMember updated = TeamMember.create(id, dto.getName(), TeamMember.Role.valueOf(dto.getRole()), dto.getTimeOverride());
        return toDto(store.saveMember(organizationId, dashboardId, updated));
    }

    public TeamMemberDto updateMemberTimeOverride(UUID organizationId, String id, double timeOverride) {
        TeamMember existing = store.findMemberById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found: " + id));

        UUID dashboardId = resolveActiveDashboardId(organizationId);
        TeamMember updated = TeamMember.create(id, existing.getName(), existing.getRole(), timeOverride);
        return toDto(store.saveMember(organizationId, dashboardId, updated));
    }

    public void deleteMember(String id) {
        store.deleteMember(id);
    }

    // ── Capacity Entries ──

    public CapacityEntryDto setDaysOff(UUID organizationId, CapacityEntryDto dto) {
        store.findMemberById(dto.getTeamMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Team member not found: " + dto.getTeamMemberId()));
        UUID dashboardId = resolveActiveDashboardId(organizationId);
        CapacityEntry entry = CapacityEntry.create(dto.getTeamMemberId(), dto.getSprintName(), dto.getDaysOff());
        return toEntryDto(store.saveEntry(organizationId, dashboardId, entry));
    }

    public void setBulkDaysOff(UUID organizationId, List<CapacityEntryDto> entries) {
        for (CapacityEntryDto dto : entries) {
            setDaysOff(organizationId, dto);
        }
    }

    // ── Full Capacity Grid ──

    public CapacityPlanningResponseDto getCapacityPlanning(UUID organizationId) {
        UUID dashboardId = resolveActiveDashboardId(organizationId);
        List<TeamMember> members = store.findAllMembers(dashboardId);
        List<CapacityEntry> allEntries = store.findAllEntries(dashboardId);
        List<SprintInfo> sprintInfos = resolveSprintInfos();
        List<String> sprints = sprintInfos.stream().map(SprintInfo::getName).toList();

        // Map sprint name → business days
        Map<String, Integer> daysPerSprintMap = new LinkedHashMap<>();
        for (SprintInfo info : sprintInfos) {
            daysPerSprintMap.put(info.getName(), computeBusinessDays(info));
        }

        // Build grid: memberId → { sprintName → daysOff }
        Map<String, Map<String, Double>> grid = new LinkedHashMap<>();
        for (TeamMember member : members) {
            Map<String, Double> memberGrid = new LinkedHashMap<>();
            for (String sprint : sprints) {
                memberGrid.put(sprint, 0.0);
            }
            grid.put(member.getId(), memberGrid);
        }

        for (CapacityEntry entry : allEntries) {
            if (grid.containsKey(entry.getTeamMemberId())) {
                Map<String, Double> memberGrid = grid.get(entry.getTeamMemberId());
                if (memberGrid.containsKey(entry.getSprintName())) {
                    memberGrid.put(entry.getSprintName(), entry.getDaysOff());
                }
            }
        }

        // Pre-parse sprint PI/iteration so the frontend doesn't need to parse names
        Map<String, SprintDetailDto> sprintDetails = new LinkedHashMap<>();
        for (SprintInfo info : sprintInfos) {
            sprintDetails.put(info.getName(), toSprintDetail(info.getName()));
        }

        return CapacityPlanningResponseDto.builder()
                .members(members.stream().map(this::toDto).toList())
                .sprints(sprints)
                .@@daysPerSprint(daysPerSprintMap)
                .daysOffGrid(grid)
                .sprintDetails(sprintDetails)
                .build();
    }

    // ── Metrics Computation ──

    public String getActiveSprintName() {
        try {
            List<SprintInfo> infos = resolveSprintInfos();
            java.time.LocalDate today = java.time.LocalDate.now();
            // First try: sprint whose date range contains today
            return infos.stream()
                    .filter(s -> s.getStartDate() != null && s.getEndDate() != null)
                    .filter(s -> !today.isBefore(s.getStartDate()) && !today.isAfter(s.getEndDate()))
                    .map(SprintInfo::getName)
                    .findFirst()
                    // Fallback: first active sprint from Jira (startDate set, no endDate constraint)
                    .orElseGet(() -> infos.stream()
                            .filter(s -> !"IP".equals(s.getName()))
                            .filter(s -> s.getStartDate() != null)
                            .map(SprintInfo::getName)
                            .findFirst()
                            .orElse("Current Sprint"));
        } catch (Exception e) {
            return "Current Sprint";
        }
    }

    public CapacityDto computeCapacity(UUID organizationId, String sprintName) {
        UUID dashboardId = resolveActiveDashboardId(organizationId);
        List<TeamMember> members = store.findAllMembers(dashboardId);
        if (members.isEmpty()) {
            return CapacityDto.builder().plannedCapacity(0).realCapacity(0).build();
        }

        int days = getDaysForSprint(sprintName);
        double planned = members.stream()
            .filter(m -> m.getRole() != TeamMember.Role.SM)
            .mapToDouble(m -> computeEffectiveDays(days, 0.0, m.getTimeOverride()))
                .sum();
        Map<String, Double> daysOffMap = store.findEntriesBySprint(dashboardId, sprintName).stream()
                .collect(Collectors.toMap(CapacityEntry::getTeamMemberId, CapacityEntry::getDaysOff, (a, b) -> b));
        double real = members.stream()
            .filter(m -> m.getRole() != TeamMember.Role.SM)
            .mapToDouble(m -> computeEffectiveDays(days, daysOffMap.getOrDefault(m.getId(), 0.0), m.getTimeOverride()))
                .sum();

        return CapacityDto.builder()
                .plannedCapacity(planned)
                .realCapacity(Math.max(0, real))
                .build();
    }

    public TeamAvailabilityDto computeTeamAvailability(UUID organizationId, String sprintName) {
        UUID dashboardId = resolveActiveDashboardId(organizationId);
        List<TeamMember> members = store.findAllMembers(dashboardId);
        int days = getDaysForSprint(sprintName);
        Map<String, Double> daysOffMap = store.findEntriesBySprint(dashboardId, sprintName).stream()
                .collect(Collectors.toMap(CapacityEntry::getTeamMemberId, CapacityEntry::getDaysOff, (a, b) -> b));

        double dev = members.stream()
                .filter(m -> m.getRole() == TeamMember.Role.DEV)
            .mapToDouble(m -> computeEffectiveDays(days, daysOffMap.getOrDefault(m.getId(), 0.0), m.getTimeOverride()))
                .sum();
        double pda = members.stream()
                .filter(m -> m.getRole() == TeamMember.Role.PDA)
            .mapToDouble(m -> computeEffectiveDays(days, daysOffMap.getOrDefault(m.getId(), 0.0), m.getTimeOverride()))
                .sum();
        double qa = members.stream()
                .filter(m -> m.getRole() == TeamMember.Role.QA)
            .mapToDouble(m -> computeEffectiveDays(days, daysOffMap.getOrDefault(m.getId(), 0.0), m.getTimeOverride()))
                .sum();

        return TeamAvailabilityDto.builder().dev(dev).pda(pda).qa(qa).build();
    }

    // ── Sprint infos (public for export) ──

    public List<SprintInfo> getSprintInfos() {
        return resolveSprintInfos();
    }

    // ── Private helpers ──

    private UUID resolveActiveDashboardId(UUID organizationId) {
        return dashboardService.findActiveDashboard(organizationId)
                .map(Dashboard::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active dashboard found. Please configure a Jira board first."));
    }

    private int getDaysForSprint(String sprintName) {
        try {
            return resolveSprintInfos().stream()
                    .filter(s -> s.getName().equals(sprintName))
                    .findFirst()
                    .map(this::computeBusinessDays)
                    .orElse(defaultDaysPerSprint);
        } catch (Exception e) {
            return defaultDaysPerSprint;
        }
    }

    private int computeBusinessDays(SprintInfo sprintInfo) {
        if (sprintInfo.getStartDate() == null || sprintInfo.getEndDate() == null) {
            return defaultDaysPerSprint;
        }

        Set<LocalDate> holidays = parseHolidays();
        int count = 0;
        LocalDate date = sprintInfo.getStartDate();
        while (!date.isAfter(sprintInfo.getEndDate())) {
            DayOfWeek day = date.getDayOfWeek();
            boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            if (isWeekday && !holidays.contains(date)) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }

    private Set<LocalDate> parseHolidays() {
        Set<LocalDate> result = new HashSet<>();
        if (holidayDates == null) {
            return result;
        }
        for (String raw : holidayDates) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            try {
                result.add(LocalDate.parse(raw.trim()));
            } catch (Exception e) {
                log.warn("Ignoring invalid holiday date '{}': {}", raw, e.getMessage());
            }
        }
        return result;
    }

    private List<SprintInfo> resolveSprintInfos() {
        try {
            List<SprintInfo> allInfos = jiraIssueRepository.fetchSprintInfos();

            // Find the current PI prefix from the active sprint (last in the list)
            String piPrefix = extractCurrentPiPrefix(allInfos);

            // Filter to only sprints belonging to the current PI
            List<SprintInfo> piSprints;
            if (piPrefix != null) {
                log.debug("Current PI prefix: {}", piPrefix);
                piSprints = allInfos.stream()
                        .filter(s -> s.getName().contains(piPrefix))
                        .toList();
            } else {
                // Fallback: keep all if we can't detect the PI
                piSprints = allInfos;
            }

            // Add synthetic IP sprint only if none exists already in the filtered list
            boolean alreadyHasIp = piSprints.stream().anyMatch(s -> isIpSprintName(s.getName()));
            if (alreadyHasIp) {
                return piSprints;
            }
            return Stream.concat(
                    piSprints.stream(),
                    Stream.of(SprintInfo.builder().name("IP").startDate(null).endDate(null).build())
            ).toList();
        } catch (Exception e) {
            log.warn("Could not fetch sprint infos from Jira: {}", e.getMessage());
            return List.of(
                    SprintInfo.builder().name("Current Sprint").startDate(null).endDate(null).build(),
                    SprintInfo.builder().name("IP").startDate(null).endDate(null).build()
            );
        }
    }

    // ── Sprint name parsing helpers ──

    private static final java.util.regex.Pattern SVC_PI_DOT  = java.util.regex.Pattern.compile("PI#?(\\d+\\.\\d+)");
    private static final java.util.regex.Pattern SVC_PI_HASH = java.util.regex.Pattern.compile("PI#(\\d+)");
    private static final java.util.regex.Pattern SVC_IT_DOT  = java.util.regex.Pattern.compile("PI#?\\d+\\.\\d+\\.(\\d+)");
    private static final java.util.regex.Pattern SVC_IT_WORD = java.util.regex.Pattern.compile("(?i)(?:IT|Sprint)\\s*(\\d+)");
    private static final java.util.regex.Pattern SVC_IP_WORD = java.util.regex.Pattern.compile("\\bIP\\b");

    private boolean isIpSprintName(String name) {
        return SVC_IP_WORD.matcher(name).find();
    }

    private String extractSprintPi(String name) {
        Matcher m = SVC_PI_DOT.matcher(name);
        if (m.find()) return m.group(1);
        m = SVC_PI_HASH.matcher(name);
        return m.find() ? m.group(1) : "";
    }

    private int extractSprintItNum(String name) {
        if (isIpSprintName(name)) return 99;
        Matcher m = SVC_IT_DOT.matcher(name);
        if (m.find()) return Integer.parseInt(m.group(1));
        m = SVC_IT_WORD.matcher(name);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 0;
    }

    private SprintDetailDto toSprintDetail(String name) {
        return SprintDetailDto.builder()
                .pi(extractSprintPi(name))
                .iteration(extractSprintItNum(name))
                .ip(isIpSprintName(name))
                .build();
    }

    /**
     * Extract the PI prefix from the active sprint name.
     * e.g. "ROC - PI26.2.2" → "PI26.2", "ROC - PI26.2 IP" → "PI26.2"
     */
    private String extractCurrentPiPrefix(List<SprintInfo> infos) {
        if (infos.isEmpty()) return null;
        // Active sprint is the last one (fetchSprintInfos returns closed then active)
        String activeName = infos.get(infos.size() - 1).getName();
        // Match "PI#26.3" or "PI26.3" → captures "26.3"
        Matcher m = Pattern.compile("PI#?(\\d+\\.\\d+)").matcher(activeName);
        return m.find() ? m.group(1) : null;
    }

    private TeamMemberDto toDto(TeamMember member) {
        return TeamMemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .role(member.getRole().name())
                .timeOverride(member.getTimeOverride())
                .build();
    }

    private CapacityEntryDto toEntryDto(CapacityEntry entry) {
        return CapacityEntryDto.builder()
                .teamMemberId(entry.getTeamMemberId())
                .sprintName(entry.getSprintName())
                .daysOff(entry.getDaysOff())
                .build();
    }

    private double computeEffectiveDays(int sprintDays, double daysOff, double timeOverride) {
        double safeSprintDays = Math.max(0, sprintDays);
        double safeDaysOff = Math.max(0.0, daysOff);
        double availableDays = Math.max(0.0, safeSprintDays - safeDaysOff);
        double safeTimeOverride = Math.max(0.0, timeOverride);
        return availableDays * safeTimeOverride;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/CapacityPlanningResponseDto#builder#members#sprints#daysPerSprint#