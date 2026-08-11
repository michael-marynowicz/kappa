package com.company.sprintreporter.service;

import com.company.sprintreporter.application.dto.CapacityPlanningResponseDto;
import com.company.sprintreporter.application.dto.SprintDetailDto;
import com.company.sprintreporter.application.dto.TeamMemberDto;
import com.company.sprintreporter.domain.model.SprintInfo;
import com.company.sprintreporter.domain.model.SprintIssue;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("CsvExportService")
class CsvExportServiceTest {

    private final CsvExportService service = new CsvExportService();

    @Test
    @DisplayName("should include CSV header row")
    void includesHeaderRow() {
        String csv = service.exportToCsv(List.of());
        assertThat(csv).contains("Key");
        assertThat(csv).contains("Total SP");
        assertThat(csv).contains("Done SP");
    }

    @Test
    @DisplayName("should include one row per issue with correct computed values")
    void includesOneRowPerIssueWithComputedValues() {
        SprintIssue issue = SprintIssue.builder()
                .issueKey("SCRUM-1")
                .summary("Build login page")
                .status("Done")
                .assignee("Alice")
                .issueType("Story")
                .totalStoryPoints(8)
                .remainingStoryPoints(0)
                .build();

        String csv = service.exportToCsv(List.of(issue));

        assertThat(csv).contains("SCRUM-1");
        assertThat(csv).contains("Build login page");
        assertThat(csv).contains("Alice");
        assertThat(csv).contains("8"); // total
        assertThat(csv).contains("0"); // remaining
        // done = 8 - 0 = 8
        assertThat(csv.lines().filter(l -> l.contains("SCRUM-1")).findFirst())
                .isPresent()
                .hasValueSatisfying(line -> assertThat(line).contains("8"));
    }

    @Test
    @DisplayName("should handle null story points gracefully")
    void handlesNullStoryPointsGracefully() {
        SprintIssue issue = SprintIssue.builder()
                .issueKey("SCRUM-2")
                .summary("Unestimated task")
                .status("To Do")
                .totalStoryPoints(null)
                .remainingStoryPoints(null)
                .build();

        assertThatCode(() -> service.exportToCsv(List.of(issue)))
                .doesNotThrowAnyException();

        String csv = service.exportToCsv(List.of(issue));
        assertThat(csv).contains("SCRUM-2");
    }

    @Test
    @DisplayName("exportCapacityToXlsx should include SP and use IT/IP with effective day formula")
    void exportCapacityToXlsxIncludesSpAndFormulaColumns() throws Exception {
        String itSprint = "ROC - PI26.3.1";
        String ipSprint = "ROC - PI26.3 IP";

        TeamMemberDto member = TeamMemberDto.builder()
                .id("m1")
                .name("Susana DURAN")
                .role("DEV")
                .timeOverride(0.5)
                .build();

        Map<String, Integer> daysPerSprint = new LinkedHashMap<>();
        daysPerSprint.put(itSprint, 20);
        daysPerSprint.put(ipSprint, 10);

        Map<String, Map<String, Double>> grid = new LinkedHashMap<>();
        grid.put("m1", Map.of(itSprint, 1.0, ipSprint, 0.0));

        CapacityPlanningResponseDto capacity = CapacityPlanningResponseDto.builder()
                .members(List.of(member))
                .sprints(List.of(itSprint, ipSprint))
                .daysPerSprint(daysPerSprint)
                .daysOffGrid(grid)
                .sprintDetails(Map.of(
                        itSprint, SprintDetailDto.builder().pi("26.3").iteration(1).ip(false).build(),
                        ipSprint, SprintDetailDto.builder().pi("26.3").iteration(99).ip(true).build()
                ))
                .build();

        byte[] content = service.exportCapacityToXlsx(
                capacity,
                List.of(
                        SprintInfo.builder().name(itSprint)
                                .startDate(LocalDate.of(2026, 6, 15)).endDate(LocalDate.of(2026, 7, 10)).build(),
                        SprintInfo.builder().name(ipSprint)
                                .startDate(LocalDate.of(2026, 9, 7)).endDate(LocalDate.of(2026, 9, 18)).build()
                )
        );

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            var summary = workbook.getSheet("Summary");
            var detail = workbook.getSheet("Detail");

            assertThat(summary.getRow(0).getCell(5).getStringCellValue()).isEqualTo("SP");

            // IT row: total=(20-1)*0.5=9.5, 80%=7.6, SP=round(7.6/1.5)=5
            assertThat(summary.getRow(1).getCell(3).getNumericCellValue()).isEqualTo(9.5);
            assertThat(summary.getRow(1).getCell(4).getNumericCellValue()).isEqualTo(7.6);
            assertThat(summary.getRow(1).getCell(5).getNumericCellValue()).isEqualTo(5.0);

            assertThat(detail.getRow(0).getCell(6).getStringCellValue()).isEqualTo("Time override");
            assertThat(detail.getRow(1).getCell(2).getStringCellValue()).isEqualTo("IT");
            assertThat(detail.getRow(1).getCell(8).getNumericCellValue()).isEqualTo(9.5);
            assertThat(detail.getRow(2).getCell(2).getStringCellValue()).isEqualTo("IP");
        }
    }
}
