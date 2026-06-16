package com.company.sprintreporter.infrastructure.jira;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryRemainingStoryPointsStore")
class InMemoryRemainingStoryPointsStoreTest {

    private InMemoryRemainingStoryPointsStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryRemainingStoryPointsStore();
    }

    @Test
    @DisplayName("should return empty when key does not exist")
    void returnsEmptyForUnknownKey() {
        assertThat(store.find("SCRUM-999")).isEmpty();
    }

    @Test
    @DisplayName("should persist and retrieve a value")
    void persistsAndRetrievesValue() {
        store.save("SCRUM-1", 5);
        assertThat(store.find("SCRUM-1")).isEqualTo(Optional.of(5));
    }

    @Test
    @DisplayName("should overwrite existing value on re-save")
    void overwritesExistingValue() {
        store.save("SCRUM-1", 5);
        store.save("SCRUM-1", 2);
        assertThat(store.find("SCRUM-1")).isEqualTo(Optional.of(2));
    }

    @Test
    @DisplayName("should clear all entries")
    void clearsAllEntries() {
        store.save("SCRUM-1", 3);
        store.save("SCRUM-2", 7);
        store.clear();
        assertThat(store.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all stored entries")
    void findAllReturnsAllEntries() {
        store.save("SCRUM-1", 3);
        store.save("SCRUM-2", 7);
        assertThat(store.findAll())
                .hasSize(2)
                .containsEntry("SCRUM-1", 3)
                .containsEntry("SCRUM-2", 7);
    }
}
