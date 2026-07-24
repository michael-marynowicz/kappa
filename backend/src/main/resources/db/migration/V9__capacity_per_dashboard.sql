-- =============================================================================
-- V9 — Scope capacity team members and day-off entries per dashboard
-- =============================================================================

-- Add dashboard_id to capacity_team_members (nullable first for migration)
ALTER TABLE capacity_team_members
    ADD COLUMN dashboard_id UUID REFERENCES dashboards(id) ON DELETE CASCADE;

-- Migrate existing members: assign to the active dashboard of their org (if any)
UPDATE capacity_team_members ctm
SET dashboard_id = (
    SELECT d.id FROM dashboards d
    WHERE d.organization_id = ctm.organization_id
      AND d.active = TRUE
    LIMIT 1
);

-- Delete orphaned members that couldn't be migrated (no dashboard exists yet)
DELETE FROM capacity_team_members WHERE dashboard_id IS NULL;

-- Now make it NOT NULL
ALTER TABLE capacity_team_members
    ALTER COLUMN dashboard_id SET NOT NULL;

-- Replace org-level index with dashboard-level index
DROP INDEX IF EXISTS idx_capacity_team_members_org;
CREATE INDEX idx_capacity_team_members_dashboard ON capacity_team_members(dashboard_id);

-- capacity_day_off_entries already scoped via team_member_id → no schema change needed
-- But update the org index for query patterns
DROP INDEX IF EXISTS idx_capacity_entries_org;
CREATE INDEX idx_capacity_entries_dashboard ON capacity_day_off_entries(team_member_id);
