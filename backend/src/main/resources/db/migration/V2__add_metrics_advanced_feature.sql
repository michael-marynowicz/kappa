-- =============================================================================
-- V2 — Add metrics_advanced feature and assign to PRO/ENTERPRISE plans
-- =============================================================================

-- Add the metrics_advanced feature (average velocity normalized by capacity)
INSERT INTO features (code, name, description) VALUES
    ('metrics_advanced', 'Advanced Metrics', 'Average velocity normalized by capacity, trend analysis across sprints');

-- Assign metrics_advanced to PRO plan
INSERT INTO plan_features (plan_id, feature_id)
SELECT p.id, f.id FROM plans p, features f
WHERE p.code = 'pro' AND f.code = 'metrics_advanced';

-- Assign metrics_advanced to ENTERPRISE plan
INSERT INTO plan_features (plan_id, feature_id)
SELECT p.id, f.id FROM plans p, features f
WHERE p.code = 'enterprise' AND f.code = 'metrics_advanced';
