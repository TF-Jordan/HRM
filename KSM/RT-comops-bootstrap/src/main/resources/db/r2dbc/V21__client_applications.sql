CREATE TABLE IF NOT EXISTS kernel.client_application (
  id uuid PRIMARY KEY,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  client_id text NOT NULL,
  name text NOT NULL,
  description text,
  secret_hash text NOT NULL,
  status text NOT NULL,
  system_managed boolean NOT NULL DEFAULT false,
  last_authenticated_at timestamptz,
  secret_rotated_at timestamptz
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_kernel_client_application_client_id
  ON kernel.client_application (lower(client_id));

CREATE INDEX IF NOT EXISTS idx_kernel_client_application_status
  ON kernel.client_application (status);
