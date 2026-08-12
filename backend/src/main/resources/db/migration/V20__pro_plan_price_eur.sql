-- V20 — Update PRO plan pricing to 99.99 EUR/month and cap members at 20
UPDATE plans SET
    price_monthly = 99.99,
    price_yearly  = 999.90,
    max_members   = 20
WHERE code = 'pro';
