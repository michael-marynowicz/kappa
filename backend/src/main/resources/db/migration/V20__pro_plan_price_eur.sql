-- V20 — Update PRO plan pricing to 99.99 EUR/month
UPDATE plans SET
    price_monthly = 99.99,
    price_yearly  = 999.90
WHERE code = 'pro';
