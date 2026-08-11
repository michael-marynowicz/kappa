package com.company.sprintreporter.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TopicCategoryMapper")
class TopicCategoryMapperTest {

    private final TopicCategoryMapper mapper = new TopicCategoryMapper();

    @ParameterizedTest(name = "raw topic \"{0}\" → category \"{1}\"")
    @CsvSource({
            "Security, Maintenance",
            "Bug, Maintenance",
            "bug fix, Maintenance",
            "Frontend, Product Evolution",
            "feature, Product Evolution",
            "enhancement, Product Evolution",
            "Backend, Implementation",
            "API integration, Implementation",
            "DevOps, Enabler",
            "Refactor, Enabler",
            "tech debt, Enabler",
            "CI/CD pipeline, Enabler",
            "Quality, Validation",
            "QA, Validation",
            "test coverage, Validation",
            "validation, Validation"
    })
    @DisplayName("should map raw topics to correct business categories")
    void mapsTopicsToCategories(String rawTopic, String expectedCategory) {
        assertThat(mapper.mapToCategory(rawTopic)).isEqualTo(expectedCategory);
    }

    @Test
    @DisplayName("should return Other for null topic")
    void returnsOtherForNull() {
        assertThat(mapper.mapToCategory(null)).isEqualTo("Other");
    }

    @Test
    @DisplayName("should return Other for blank topic")
    void returnsOtherForBlank() {
        assertThat(mapper.mapToCategory("  ")).isEqualTo("Other");
    }

    @Test
    @DisplayName("should return Other for unmapped topic")
    void returnsOtherForUnmapped() {
        assertThat(mapper.mapToCategory("random unknown")).isEqualTo("Other");
    }
}
