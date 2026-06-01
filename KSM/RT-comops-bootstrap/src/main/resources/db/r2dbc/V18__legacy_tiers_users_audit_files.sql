CREATE SCHEMA IF NOT EXISTS file_core;

ALTER TABLE tp.third_party
  ADD COLUMN IF NOT EXISTS accounting_account text,
  ADD COLUMN IF NOT EXISTS active boolean NOT NULL DEFAULT true,
  ADD COLUMN IF NOT EXISTS converted_at timestamptz;

CREATE UNIQUE INDEX IF NOT EXISTS ux_tp_third_party_accounting_account
  ON tp.third_party(tenant_id, organization_id, upper(accounting_account))
  WHERE accounting_account IS NOT NULL;

CREATE TABLE IF NOT EXISTS tp.third_party_bank_account (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  third_party_id uuid NOT NULL,
  label text NOT NULL,
  bank_name text NOT NULL,
  iban text NOT NULL,
  swift_bic text,
  currency text NOT NULL,
  primary_account boolean NOT NULL DEFAULT false,
  UNIQUE (tenant_id, third_party_id, iban)
);

CREATE INDEX IF NOT EXISTS idx_tp_bank_account_third_party
  ON tp.third_party_bank_account(tenant_id, third_party_id);

ALTER TABLE auth_core.user_account
  ADD COLUMN IF NOT EXISTS plan text NOT NULL DEFAULT 'FREE_TIER',
  ADD COLUMN IF NOT EXISTS onboarding_status text NOT NULL DEFAULT 'NOT_STARTED',
  ADD COLUMN IF NOT EXISTS onboarding_step integer NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS kernel.system_audit_entry (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid,
  actor_user_id uuid,
  action text NOT NULL,
  target_type text NOT NULL,
  target_id text NOT NULL,
  payload_summary text NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_kernel_system_audit_user
  ON kernel.system_audit_entry(tenant_id, actor_user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_kernel_system_audit_org
  ON kernel.system_audit_entry(tenant_id, organization_id, created_at DESC);

CREATE TABLE IF NOT EXISTS file_core.stored_file (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid,
  uploaded_by_user_id uuid,
  file_name text NOT NULL,
  content_type text NOT NULL,
  size bigint NOT NULL,
  storage_path text NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_file_core_stored_file_tenant
  ON file_core.stored_file(tenant_id, created_at DESC);
