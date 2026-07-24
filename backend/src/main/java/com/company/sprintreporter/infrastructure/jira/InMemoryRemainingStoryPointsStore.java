package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.port.RemainingStoryPointsStore;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of RemainingStoryPointsStore.
 * Kept for tests only — not a Spring component in production.
 */
@Slf4j
public class InMemoryRemainingStoryPointsStore implements RemainingStoryPointsStore {

    private final Map<String, Integer> store = new ConcurrentHashMap<>();

    @Override
    public void save(UUID organizationId, String issueKey, int remainingStoryPoints) {
        log.debug("Storing remaining SP override: {} -> {}", issueKey, remainingStoryPoints);
        store.put(issueKey, remainingStoryPoints);
    }

    @Override
    public Optional<Integer> find(UUID organizationId, String issueKey) {
        return Optional.ofNullable(store.get(issueKey));
    }

    @Override
    public Map<String, Integer> findAll(UUID organizationId) {
        return Collections.unmodifiableMap(store);
    }

    @Override
    public void clear(UUID organizationId) {
        log.info("Clearing all remaining SP overrides from in-memory store");
        store.clear();
    }
}
