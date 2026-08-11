-- =============================================================================
-- V5 — Persist capacity planning: team members & day-off entries per org
-- =============================================================================

CREATE TABLE capacity_team_members (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    role                VARCHAR(10) NOT NULL CHECK (role IN ('DEV','PDA','QA')),
    time_override       NUMERIC(3,2) NOT NULL DEFAULT 1.00
                        CHECK (time_override >= 0 AND time_override <= 1),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_capacity_team_members_org ON capacity_team_members(organization_id);

CREATE TABLE capacity_day_off_entries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    team_member_id      UUID NOT NULL REFERENCES capacity_team_members(id) ON DELETE CASCADE,
    sprint_name         VARCHAR(200) NOT NULL,
    days_off            NUMERIC(4,1) NOT NULL DEFAULT 0 CHECK (days_off >= 0),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_capacity_entry_member_sprint UNIQUE (team_member_id, sprint_name)
);

CREATE INDEX idx_capacity_entries_org ON capacity_day_off_entries(organization_id);
CREATE INDEX idx_capacity_entries_member ON capacity_day_off_entries(team_member_id);
CREATE INDEX idx_capacity_entries_sprint ON capacity_day_off_entries(organization_id, sprint_name);
