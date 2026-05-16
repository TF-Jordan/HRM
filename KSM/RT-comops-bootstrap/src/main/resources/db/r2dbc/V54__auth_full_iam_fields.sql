ALTER TABLE auth_core.user_account
    ADD COLUMN IF NOT EXISTS phone_number text,
    ADD COLUMN IF NOT EXISTS phone_verified_at timestamptz,
    ADD COLUMN IF NOT EXISTS mfa_enabled boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS mfa_channel text,
    ADD COLUMN IF NOT EXISTS external_subject text,
    ADD COLUMN IF NOT EXISTS account_type text NOT NULL DEFAULT 'PROSPECT',
    ADD COLUMN IF NOT EXISTS business_type text,
    ADD COLUMN IF NOT EXISTS onboarding_payload text;

CREATE INDEX IF NOT EXISTS idx_user_account_phone_number
    ON auth_core.user_account (tenant_id, phone_number)
    WHERE phone_number IS NOT NULL;
