ALTER TABLE tp.third_party
    ADD COLUMN IF NOT EXISTS segment VARCHAR(64),
    ADD COLUMN IF NOT EXISTS qualification_score INTEGER;

ALTER TABLE tp.third_party
    ADD CONSTRAINT ck_third_party_qualification_score_range
        CHECK (qualification_score IS NULL OR (qualification_score >= 0 AND qualification_score <= 100));

CREATE INDEX IF NOT EXISTS idx_third_party_segment
    ON tp.third_party (tenant_id, organization_id, segment);
