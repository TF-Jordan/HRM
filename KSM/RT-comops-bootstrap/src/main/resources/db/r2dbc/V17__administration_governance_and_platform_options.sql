CREATE TABLE IF NOT EXISTS administration.administrative_platform_options (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL UNIQUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    require_business_actor_approval boolean NOT NULL DEFAULT true,
    require_organization_approval boolean NOT NULL DEFAULT true,
    allow_organization_self_service_creation boolean NOT NULL DEFAULT true,
    allow_agency_self_service_creation boolean NOT NULL DEFAULT false,
    allow_role_cloning boolean NOT NULL DEFAULT true,
    allow_agency_scoped_custom_roles boolean NOT NULL DEFAULT true,
    allow_organization_admins_to_govern_agencies boolean NOT NULL DEFAULT true,
    allow_business_actor_self_reactivation boolean NOT NULL DEFAULT false
);

ALTER TABLE actor.business_actor_profile
    ADD COLUMN IF NOT EXISTS governance_status varchar(32) NOT NULL DEFAULT 'PENDING_REVIEW',
    ADD COLUMN IF NOT EXISTS governed_by_user_id uuid,
    ADD COLUMN IF NOT EXISTS governed_at timestamptz,
    ADD COLUMN IF NOT EXISTS governance_reason text;

ALTER TABLE organization.organization
    ADD COLUMN IF NOT EXISTS governance_status varchar(32) NOT NULL DEFAULT 'PENDING_APPROVAL',
    ADD COLUMN IF NOT EXISTS governed_by_user_id uuid,
    ADD COLUMN IF NOT EXISTS governed_at timestamptz,
    ADD COLUMN IF NOT EXISTS governance_reason text;

ALTER TABLE organization.agency
    ADD COLUMN IF NOT EXISTS governance_status varchar(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS governed_by_user_id uuid,
    ADD COLUMN IF NOT EXISTS governed_at timestamptz,
    ADD COLUMN IF NOT EXISTS governance_reason text;

CREATE INDEX IF NOT EXISTS idx_business_actor_profile_governance
    ON actor.business_actor_profile (tenant_id, governance_status);

CREATE INDEX IF NOT EXISTS idx_organization_governance
    ON organization.organization (tenant_id, governance_status);

CREATE INDEX IF NOT EXISTS idx_agency_governance
    ON organization.agency (tenant_id, organization_id, governance_status);
