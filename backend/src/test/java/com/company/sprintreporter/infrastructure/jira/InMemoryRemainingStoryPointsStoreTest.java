package com.company.sprintreporter.infrastructure.jira;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryRemainingStoryPointsStore")
class InMemoryRemainingStoryPointsStoreTest {

    private InMemoryRemainingStoryPointsStore store;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        store = new InMemoryRemainingStoryPointsStore();
    }

    @Test
    @DisplayName("should return empty when key does not exist")
    void returnsEmptyForUnknownKey() {
        assertThat(store.find(orgId, "SCRUM-999")).isEmpty();
    }

    @Test
    @DisplayName("should persist and retrieve a value")
    void persistsAndRetrievesValue() {
        store.save(orgId, "SCRUM-1", 5);
        assertThat(store.find(orgId, "SCRUM-1")).isEqualTo(Optional.of(5));
    }

    @Test
    @DisplayName("should overwrite existing value on re-save")
    void overwritesExistingValue() {
        store.save(orgId, "SCRUM-1", 5);
        store.save(orgId, "SCRUM-1", 2);
        assertThat(store.find(orgId, "SCRUM-1")).isEqualTo(Optional.of(2));
    }

    @Test
    @DisplayName("should clear all entries")
    void clearsAllEntries() {
        store.save(orgId, "SCRUM-1", 3);
        store.save(orgId, "SCRUM-2", 7);
        store.clear(orgId);
        assertThat(store.findAll(orgId)).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all stored entries")
    void findAllReturnsAllEntries() {
        store.save(orgId, "SCRUM-1", 3);
        store.save(orgId, "SCRUM-2", 7);
        assertThat(store.findAll(orgId))
                .hasSize(2)
                .containsEntry("SCRUM-1", 3)
                .containsEntry("SCRUM-2", 7);
    }
}
