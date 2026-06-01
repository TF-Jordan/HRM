ALTER TABLE integration.domain_event_projection
  ADD COLUMN IF NOT EXISTS source_event_id uuid;

UPDATE integration.domain_event_projection
SET source_event_id = id
WHERE source_event_id IS NULL;

ALTER TABLE integration.domain_event_projection
  ALTER COLUMN source_event_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_integration_domain_event_projection_source_event
  ON integration.domain_event_projection (source_event_id);
