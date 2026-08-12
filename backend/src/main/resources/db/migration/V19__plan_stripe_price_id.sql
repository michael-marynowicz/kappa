ALTER TABLE plans
ADD COLUMN IF NOT EXISTS stripe_price_id VARCHAR(255);

UPDATE plans
SET stripe_price_id = 'https://buy.stripe.com/6oU9AScEA7ab28I2Wn7ok00'
WHERE code = 'pro';
