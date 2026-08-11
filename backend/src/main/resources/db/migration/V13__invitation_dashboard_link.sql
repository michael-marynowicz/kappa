-- V13 — Link invitations to a specific dashboard
--
-- When an admin invites a user for a specific dashboard, the dashboard_id
-- is stored so that after email verification the front-end can redirect
-- the new user directly to that dashboard.
-- NULL = invitation not tied to a specific dashboard (general org invite).

ALTER TABLE invitations ADD COLUMN IF NOT EXISTS dashboard_id UUID
    REFERENCES dashboards(id) ON DELETE SET NULL;
