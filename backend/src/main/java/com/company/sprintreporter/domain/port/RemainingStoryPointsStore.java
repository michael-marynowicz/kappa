package com.company.sprintreporter.domain.port;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: defines how remaining story point overrides are stored.
 * Decoupled from the storage mechanism (in-memory, DB, Redis, etc.)
 */
public interface RemainingStoryPointsStore {

    /**
     * Persist an override for a given issue key within an organization.
     */
    void save(UUID organizationId, String issueKey, int remainingStoryPoints);

    /**
     * Retrieve a previously saved override if it exists.
     */
    Optional<Integer> find(UUID organizationId, String issueKey);

    /**
     * Return all stored overrides for an organization (key → remaining SP).
     */
    Map<String, Integer> findAll(UUID organizationId);

    /**
     * Remove all stored overrides for an organization.
     */
    void clear(UUID organizationId);
}
