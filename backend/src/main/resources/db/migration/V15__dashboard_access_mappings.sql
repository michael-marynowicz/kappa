-- V15 — Multi-dashboard access mappings for users and invitations

CREATE TABLE IF NOT EXISTS invitation_dashboard_access (
    invitation_id UUID NOT NULL REFERENCES invitations(id) ON DELETE CASCADE,
    dashboard_id UUID NOT NULL REFERENCES dashboards(id) ON DELETE CASCADE,
    PRIMARY KEY (invitation_id, dashboard_id)
);

CREATE TABLE IF NOT EXISTS user_dashboard_access (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dashboard_id UUID NOT NULL REFERENCES dashboards(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, dashboard_id)
);

-- Backfill from legacy single-dashboard invitation mapping.
INSERT INTO invitation_dashboard_access (invitation_id, dashboard_id)
SELECT i.id, i.dashboard_id
FROM invitations i
WHERE i.dashboard_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Grant existing users access to all dashboards in their organization.
INSERT INTO user_dashboard_access (user_id, dashboard_id)
SELECT u.id, d.id
FROM users u
JOIN dashboards d ON d.organization_id = u.organization_id
ON CONFLICT DO NOTHING;
