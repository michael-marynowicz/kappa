package com.company.sprintreporter.service;

import com.company.sprintreporter.application.dto.CapacityPlanningResponseDto;
import com.company.sprintreporter.application.dto.TeamMemberDto;
import com.company.sprintreporter.domain.model.SprintInfo;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service responsible for generating CSV output from domain data.
 * Single-responsibility: knows how to serialize SprintIssue to CSV format.
 * The controller handles HTTP response headers; this service only returns content.
 */
@Slf4j
@Service
public class CsvExportService {

    private static final String[] CSV_HEADER = {
            "Key", "Summary", "Assignee", "Type", "Status",
            "Total SP", "Remaining SP", "Done SP"
    };

    private static final String[] CAPACITY_CSV_HEADER = {
            "PI", "Iteration", "Type", "Days", "Name",
            "Time", "Time override", "Days off sprint", "Effective days"
    };

        private static final double SP_EFFECTIVE_DAY_FACTOR = 1.5;

    /**
     * Serializes a list of sprint issues into CSV format.
     *
     * @param issues the issues to export
     * @return UTF-8 CSV string with header and one row per issue
     */
    public String exportToCsv(List<SprintIssue> issues) {
        log.debug("Generating CSV for {} issues", issues.size());

        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(CSV_HEADER);
            issues.stream()
                    .map(this::toRow)
                    .forEach(writer::writeNext);
        } catch (Exception e) {
            log.error("Error generating CSV export", e);
            throw new RuntimeException("Failed to generate CSV export", e);
        }

        return sw.toString();
    }

    private String[] toRow(SprintIssue issue) {
        return new String[]{
                issue.getIssueKey(),
                Objects.requireNonNullElse(issue.getSummary(), ""),
                Objects.requireNonNullElse(issue.getAssignee(), "Unassigned"),
                Objects.requireNonNullElse(issue.getIssueType(), ""),
                Objects.requireNonNullElse(issue.getStatus(), ""),
                nullableIntToString(issue.getTotalStoryPoints()),
                nullableIntToString(issue.getRemainingStoryPoints()),
                nullableIntToString(issue.getDoneStoryPoints())
        };
    }

    private String nullableIntToString(Integer value) {
        return value != null ? value.toString() : "";
    }

    /**
     * Exports capacity planning data to CSV format.
     * Columns: PI, Iteration, Type, Days, Name, Time override, Days of sprint, Effective days
     * One row per member per sprint.
     */
    public String exportCapacityToCsv(CapacityPlanningResponseDto capacity) {
        log.debug("Generating capacity CSV for {} members across {} sprints",
                capacity.getMembers().size(), capacity.getSprints().size());

        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(CAPACITY_CSV_HEADER);

            Map<String, Map<String, Double>> grid = capacity.getDaysOffGrid();
            double totalEffectiveDays = 0.0;
            double totalEffectiveDaysWithoutIp = 0.0;

            for (String sprint : capacity.getSprints()) {
                int businessDays = capacity.getDaysPerSprint().getOrDefault(sprint, 0);
                String pi = extractPi(sprint);
                String iteration = String.valueOf(extractItNum(sprint));
                String sprintType = isIp(sprint) ? "IP" : "IT";
                double sprintEffectiveTotal = 0.0;

                for (TeamMemberDto member : capacity.getMembers()) {
                    Map<String, Double> memberDaysOff = grid.getOrDefault(member.getId(), Map.of());
                    double daysOff = memberDaysOff.getOrDefault(sprint, 0.0);
                    double effectiveDays = computeEffectiveDays(businessDays, daysOff, member.getTimeOverride());
                    sprintEffectiveTotal += effectiveDays;

                    writer.writeNext(new String[]{
                            pi,
                            iteration,
                            sprintType,
                            String.valueOf(businessDays),
                            member.getName(),
                            String.valueOf(member.getTimeOverride()),
                            String.valueOf(member.getTimeOverride()),
                            String.valueOf(daysOff),
                            String.format("%.2f", effectiveDays)
                    });
                }

                totalEffectiveDays += sprintEffectiveTotal;
                if (!isIp(sprint)) {
                    totalEffectiveDaysWithoutIp += sprintEffectiveTotal;
                }
            }

            writer.writeNext(new String[]{});
            writer.writeNext(new String[]{"Label", "Total", "80%", "SP"});
            writer.writeNext(summaryRow("Total", totalEffectiveDays));
            writer.writeNext(summaryRow("Total without IP week", totalEffectiveDaysWithoutIp));
        } catch (Exception e) {
            log.error("Error generating capacity CSV export", e);
            throw new RuntimeException("Failed to generate capacity CSV export", e);
        }

        return sw.toString();
    }

    // Matches "PI26.3" or "PI#26.3" → group 1 = "26.3"
    private static final Pattern PI_DOT_PATTERN  = Pattern.compile("PI#?(\\d+\\.\\d+)");
    // Fallback: matches "PI#26" (no sub-version) → group 1 = "26"
    private static final Pattern PI_HASH_PATTERN = Pattern.compile("PI#(\\d+)");
    // Matches "PI#26.3.1" or "PI26.3.1" → group 1 = "1"
    private static final Pattern IT_DOT_PATTERN  = Pattern.compile("PI#?\\d+\\.\\d+\\.(\\d+)");
    // Fallback: matches "IT1", "IT 1", "Sprint 1" → group 1 = number
    private static final Pattern IT_WORD_PATTERN = Pattern.compile("(?i)(?:IT|Sprint)\\s*(\\d+)");
    private static final Pattern IP_WORD_PATTERN = Pattern.compile("\\bIP\\b");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exports capacity planning data as an Excel (.xlsx) file with two tables and AutoFilter.
     * Table 1 — Summary per sprint: PI | Iteration | Dates | Total (EFT) | 80%
     * Table 2 — Detail per member: PI | Iteration | Type | Days | Name | Time | Time override | Days off sprint | Effective days
     */
    public byte[] exportCapacityToXlsx(CapacityPlanningResponseDto capacity, List<SprintInfo> sprintInfos) {
        log.debug("Generating capacity XLSX for {} members, {} sprints",
                capacity.getMembers().size(), capacity.getSprints().size());

        Map<String, SprintInfo> infoMap = sprintInfos.stream()
                .filter(s -> s.getName() != null)
                .collect(Collectors.toMap(SprintInfo::getName, s -> s, (a, b) -> a));

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            XSSFCellStyle orangeHeader = buildHeaderStyle(wb, new byte[]{(byte)196, (byte)89, (byte)17});
            XSSFCellStyle blueHeader   = buildHeaderStyle(wb, new byte[]{(byte)31,  (byte)78, (byte)121});
            XSSFCellStyle totalStyle   = buildTotalStyle(wb);
            XSSFCellStyle dataStyle    = buildDataStyle(wb);
            XSSFCellStyle numStyle     = buildNumStyle(wb);

            // ── SHEET 1: Summary per sprint ──
            XSSFSheet summarySheet = wb.createSheet("Summary");
            int r1 = 0;

            writeHeaderRow(summarySheet.createRow(r1++),
                    new String[]{"PI", "Iteration", "Dates", "Total", "80%", "SP"}, orangeHeader);

            double grandTotal = 0;
            for (String sprint : capacity.getSprints()) {
                int bizDays = capacity.getDaysPerSprint().getOrDefault(sprint, 0);
                double total = computeSprintTotal(capacity, sprint, bizDays);
                double eightyPercent = total * 0.8;
                int sp = computeStoryPoints(eightyPercent);
                if (!isIp(sprint)) grandTotal += total;

                Row row = summarySheet.createRow(r1++);
                cell(row, 0, extractPi(sprint), dataStyle);
                cell(row, 1, extractItNum(sprint), numStyle);
                cell(row, 2, formatDates(infoMap.get(sprint)), dataStyle);
                cell(row, 3, round2(total), numStyle);
                cell(row, 4, round2(eightyPercent), numStyle);
                cell(row, 5, sp, numStyle);
            }

            int summaryLastDataRow = r1 - 1;

            Row totalRow = summarySheet.createRow(r1++);
            cell(totalRow, 2, "Total without IP week", totalStyle);
            cell(totalRow, 3, round2(grandTotal), totalStyle);
            cell(totalRow, 4, round2(grandTotal * 0.8), totalStyle);
            cell(totalRow, 5, computeStoryPoints(grandTotal * 0.8), totalStyle);

            for (int i = 0; i < 6; i++) summarySheet.autoSizeColumn(i);

            // ── SHEET 2: Detail per member ──
            XSSFSheet detailSheet = wb.createSheet("Detail");
            int r2 = 0;

            writeHeaderRow(detailSheet.createRow(r2++),
                    new String[]{"PI", "Iteration", "Type", "Days", "Name",
                            "Time", "Time override", "Days off sprint", "Effective days"}, blueHeader);

            for (String sprint : capacity.getSprints()) {
                int bizDays    = capacity.getDaysPerSprint().getOrDefault(sprint, 0);
                String pi      = extractPi(sprint);
                int iteration  = extractItNum(sprint);
                String sprintType = isIp(sprint) ? "IP" : "IT";

                for (TeamMemberDto m : capacity.getMembers()) {
                    double daysOff   = capacity.getDaysOffGrid()
                            .getOrDefault(m.getId(), Map.of())
                            .getOrDefault(sprint, 0.0);
                    double effective = computeEffectiveDays(bizDays, daysOff, m.getTimeOverride());

                    Row row = detailSheet.createRow(r2++);
                    cell(row, 0, pi, dataStyle);
                    cell(row, 1, iteration, numStyle);
                    cell(row, 2, sprintType, dataStyle);
                    cell(row, 3, bizDays, numStyle);
                    cell(row, 4, m.getName(), dataStyle);
                    cell(row, 5, m.getTimeOverride(), numStyle);
                    cell(row, 6, m.getTimeOverride(), numStyle);
                    cell(row, 7, daysOff, numStyle);
                    cell(row, 8, round2(effective), numStyle);
                }
            }

            for (int i = 0; i < 9; i++) detailSheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generating capacity XLSX", e);
            throw new RuntimeException("Failed to generate capacity XLSX", e);
        }
    }


    /**
     * Injects slicer XML parts into the XLSX ZIP so Excel shows Iteration and Name slicers
     * on the Detail sheet. Falls back gracefully (returns original bytes) on any error.
     */
    // ── XLSX helpers ──

    private XSSFCellStyle buildHeaderStyle(XSSFWorkbook wb, byte[] rgb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(rgb, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private XSSFCellStyle buildTotalStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(new byte[]{(byte)255, (byte)230, (byte)153}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private XSSFCellStyle buildDataStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBorderRight(BorderStyle.HAIR);
        return s;
    }

    private XSSFCellStyle buildNumStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = buildDataStyle(wb);
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private void writeHeaderRow(Row row, String[] headers, CellStyle style) {
        for (int i = 0; i < headers.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(style);
        }
    }

    private void cell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }

    private void cell(Row row, int col, int value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void cell(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private double computeSprintTotal(CapacityPlanningResponseDto cap, String sprint, int bizDays) {
        return cap.getMembers().stream().mapToDouble(m -> {
            double off = cap.getDaysOffGrid().getOrDefault(m.getId(), Map.of()).getOrDefault(sprint, 0.0);
            return computeEffectiveDays(bizDays, off, m.getTimeOverride());
        }).sum();
    }

    private double computeEffectiveDays(int sprintDays, double daysOff, double timeOverride) {
        double safeSprintDays = Math.max(0, sprintDays);
        double safeDaysOff = Math.max(0.0, daysOff);
        double availableDays = Math.max(0.0, safeSprintDays - safeDaysOff);
        double safeTimeOverride = Math.max(0.0, timeOverride);
        return availableDays * safeTimeOverride;
    }

    private int computeStoryPoints(double effectiveEightyPercentDays) {
        if (effectiveEightyPercentDays <= 0) {
            return 0;
        }
        return (int) Math.round(effectiveEightyPercentDays / SP_EFFECTIVE_DAY_FACTOR);
    }

    private String[] summaryRow(String label, double effectiveTotalDays) {
        double eightyPercent = effectiveTotalDays * 0.8;
        int sp = computeStoryPoints(eightyPercent);
        return new String[]{
                label,
                String.format("%.2f", effectiveTotalDays),
                String.format("%.2f", eightyPercent),
                String.valueOf(sp)
        };
    }

    private String formatDates(SprintInfo info) {
        if (info == null || info.getStartDate() == null) return "";
        String start = info.getStartDate().format(DATE_FMT);
        String end   = info.getEndDate() != null ? info.getEndDate().format(DATE_FMT) : "?";
        return start + " - " + end;
    }

    private String extractPi(String name) {
        Matcher m = PI_DOT_PATTERN.matcher(name);
        if (m.find()) return m.group(1);
        m = PI_HASH_PATTERN.matcher(name);
        return m.find() ? m.group(1) : "";
    }

    private int extractItNum(String name) {
        if (isIp(name)) return 99;
        Matcher m = IT_DOT_PATTERN.matcher(name);
        if (m.find()) return Integer.parseInt(m.group(1));
        m = IT_WORD_PATTERN.matcher(name);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 0;
    }

    private boolean isIp(String name) {
        return IP_WORD_PATTERN.matcher(name).find();
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
