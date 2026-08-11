-- =============================================================================
-- V8 — Dashboards: multiple boards per organization
-- =============================================================================

CREATE TABLE dashboards (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    board_id        INTEGER NOT NULL,
    project_key     VARCHAR(50) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT FALSE,
    position        INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_dashboard_board_org UNIQUE (organization_id, board_id)
);

CREATE INDEX idx_dashboards_org ON dashboards(organization_id);
CREATE INDEX idx_dashboards_org_active ON dashboards(organization_id, active);
