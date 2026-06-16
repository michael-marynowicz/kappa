package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.port.RemainingStoryPointsStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of RemainingStoryPointsStore.
 *
 * Uses ConcurrentHashMap for thread-safety under concurrent requests.
 * Data is lost on application restart — acceptable for MVP.
 *
 * To switch to persistent storage (e.g., PostgreSQL, Redis):
 * 1. Create a new @Component implementing RemainingStoryPointsStore
 * 2. Annotate it with @Primary
 * 3. This in-memory impl will be ignored automatically
 * No service or controller code changes required.
 */
@Slf4j
@Component
public class InMemoryRemainingStoryPointsStore implements RemainingStoryPointsStore {

    private final Map<String, Integer> store = new ConcurrentHashMap<>();

    @Override
    public void save(String issueKey, int remainingStoryPoints) {
        log.debug("Storing remaining SP override: {} -> {}", issueKey, remainingStoryPoints);
        store.put(issueKey, remainingStoryPoints);
    }

    @Override
    public Optional<Integer> find(String issueKey) {
        return Optional.ofNullable(store.get(issueKey));
    }

    @Override
    public Map<String, Integer> findAll() {
        return Collections.unmodifiableMap(store);
    }

    @Override
    public void clear() {
        log.info("Clearing all remaining SP overrides from in-memory store");
        store.clear();
    }
}
