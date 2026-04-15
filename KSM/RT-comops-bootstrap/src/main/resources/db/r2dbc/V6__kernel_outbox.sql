CREATE SCHEMA IF NOT EXISTS kernel;

CREATE TABLE IF NOT EXISTS kernel.outbox_event (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid,
  event_type text NOT NULL,
  aggregate_type text NOT NULL,
  aggregate_id uuid NOT NULL,
  occurred_at timestamptz NOT NULL,
  payload text NOT NULL,
  published_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_kernel_outbox_event_tenant_created
  ON kernel.outbox_event (tenant_id, created_at);

CREATE INDEX IF NOT EXISTS idx_kernel_outbox_event_aggregate
  ON kernel.outbox_event (aggregate_type, aggregate_id);

CREATE INDEX IF NOT EXISTS idx_kernel_outbox_event_unpublished
  ON kernel.outbox_event (published_at)
  WHERE published_at IS NULL;
