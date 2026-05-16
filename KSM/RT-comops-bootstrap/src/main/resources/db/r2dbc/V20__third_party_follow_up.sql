ALTER TABLE tp.third_party
    ADD COLUMN IF NOT EXISTS last_contacted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS next_follow_up_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS follow_up_status VARCHAR(32);

CREATE INDEX IF NOT EXISTS idx_third_party_follow_up
    ON tp.third_party (tenant_id, organization_id, follow_up_status, next_follow_up_at);
