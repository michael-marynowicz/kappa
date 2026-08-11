-- =============================================================================
-- V3 — Invitation system for multi-user plans (Pro / Enterprise)
-- =============================================================================

-- =============================================================================
-- INVITATIONS — Pre-registered emails by admin, pending user signup
-- =============================================================================
CREATE TABLE invitations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email           VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'MEMBER'
                    CHECK (role IN ('ADMIN','MEMBER','VIEWER')),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','ACCEPTED','REVOKED')),
    invited_by      UUID REFERENCES users(id) ON DELETE SET NULL,
    accepted_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invitations_email ON invitations(email);
CREATE INDEX idx_invitations_org_status ON invitations(organization_id, status);
CREATE UNIQUE INDEX idx_invitations_org_email_pending ON invitations(organization_id, email)
    WHERE status = 'PENDING';

-- =============================================================================
-- EMAIL_VERIFICATIONS — Token-based email verification
-- =============================================================================
CREATE TABLE email_verifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_email_verifications_user ON email_verifications(user_id);
