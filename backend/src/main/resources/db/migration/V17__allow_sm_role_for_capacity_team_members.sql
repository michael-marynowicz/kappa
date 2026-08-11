-- =============================================================================
-- V17 — Allow Scrum Master role in capacity team members
-- =============================================================================

ALTER TABLE capacity_team_members
    DROP CONSTRAINT IF EXISTS capacity_team_members_role_check;

ALTER TABLE capacity_team_members
    ADD CONSTRAINT capacity_team_members_role_check
    CHECK (role IN ('DEV', 'PDA', 'QA', 'SM'));
