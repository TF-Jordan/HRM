CREATE TABLE IF NOT EXISTS organization.physical_space (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  parent_space_id uuid,
  code text NOT NULL,
  name text NOT NULL,
  space_type text NOT NULL,
  description text,
  level_number integer,
  capacity integer,
  active boolean NOT NULL DEFAULT true
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_physical_space_code
  ON organization.physical_space (tenant_id, organization_id, agency_id, code);

CREATE INDEX IF NOT EXISTS idx_physical_space_agency_parent
  ON organization.physical_space (tenant_id, organization_id, agency_id, parent_space_id);
