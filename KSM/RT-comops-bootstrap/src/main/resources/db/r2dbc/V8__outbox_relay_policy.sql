ALTER TABLE kernel.outbox_event
  ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  ADD COLUMN IF NOT EXISTS attempt_count INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS last_attempt_at TIMESTAMP WITH TIME ZONE NULL,
  ADD COLUMN IF NOT EXISTS next_attempt_at TIMESTAMP WITH TIME ZONE NULL,
  ADD COLUMN IF NOT EXISTS last_error TEXT NULL,
  ADD COLUMN IF NOT EXISTS dead_lettered_at TIMESTAMP WITH TIME ZONE NULL;

UPDATE kernel.outbox_event
SET status = CASE
    WHEN dead_lettered_at IS NOT NULL THEN 'DEAD_LETTER'
    WHEN published_at IS NOT NULL THEN 'PUBLISHED'
    ELSE 'PENDING'
END,
next_attempt_at = COALESCE(next_attempt_at, created_at)
WHERE status IS NULL
   OR next_attempt_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_kernel_outbox_event_status_next_attempt
  ON kernel.outbox_event (status, next_attempt_at, created_at);

CREATE INDEX IF NOT EXISTS idx_kernel_outbox_event_tenant_status
  ON kernel.outbox_event (tenant_id, status, created_at);
