-- =============================================================================
-- V1 — Kappa SaaS: Multi-tenant schema
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- ORGANIZATIONS — Root tenant entity
-- =============================================================================
CREATE TABLE organizations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(255),
    logo_url        VARCHAR(512),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_organizations_slug ON organizations(slug);

-- =============================================================================
-- PLANS — Billing plans (FREE / PRO / ENTERPRISE)
-- =============================================================================
CREATE TABLE plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50) NOT NULL UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    description     TEXT,
    max_members     INTEGER NOT NULL DEFAULT 5,
    price_monthly   NUMERIC(10, 2) NOT NULL DEFAULT 0,
    price_yearly    NUMERIC(10, 2) NOT NULL DEFAULT 0,
    trial_days      INTEGER NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- FEATURES — Granular feature toggles
-- =============================================================================
CREATE TABLE features (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- PLAN_FEATURES — N:N plan ↔ features
-- =============================================================================
CREATE TABLE plan_features (
    plan_id     UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    feature_id  UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    PRIMARY KEY (plan_id, feature_id)
);

-- =============================================================================
-- SUBSCRIPTIONS — Organization-level, provider-agnostic
-- =============================================================================
CREATE TABLE subscriptions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id         UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    plan_id                 UUID NOT NULL REFERENCES plans(id) ON DELETE RESTRICT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'TRIALING'
                            CHECK (status IN ('ACTIVE','TRIALING','PAST_DUE','CANCELED','EXPIRED')),
    billing_provider        VARCHAR(30) NOT NULL DEFAULT 'NONE'
                            CHECK (billing_provider IN ('NONE','STRIPE','PADDLE','MANUAL')),
    provider_subscription_id VARCHAR(255),
    current_period_start    TIMESTAMPTZ NOT NULL,
    current_period_end      TIMESTAMPTZ,
    cancel_at_period_end    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_subscriptions_org UNIQUE (organization_id)
);

CREATE INDEX idx_subscriptions_status ON subscriptions(status);

-- =============================================================================
-- PAYMENT_CUSTOMER_REFERENCES — External billing provider customer mapping
-- =============================================================================
CREATE TABLE payment_customer_references (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    provider            VARCHAR(30) NOT NULL CHECK (provider IN ('STRIPE','PADDLE','MANUAL')),
    provider_customer_id VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_payment_customer_org_provider UNIQUE (organization_id, provider)
);

-- =============================================================================
-- USERS — Belong to an organization with a role
-- =============================================================================
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    role                VARCHAR(20) NOT NULL DEFAULT 'MEMBER'
                        CHECK (role IN ('ADMIN','MEMBER','VIEWER')),
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_organization ON users(organization_id);
CREATE UNIQUE INDEX idx_users_email_lower ON users(LOWER(email));

-- =============================================================================
-- REFRESH_TOKENS — For JWT refresh flow
-- =============================================================================
CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at) WHERE revoked = FALSE;

-- =============================================================================
-- PROMO_CODES — Flexible promo system
-- =============================================================================
CREATE TABLE promo_codes (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                    VARCHAR(50) NOT NULL UNIQUE,
    discount_type           VARCHAR(20) NOT NULL
                            CHECK (discount_type IN ('PERCENT','FIXED','TRIAL_EXTENSION')),
    discount_value          NUMERIC(10, 2) NOT NULL CHECK (discount_value > 0),
    plan_id                 UUID REFERENCES plans(id) ON DELETE SET NULL,
    max_redemptions         INTEGER NOT NULL DEFAULT 1,
    current_redemptions     INTEGER NOT NULL DEFAULT 0,
    valid_from              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valid_until             TIMESTAMPTZ NOT NULL,
    organization_id         UUID REFERENCES organizations(id) ON DELETE SET NULL,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_promo_dates CHECK (valid_until > valid_from),
    CONSTRAINT chk_promo_redemptions CHECK (current_redemptions <= max_redemptions)
);

CREATE UNIQUE INDEX idx_promo_codes_code_upper ON promo_codes(UPPER(code));

-- =============================================================================
-- PROMO_REDEMPTIONS — Audit trail for promo usage
-- =============================================================================
CREATE TABLE promo_redemptions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    promo_code_id       UUID NOT NULL REFERENCES promo_codes(id) ON DELETE CASCADE,
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id     UUID REFERENCES subscriptions(id) ON DELETE SET NULL,
    redeemed_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_promo_redemption_org UNIQUE (promo_code_id, organization_id)
);

-- =============================================================================
-- TEAMS — Within an organization
-- =============================================================================
CREATE TABLE teams (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_team_name_org UNIQUE (organization_id, name)
);

CREATE INDEX idx_teams_organization ON teams(organization_id);

-- =============================================================================
-- TEAM_MEMBERS — N:N user ↔ teams
-- =============================================================================
CREATE TABLE team_members (
    team_id     UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

-- =============================================================================
-- JIRA_CONFIGURATIONS — Per-organization Jira integration
-- =============================================================================
CREATE TABLE jira_configurations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    base_url            VARCHAR(512) NOT NULL,
    auth_type           VARCHAR(20) NOT NULL DEFAULT 'PAT'
                        CHECK (auth_type IN ('PAT','BASIC','OAUTH2')),
    user_email          VARCHAR(255),
    encrypted_token     VARCHAR(1024) NOT NULL,
    project_key         VARCHAR(50) NOT NULL,
    board_id            INTEGER,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    last_sync_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_jira_config_org UNIQUE (organization_id)
);

-- =============================================================================
-- SPRINTS — Tracked per organization
-- =============================================================================
CREATE TABLE sprints (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    external_id         VARCHAR(100),
    name                VARCHAR(200) NOT NULL,
    goal                TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('FUTURE','ACTIVE','CLOSED')),
    start_date          DATE,
    end_date            DATE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sprints_organization ON sprints(organization_id);
CREATE INDEX idx_sprints_status ON sprints(organization_id, status);

-- =============================================================================
-- BACKLOG_ITEMS — Issues/stories within a sprint
-- =============================================================================
CREATE TABLE backlog_items (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id         UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    sprint_id               UUID REFERENCES sprints(id) ON DELETE SET NULL,
    external_key            VARCHAR(50),
    summary                 VARCHAR(500) NOT NULL,
    issue_type              VARCHAR(50),
    status                  VARCHAR(50) NOT NULL DEFAULT 'To Do',
    assignee                VARCHAR(200),
    story_points            NUMERIC(5,1),
    remaining_story_points  NUMERIC(5,1),
    topic                   VARCHAR(100),
    priority                VARCHAR(20),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_backlog_items_org ON backlog_items(organization_id);
CREATE INDEX idx_backlog_items_sprint ON backlog_items(sprint_id);
CREATE INDEX idx_backlog_items_external_key ON backlog_items(organization_id, external_key);

-- =============================================================================
-- CAPACITY_PLANS — Team capacity for a sprint
-- =============================================================================
CREATE TABLE capacity_plans (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    sprint_id           UUID NOT NULL REFERENCES sprints(id) ON DELETE CASCADE,
    team_id             UUID REFERENCES teams(id) ON DELETE SET NULL,
    planned_capacity    NUMERIC(6,1) NOT NULL DEFAULT 0,
    real_capacity       NUMERIC(6,1) NOT NULL DEFAULT 0,
    days_off            NUMERIC(5,1) NOT NULL DEFAULT 0,
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_capacity_sprint_team UNIQUE (sprint_id, team_id)
);

CREATE INDEX idx_capacity_plans_org ON capacity_plans(organization_id);

-- =============================================================================
-- METRIC_SNAPSHOTS — Computed sprint metrics for history
-- =============================================================================
CREATE TABLE metric_snapshots (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id         UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    sprint_id               UUID NOT NULL REFERENCES sprints(id) ON DELETE CASCADE,
    committed_points        NUMERIC(6,1) NOT NULL DEFAULT 0,
    delivered_points        NUMERIC(6,1) NOT NULL DEFAULT 0,
    velocity                NUMERIC(6,1) NOT NULL DEFAULT 0,
    ratio                   NUMERIC(5,2) NOT NULL DEFAULT 0,
    scope_change_points     NUMERIC(6,1) NOT NULL DEFAULT 0,
    snapshot_date           DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_metric_snapshot_sprint UNIQUE (sprint_id, snapshot_date)
);

CREATE INDEX idx_metric_snapshots_org ON metric_snapshots(organization_id);

-- =============================================================================
-- SEED: Plans & Features
-- =============================================================================
INSERT INTO features (code, name, description) VALUES
    ('sprint_tracking', 'Sprint Tracking', 'Basic sprint backlog tracking'),
    ('metrics_dashboard', 'Metrics Dashboard', 'Sprint velocity and KPI metrics'),
    ('csv_export', 'CSV Export', 'Export reports as CSV'),
    ('jira_integration', 'Jira Integration', 'Connect to Jira and sync data'),
    ('iteration_comparison', 'Iteration Comparison', 'Compare metrics across sprints'),
    ('capacity_planning', 'Capacity Planning', 'Team capacity and availability planning'),
    ('multi_board', 'Multi-Board Support', 'Track multiple Jira boards'),
    ('api_access', 'REST API Access', 'Programmatic API access'),
    ('custom_reports', 'Custom Reports', 'Create custom report templates'),
    ('priority_support', 'Priority Support', '24h response time support');

INSERT INTO plans (code, display_name, description, max_members, price_monthly, price_yearly, trial_days) VALUES
    ('free', 'Free', 'Get started with basic sprint tracking', 3, 0, 0, 0),
    ('pro', 'Pro', 'Full-featured sprint reporting for growing teams', 20, 29.00, 290.00, 14),
    ('enterprise', 'Enterprise', 'Unlimited access with priority support and custom integrations', 1000, 99.00, 990.00, 14);

-- Free: basic tracking + metrics
INSERT INTO plan_features (plan_id, feature_id)
SELECT p.id, f.id FROM plans p, features f
WHERE p.code = 'free' AND f.code IN ('sprint_tracking', 'metrics_dashboard');

-- Pro: all except priority support & custom reports
INSERT INTO plan_features (plan_id, feature_id)
SELECT p.id, f.id FROM plans p, features f
WHERE p.code = 'pro' AND f.code IN (
    'sprint_tracking', 'metrics_dashboard', 'csv_export',
    'jira_integration', 'iteration_comparison', 'capacity_planning', 'api_access'
);

-- Enterprise: everything
INSERT INTO plan_features (plan_id, feature_id)
SELECT p.id, f.id FROM plans p, features f
WHERE p.code = 'enterprise';
