-- Phase 13 — RBAC: 8 standard business roles + Platform Administrator
-- The existing HRM_ADMIN (V67) is retained as the "Admin RH" account.
-- A new PLATFORM_ADMIN user is created who can manage roles + assignments.

-- =========================================================================
-- 1. Standard business roles (TENANT scope)
-- =========================================================================

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES (
    '00000001-0000-0000-0000-000000000100'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'PLATFORM_ADMIN',
    'Administrateur de la plateforme',
    ARRAY[
        'iam:admin','tenant:admin',
        'hrm:employee:create','hrm:employee:read','hrm:employee:update',
        'hrm:employee:terminate','hrm:employee:suspend','hrm:employee:reactivate'
    ]::text[],
    'TENANT'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES (
    '00000001-0000-0000-0000-000000000101'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'DRH',
    'Directeur des Ressources Humaines',
    ARRAY[
        'hrm:employee:create','hrm:employee:read','hrm:employee:update',
        'hrm:employee:terminate','hrm:employee:suspend','hrm:employee:reactivate',
        'hrm:contract:create','hrm:contract:read',
        'hrm:dependent:create','hrm:dependent:read',
        'hrm:training:create','hrm:training:read','hrm:training:manage',
        'hrm:budget:create','hrm:budget:read','hrm:budget:manage',
        'hrm:skill:create','hrm:skill:read',
        'hrm:recruitment:create','hrm:recruitment:read','hrm:recruitment:manage',
        'hrm:onboarding:create','hrm:onboarding:read','hrm:onboarding:manage',
        'hrm:kpi:create','hrm:kpi:read',
        'hrm:review:create','hrm:review:read','hrm:review:manage'
    ]::text[],
    'TENANT'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES (
    '00000001-0000-0000-0000-000000000102'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'RESP_PAIE',
    'Responsable Paie',
    ARRAY[
        'hrm:payroll:run','hrm:payroll:validate','hrm:payroll:read',
        'hrm:declaration:create','hrm:declaration:read','hrm:declaration:manage',
        'hrm:employee:read'
    ]::text[],
    'TENANT'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES (
    '00000001-0000-0000-0000-000000000103'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'COMPTABLE',
    'Comptable',
    ARRAY[
        'hrm:payroll:read','hrm:payroll:validate',
        'hrm:loan:read','hrm:loan:approve',
        'hrm:expense:read','hrm:expense:manage',
        'hrm:employee:read'
    ]::text[],
    'TENANT'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES (
    '00000001-0000-0000-0000-000000000104'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'RECRUTEUR',
    'Recruteur',
    ARRAY[
        'hrm:recruitment:create','hrm:recruitment:read','hrm:recruitment:manage',
        'hrm:onboarding:create','hrm:onboarding:read','hrm:onboarding:manage',
        'hrm:employee:read'
    ]::text[],
    'TENANT'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES (
    '00000001-0000-0000-0000-000000000105'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'MANAGER',
    'Manager d''équipe',
    ARRAY[
        'hrm:leave:read','hrm:leave:approve',
        'hrm:expense:read','hrm:expense:manage',
        'hrm:mission:read','hrm:mission:manage',
        'hrm:review:read','hrm:review:manage',
        'hrm:timesheet:read','hrm:timesheet:validate',
        'hrm:employee:read'
    ]::text[],
    'TENANT'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES (
    '00000001-0000-0000-0000-000000000106'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'MEDECIN',
    'Médecin du travail',
    ARRAY[
        'hrm:medical:create','hrm:medical:read',
        'hrm:employee:read'
    ]::text[],
    'TENANT'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES (
    '00000001-0000-0000-0000-000000000107'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'EMPLOYE',
    'Employé',
    ARRAY[
        'hrm:leave:create','hrm:leave:read',
        'hrm:loan:create','hrm:loan:read',
        'hrm:expense:create','hrm:expense:read',
        'hrm:timesheet:create','hrm:timesheet:read',
        'hrm:training:read',
        'hrm:medical:read',
        'hrm:payroll:read'
    ]::text[],
    'TENANT'
) ON CONFLICT (id) DO NOTHING;

-- =========================================================================
-- 2. Platform Administrator actor + user account
-- Password = "Admin@HRCore2025!" (same as admin RH for simplicity in dev)
-- =========================================================================

INSERT INTO actor.actor (
    id, tenant_id, created_at, updated_at, first_name, last_name, email, profession
) VALUES (
    '00000001-0000-0000-0000-000000000200'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    'Platform', 'Administrator',
    'superadmin@hrcore.local',
    'Platform owner'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO auth_core.user_account (
    id, tenant_id, created_at, updated_at, actor_id,
    username, email, auth_provider, status, password_hash,
    plan, onboarding_status, account_type, mfa_enabled, email_verified_at
) VALUES (
    '00000001-0000-0000-0000-000000000201'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    '00000001-0000-0000-0000-000000000200'::uuid,
    'superadmin@hrcore.local',
    'superadmin@hrcore.local',
    'LOCAL',
    'ACTIVE',
    '$2b$10$rGvfzZlY0TjmENXH0ct6v.J70tbUt.PmYimHVjeGYc5nbt5DAxyoi',
    'FREE_TIER',
    'COMPLETED',
    'BUSINESS',
    false,
    now()
) ON CONFLICT (id) DO NOTHING;

-- 3. PLATFORM_ADMIN role assignment

INSERT INTO roles_core.user_role_assignment (
    id, tenant_id, created_at, updated_at, user_id, role_id, scope, scope_type, scope_id
) VALUES (
    '00000001-0000-0000-0000-000000000202'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    '00000001-0000-0000-0000-000000000201'::uuid,
    '00000001-0000-0000-0000-000000000100'::uuid,
    'TENANT',
    'TENANT',
    NULL
) ON CONFLICT (id) DO NOTHING;

-- 4. Platform admin organisation membership

INSERT INTO organization.employee_membership (
    id, tenant_id, created_at, updated_at, organization_id, user_id, actor_id,
    email, status
) VALUES (
    '00000001-0000-0000-0000-000000000203'::uuid,
    '00000001-0000-0000-0000-000000000001'::uuid,
    now(), now(),
    '00000001-0000-0000-0000-000000000002'::uuid,
    '00000001-0000-0000-0000-000000000201'::uuid,
    '00000001-0000-0000-0000-000000000200'::uuid,
    'superadmin@hrcore.local',
    'ACTIVE'
) ON CONFLICT (id) DO NOTHING;
