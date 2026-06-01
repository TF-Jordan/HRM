ALTER TABLE resource.resource_assignment
  ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE resource.resource_assignment
  ADD COLUMN IF NOT EXISTS unassigned_at timestamptz;

CREATE TABLE IF NOT EXISTS resource.resource_reservation (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  resource_id uuid NOT NULL,
  reservee_type text NOT NULL,
  reservee_id uuid NOT NULL,
  reason text NOT NULL,
  reserved_at timestamptz NOT NULL,
  status text NOT NULL,
  released_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_resource_reservation_resource_id
  ON resource.resource_reservation (tenant_id, resource_id, reserved_at DESC);

CREATE INDEX IF NOT EXISTS idx_resource_reservation_active
  ON resource.resource_reservation (tenant_id, resource_id, status);
