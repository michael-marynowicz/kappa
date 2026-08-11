-- V16 — Per-user Jira credentials
-- Each user can connect their own Jira account (username + password/token).
-- The org-level JiraConfiguration keeps baseUrl, boardId, projectKey.
-- Credentials are per-user so each person authenticates with their own Jira identity.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS jira_username       VARCHAR(255),
    ADD COLUMN IF NOT EXISTS jira_encrypted_password VARCHAR(1024),
    ADD COLUMN IF NOT EXISTS jira_connected      BOOLEAN NOT NULL DEFAULT FALSE;
