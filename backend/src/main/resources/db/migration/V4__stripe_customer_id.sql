-- Add Stripe customer ID to organizations for billing portal
ALTER TABLE organizations ADD COLUMN stripe_customer_id VARCHAR(255);
