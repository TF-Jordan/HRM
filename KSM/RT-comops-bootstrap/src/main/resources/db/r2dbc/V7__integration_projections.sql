CREATE SCHEMA IF NOT EXISTS integration;

CREATE TABLE IF NOT EXISTS integration.domain_event_projection (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid,
  domain_type text NOT NULL,
  event_type text NOT NULL,
  aggregate_type text NOT NULL,
  aggregate_id uuid NOT NULL,
  business_key text,
  lifecycle_status text,
  occurred_at timestamptz NOT NULL,
  payload text NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_integration_domain_event_projection_tenant_created
  ON integration.domain_event_projection (tenant_id, created_at);

CREATE INDEX IF NOT EXISTS idx_integration_domain_event_projection_domain_type
  ON integration.domain_event_projection (domain_type, event_type);

CREATE INDEX IF NOT EXISTS idx_integration_domain_event_projection_aggregate
  ON integration.domain_event_projection (aggregate_type, aggregate_id);
