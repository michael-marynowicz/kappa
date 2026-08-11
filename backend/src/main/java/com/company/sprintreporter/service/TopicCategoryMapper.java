package com.company.sprintreporter.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Maps raw Jira topics (labels, epic names) to business categories
 * for the iteration review dashboard.
 *
 * Categories:
 * - Validation: testing, QA, quality activities
 * - Product Evolution: features, enhancements, new capabilities
 * - Enabler: tech debt, refactoring, infrastructure
 * - Implementation: core implementation work
 * - Maintenance: bugs, support, fixes
 */
@Component
public class TopicCategoryMapper {

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
            "Validation", List.of("validation", "test", "qa", "quality", "verify", "audit"),
            "Product Evolution", List.of("feature", "enhancement", "evolution", "frontend", "ui", "ux", "design"),
            "Enabler", List.of("tech", "refactor", "enabler", "devops", "ci/cd", "pipeline", "infra", "infrastructure", "migration"),
            "Implementation", List.of("implementation", "backend", "api", "integration", "develop"),
            "Maintenance", List.of("bug", "support", "fix", "hotfix", "patch", "incident", "security")
    );

    private static final String DEFAULT_CATEGORY = "Other";

    /**
     * Maps a raw topic string to a business category.
     */
    public String mapToCategory(String rawTopic) {
        if (rawTopic == null || rawTopic.isBlank()) {
            return DEFAULT_CATEGORY;
        }

        String normalized = rawTopic.toLowerCase().trim();

        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalized.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return DEFAULT_CATEGORY;
    }
}
