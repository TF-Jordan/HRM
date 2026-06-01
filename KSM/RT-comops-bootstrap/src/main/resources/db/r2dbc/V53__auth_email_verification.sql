ALTER TABLE auth_core.user_account
    ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMP;
