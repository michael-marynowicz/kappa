-- =============================================================================
-- V7 — Add OAuth 2.0 token fields to jira_configurations
-- =============================================================================

ALTER TABLE jira_configurations
    ADD COLUMN oauth_refresh_token VARCHAR(2048),
    ADD COLUMN oauth_token_expiry  TIMESTAMPTZ,
    ADD COLUMN oauth_cloud_id      VARCHAR(255);
