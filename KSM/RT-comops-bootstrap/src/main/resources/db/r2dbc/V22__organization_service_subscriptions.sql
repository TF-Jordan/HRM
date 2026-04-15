CREATE TABLE IF NOT EXISTS organization.organization_service_subscription (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL REFERENCES organization.organization(id) ON DELETE CASCADE,
  service_code text NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_organization_service_subscription_org_service
  ON organization.organization_service_subscription (tenant_id, organization_id, service_code);

CREATE INDEX IF NOT EXISTS idx_organization_service_subscription_org
  ON organization.organization_service_subscription (tenant_id, organization_id);
