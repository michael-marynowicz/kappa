-- =============================================================================
-- V6 — Persist remaining story point overrides (replaces in-memory store)
-- =============================================================================

CREATE TABLE remaining_sp_overrides (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    issue_key       VARCHAR(50) NOT NULL,
    remaining_sp    INTEGER NOT NULL CHECK (remaining_sp >= 0),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_remaining_sp_org_issue UNIQUE (organization_id, issue_key)
);

CREATE INDEX idx_remaining_sp_org ON remaining_sp_overrides(organization_id);
