-- V11 — Add subscription_type and pilot_expires_at to subscriptions
-- subscription_type: SELF_SERVICE (default, backward-compatible) | ENTERPRISE | PILOT
-- pilot_expires_at: only relevant when subscription_type = 'PILOT'

ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS subscription_type VARCHAR(20) NOT NULL DEFAULT 'SELF_SERVICE'
        CHECK (subscription_type IN ('SELF_SERVICE','ENTERPRISE','PILOT')),
    ADD COLUMN IF NOT EXISTS pilot_expires_at TIMESTAMPTZ NULL;
