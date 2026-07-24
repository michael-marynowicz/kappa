-- V14 — Allow credentials-only Jira setup
-- project_key is now optional and tied to dashboards, not credentials.

ALTER TABLE jira_configurations
    ALTER COLUMN project_key DROP NOT NULL;
