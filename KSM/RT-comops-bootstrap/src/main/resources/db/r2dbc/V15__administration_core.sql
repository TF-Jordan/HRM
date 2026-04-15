CREATE SCHEMA IF NOT EXISTS administration;

CREATE TABLE IF NOT EXISTS administration.admin_audit_entry (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    organization_id uuid,
    actor_user_id uuid NOT NULL,
    action text NOT NULL,
    target_type text NOT NULL,
    target_id text NOT NULL,
    payload_summary text NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_entry_tenant_created_at
    ON administration.admin_audit_entry (tenant_id, created_at DESC);
