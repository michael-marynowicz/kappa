-- V12 — Dashboard limits per plan + updated pricing
--
-- max_dashboards:
--   1   = FREE  (1 board only)
--   3   = PRO   (up to 3 boards)
--  -1   = ENTERPRISE (unlimited, -1 = no limit)
--
-- Pricing update:
--   free       →  0$/month  (unchanged)
--   pro        → 99$/month  (was 29)
--   enterprise → 499$/month (was 99)

ALTER TABLE plans ADD COLUMN IF NOT EXISTS max_dashboards INTEGER NOT NULL DEFAULT 1;

-- Set limits per plan
UPDATE plans SET max_dashboards = 1   WHERE code = 'free';
UPDATE plans SET max_dashboards = 3   WHERE code = 'pro';
UPDATE plans SET max_dashboards = -1  WHERE code = 'enterprise';

-- Update pricing
UPDATE plans SET
    price_monthly = 0.00,
    price_yearly  = 0.00
WHERE code = 'free';

UPDATE plans SET
    price_monthly = 99.00,
    price_yearly  = 990.00,
    max_members   = -1
WHERE code = 'pro';

UPDATE plans SET
    price_monthly = 499.00,
    price_yearly  = 4990.00,
    max_members   = -1,
    description   = 'Unlimited dashboards, priority support, manual billing and dedicated onboarding'
WHERE code = 'enterprise';

-- Add multi_board + custom_reports + priority_support to PRO
INSERT INTO plan_features (plan_id, feature_id)
SELECT p.id, f.id
FROM plans p, features f
WHERE p.code = 'pro'
  AND f.code IN ('multi_board', 'iteration_comparison', 'custom_reports')
ON CONFLICT DO NOTHING;
