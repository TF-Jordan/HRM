CREATE TABLE IF NOT EXISTS resource.resource_assignment (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  resource_id uuid NOT NULL,
  assignee_type text NOT NULL,
  assignee_id uuid NOT NULL,
  assigned_at timestamptz NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_resource_assignment_resource_id
  ON resource.resource_assignment (tenant_id, resource_id, assigned_at DESC);

CREATE TABLE IF NOT EXISTS resource.maintenance_record (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  resource_id uuid NOT NULL,
  maintenance_type text NOT NULL,
  description text NOT NULL,
  status text NOT NULL,
  completed_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_maintenance_record_resource_id
  ON resource.maintenance_record (tenant_id, resource_id, created_at DESC);

CREATE TABLE IF NOT EXISTS resource.resource_network_observation (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  resource_id uuid NOT NULL,
  ip_address text,
  mac_address text,
  observed_at timestamptz NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_resource_network_observation_resource_id
  ON resource.resource_network_observation (tenant_id, resource_id, observed_at DESC);

CREATE TABLE IF NOT EXISTS resource.resource_location_observation (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  resource_id uuid NOT NULL,
  latitude double precision NOT NULL,
  longitude double precision NOT NULL,
  observed_at timestamptz NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_resource_location_observation_resource_id
  ON resource.resource_location_observation (tenant_id, resource_id, observed_at DESC);
