-- Add language preference per user (FR/EN)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS language_preference VARCHAR(5) NOT NULL DEFAULT 'en';
