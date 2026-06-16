package com.company.sprintreporter.domain.port;

import java.util.Map;
import java.util.Optional;

/**
 * Outbound port: defines how remaining story point overrides are stored.
 * Decoupled from the storage mechanism (in-memory, DB, Redis, etc.)
 * The default implementation is in-memory; swap later for DB without changing the domain.
 */
public interface RemainingStoryPointsStore {

    /**
     * Persist an override for a given issue key.
     */
    void save(String issueKey, int remainingStoryPoints);

    /**
     * Retrieve a previously saved override if it exists.
     */
    Optional<Integer> find(String issueKey);

    /**
     * Return all stored overrides (key → remaining SP).
     */
    Map<String, Integer> findAll();

    /**
     * Remove all stored overrides (useful for test teardown or reset endpoint).
     */
    void clear();
}
