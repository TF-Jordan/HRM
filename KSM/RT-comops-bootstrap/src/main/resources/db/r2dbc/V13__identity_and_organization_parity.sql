ALTER TABLE auth_core.user_account
    ADD COLUMN IF NOT EXISTS password_hash text;

UPDATE auth_core.user_account
SET password_hash = '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi5uLZ0YJrped1IovnHgwlHGawEqTG.'
WHERE password_hash IS NULL;

ALTER TABLE auth_core.user_account
    ALTER COLUMN password_hash SET NOT NULL;

CREATE TABLE IF NOT EXISTS actor.business_actor_profile (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    actor_id uuid NOT NULL,
    name text NOT NULL,
    business_id text,
    niu text,
    trade_registry_number text,
    website text,
    contact_phone text,
    private_address text,
    business_address text,
    business_profile text,
    UNIQUE (tenant_id, actor_id)
);

CREATE TABLE IF NOT EXISTS organization.employee_membership (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    organization_id uuid NOT NULL,
    user_id uuid NOT NULL,
    actor_id uuid,
    email text NOT NULL,
    agency_id uuid,
    role_id uuid,
    status text NOT NULL,
    UNIQUE (tenant_id, organization_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_employee_membership_org
    ON organization.employee_membership (tenant_id, organization_id, status);
