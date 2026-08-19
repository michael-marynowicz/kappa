-- V21 — Per-user Jira base URL
-- Allows each user to connect Jira without relying on an org-level Jira base URL.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS jira_base_url VARCHAR(512);
