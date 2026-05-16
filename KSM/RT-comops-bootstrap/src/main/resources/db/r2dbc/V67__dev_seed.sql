-- Dev seed: minimal tenant + organisation + HRM subscription + admin user
-- with all hrm:* permissions, for local development of the HRM frontend.
-- IDs are deterministic to make the data referenceable from frontend .env / tests.

-- =========================================================================
-- Tenant / organisation
-- =========================================================================

INSERT INTO actor.actor (
    id, tenant_id, created_at, updated_at, first_name, last_name, email, profession
)
VALUES (
    '00000001-0000-0000-0000-000000000008'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'HR', 'Core',
    'owner@hrcore.local',
    'Organization owner'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO organization.organization (
    id, tenant_id, created_at, updated_at, business_actor_id,
    code, legal_name, display_name, organization_type, governance_status
)
VALUES (
    '00000001-0000-0000-0000-000000000002'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    '00000001-0000-0000-0000-000000000008'::uuid,
    'HRCORE',
    'HR Core Demo Sarl',
    'HR Core Demo',
    'PRIVATE_COMPANY',
    'APPROVED'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO organization.organization_service_subscription (
    id, tenant_id, created_at, updated_at, organization_id, service_code
)
VALUES (
    '00000001-0000-0000-0000-000000000007'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    '00000001-0000-0000-0000-000000000002'::uuid,
    'HRM'
)
ON CONFLICT (id) DO NOTHING;

-- =========================================================================
-- Admin actor + user account
-- Password = "Admin@HRCore2025!" (BCrypt strength 10)
-- =========================================================================

INSERT INTO actor.actor (
    id, tenant_id, created_at, updated_at, first_name, last_name, email, profession
)
VALUES (
    '00000001-0000-0000-0000-000000000003'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'Admin', 'RH',
    'admin@hrcore.local',
    'HR Administrator'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO auth_core.user_account (
    id, tenant_id, created_at, updated_at, actor_id,
    username, email, auth_provider, status, password_hash,
    plan, onboarding_status, account_type, mfa_enabled, email_verified_at
)
VALUES (
    '00000001-0000-0000-0000-000000000004'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    '00000001-0000-0000-0000-000000000003'::uuid,
    'admin@hrcore.local',
    'admin@hrcore.local',
    'LOCAL',
    'ACTIVE',
    '$2b$10$rGvfzZlY0TjmENXH0ct6v.J70tbUt.PmYimHVjeGYc5nbt5DAxyoi',
    'FREE_TIER',
    'COMPLETED',
    'BUSINESS',
    false,
    now()
)
ON CONFLICT (id) DO NOTHING;

-- =========================================================================
-- HRM full-access role + assignment at TENANT scope
-- =========================================================================

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
)
VALUES (
    '00000001-0000-0000-0000-000000000005'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'HRM_ADMIN',
    'HRM Administrator',
    ARRAY[
        'hrm:employee:create','hrm:employee:read','hrm:employee:update',
        'hrm:employee:terminate','hrm:employee:suspend','hrm:employee:reactivate',
        'hrm:contract:create','hrm:contract:read',
        'hrm:dependent:create','hrm:dependent:read',
        'hrm:leave:create','hrm:leave:read','hrm:leave:approve',
        'hrm:loan:create','hrm:loan:read','hrm:loan:approve',
        'hrm:payroll:run','hrm:payroll:validate','hrm:payroll:read',
        'hrm:expense:create','hrm:expense:read','hrm:expense:manage',
        'hrm:medical:create','hrm:medical:read',
        'hrm:mission:create','hrm:mission:read','hrm:mission:manage',
        'hrm:recruitment:create','hrm:recruitment:read','hrm:recruitment:manage',
        'hrm:onboarding:create','hrm:onboarding:read','hrm:onboarding:manage',
        'hrm:review:create','hrm:review:read','hrm:review:manage',
        'hrm:skill:create','hrm:skill:read',
        'hrm:timesheet:create','hrm:timesheet:read','hrm:timesheet:validate',
        'hrm:training:create','hrm:training:read','hrm:training:manage',
        'hrm:budget:create','hrm:budget:read','hrm:budget:manage',
        'hrm:declaration:create','hrm:declaration:read','hrm:declaration:manage',
        'hrm:kpi:create','hrm:kpi:read'
    ]::text[],
    'TENANT'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO roles_core.user_role_assignment (
    id, tenant_id, created_at, updated_at, user_id, role_id, scope, scope_type, scope_id
)
VALUES (
    '00000001-0000-0000-0000-000000000006'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    '00000001-0000-0000-0000-000000000004'::uuid,
    '00000001-0000-0000-0000-000000000005'::uuid,
    'TENANT',
    'TENANT',
    NULL
)
ON CONFLICT (id) DO NOTHING;

-- =========================================================================
-- Employee membership: ties admin user to the seeded organisation so that
-- auth discover-contexts surfaces it under his accessible organisations.
-- =========================================================================

INSERT INTO organization.employee_membership (
    id, tenant_id, created_at, updated_at, organization_id, user_id, actor_id,
    email, status
)
VALUES (
    '00000001-0000-0000-0000-000000000009'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    '00000001-0000-0000-0000-000000000002'::uuid,
    '00000001-0000-0000-0000-000000000004'::uuid,
    '00000001-0000-0000-0000-000000000003'::uuid,
    'admin@hrcore.local',
    'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;
