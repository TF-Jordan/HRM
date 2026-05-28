-- HR Core demo seed: ready-to-use data so `docker compose up` yields a working
-- environment for development & end-to-end testing.
--
-- Idempotent: every INSERT uses ON CONFLICT DO NOTHING so re-runs are safe.
-- Sentinel UUIDs prefixed 00000000-0000-0000-0000-0000000000XX are reserved
-- for fixture data only and must never collide with real production rows.
--
-- Demo credentials (LOCAL auth provider, BCrypt hashes generated with
-- BCryptPasswordEncoder(strength=10)):
--   super.admin@hrcore.demo  / Demo@2024!     -> SUPER_ADMIN (TENANT)
--   hr.admin@hrcore.demo     / Hr@2024Admin   -> HR_ADMIN    (ORGANIZATION)
--   employee@hrcore.demo     / Emp@2024User   -> EMPLOYEE    (ORGANIZATION)
--
-- BFF ClientApplication:
--   client_id   = hrm-frontend
--   secret      = hrm-bff-dev-secret-2024
--   services    = ORGANIZATION, SETTINGS, HRM

-- ============================================================
-- 1. BFF ClientApplication (X-Client-Id + X-Api-Key entry point)
-- ============================================================
INSERT INTO kernel.client_application (
    id, created_at, updated_at, client_id, name, description,
    secret_hash, status, system_managed, allowed_service_codes
) VALUES (
    '00000000-0000-0000-0000-0000000d0001', now(), now(),
    'hrm-frontend', 'HR Core Frontend BFF',
    'Backend-for-frontend serving the HR Core Next.js application (development).',
    '$2a$10$xpgPKCtafeGIg9OcEnglFu27usFtT9YB6YUf304qwF/paLQZOEib6',
    'ACTIVE', false,
    ARRAY['ORGANIZATION', 'SETTINGS', 'HRM']
)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. Demo tenant : 00000000-0000-0000-0000-0000000a0001 (MUFID Union)
-- ============================================================

-- 2.a Actors (identités humaines)
INSERT INTO actor.actor (
    id, tenant_id, created_at, updated_at,
    first_name, last_name, email, phone_number, gender, nationality
) VALUES
    ('00000000-0000-0000-0000-0000000b0001', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Jordan', 'Toulépi', 'super.admin@hrcore.demo', '+237699000001', 'MALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0002', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Marie', 'Ngo', 'hr.admin@hrcore.demo', '+237699000002', 'FEMALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0003', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Jean', 'Dupont', 'employee@hrcore.demo', '+237699000003', 'MALE', 'CMR')
ON CONFLICT (tenant_id, email) DO NOTHING;

-- 2.b Organization
INSERT INTO organization.organization (
    id, tenant_id, created_at, updated_at,
    business_actor_id, code, legal_name, display_name, organization_type
) VALUES (
    '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000b0001',
    'MUFID', 'MUFID UNION S.A.', 'MUFID Union', 'PRIVATE_COMPANY'
)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- 2.c Agency (siège)
INSERT INTO organization.agency (
    id, tenant_id, created_at, updated_at,
    organization_id, code, name, agency_type, active
) VALUES (
    '00000000-0000-0000-0000-0000000a0003', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000a0002', 'HQ', 'Siège Yaoundé', 'HEADQUARTERS', true
)
ON CONFLICT (tenant_id, organization_id, code) DO NOTHING;

-- 2.d Service subscriptions (HRM must be active for /api/v1/hrm/* to pass the entitlement filter)
INSERT INTO organization.organization_service_subscription (
    id, tenant_id, created_at, updated_at, organization_id, service_code
) VALUES
    ('00000000-0000-0000-0000-0000000a0004', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000a0005', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', 'SETTINGS'),
    ('00000000-0000-0000-0000-0000000a0006', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', 'HRM')
ON CONFLICT (tenant_id, organization_id, service_code) DO NOTHING;

-- 2.e User accounts (BCrypt hashes via BCryptPasswordEncoder strength=10)
INSERT INTO auth_core.user_account (
    id, tenant_id, created_at, updated_at, actor_id, username, email,
    auth_provider, status, password_hash, force_password_change
) VALUES
    ('00000000-0000-0000-0000-0000000c0001', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0001', 'super.admin', 'super.admin@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false),
    ('00000000-0000-0000-0000-0000000c0002', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0002', 'hr.admin', 'hr.admin@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$qmrSxSZRx92YrbcCn44W8erdNNE2kxBqinyDwfuytZ0vl1rqhxbEe', false),
    ('00000000-0000-0000-0000-0000000c0003', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0003', 'employee', 'employee@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$EkqFWVymx7BJsALZrbiTeuldtbfg5kjkGBXpakO.oMUJzKooYlgie', false)
ON CONFLICT (tenant_id, username) DO NOTHING;

-- 2.f Roles materialised for the demo tenant
-- (Same definitions as the in-memory templates in
--  RT-comops-administration-core/AdministrationApplicationService.java)
INSERT INTO roles_core.role (
    id, tenant_id, created_at, updated_at, code, name, permissions, scope_type
) VALUES
    ('00000000-0000-0000-0000-0000000d0001', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'SUPER_ADMIN', 'Super Administrator',
     ARRAY[
       'administration:read','administration:write','administration:roles:read','administration:roles:write',
       'administration:roles:clone','administration:permissions:read','administration:assignments:write',
       'administration:settings:read','administration:settings:write','administration:audit:read',
       'administration:govern:business-actors','administration:govern:organizations',
       'administration:govern:agencies',
       'organizations:write','settings:read','settings:write','tenant:admin',
       'hrm:employee:create','hrm:employee:read','hrm:employee:update','hrm:employee:terminate',
       'hrm:employee:suspend','hrm:employee:reactivate','hrm:contract:create','hrm:contract:read',
       'hrm:dependent:create','hrm:dependent:read','hrm:onboarding:create','hrm:onboarding:read',
       'hrm:onboarding:manage','hrm:recruitment:create','hrm:recruitment:read','hrm:recruitment:manage',
       'hrm:payroll:run','hrm:payroll:validate','hrm:payroll:read','hrm:leave:create','hrm:leave:approve',
       'hrm:leave:read','hrm:timesheet:create','hrm:timesheet:validate','hrm:timesheet:read',
       'hrm:expense:create','hrm:expense:manage','hrm:expense:read','hrm:loan:create','hrm:loan:approve',
       'hrm:loan:read','hrm:mission:create','hrm:mission:manage','hrm:mission:read','hrm:medical:create',
       'hrm:medical:read','hrm:review:create','hrm:review:manage','hrm:review:read','hrm:training:create',
       'hrm:training:manage','hrm:training:read','hrm:budget:create','hrm:budget:manage','hrm:budget:read',
       'hrm:skill:create','hrm:skill:read','hrm:declaration:create','hrm:declaration:manage',
       'hrm:declaration:read','hrm:kpi:create','hrm:kpi:read'
     ],
     'TENANT'),
    ('00000000-0000-0000-0000-0000000d0002', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'HR_ADMIN', 'HR Administrator',
     ARRAY[
       'hrm:employee:create','hrm:employee:read','hrm:employee:update','hrm:employee:terminate',
       'hrm:employee:suspend','hrm:employee:reactivate','hrm:contract:create','hrm:contract:read',
       'hrm:dependent:create','hrm:dependent:read','hrm:onboarding:create','hrm:onboarding:read',
       'hrm:onboarding:manage','hrm:recruitment:create','hrm:recruitment:read','hrm:recruitment:manage',
       'hrm:payroll:run','hrm:payroll:validate','hrm:payroll:read','hrm:leave:create','hrm:leave:approve',
       'hrm:leave:read','hrm:timesheet:create','hrm:timesheet:validate','hrm:timesheet:read',
       'hrm:expense:create','hrm:expense:manage','hrm:expense:read','hrm:loan:create','hrm:loan:approve',
       'hrm:loan:read','hrm:mission:create','hrm:mission:manage','hrm:mission:read','hrm:medical:create',
       'hrm:medical:read','hrm:review:create','hrm:review:manage','hrm:review:read','hrm:training:create',
       'hrm:training:manage','hrm:training:read','hrm:budget:create','hrm:budget:manage','hrm:budget:read',
       'hrm:skill:create','hrm:skill:read','hrm:declaration:create','hrm:declaration:manage',
       'hrm:declaration:read','hrm:kpi:create','hrm:kpi:read'
     ],
     'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000d0003', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'HR_DIRECTOR', 'HR Director (DRH)',
     ARRAY[
       'hrm:employee:read','hrm:contract:read','hrm:dependent:read',
       'hrm:training:create','hrm:training:manage','hrm:training:read',
       'hrm:budget:create','hrm:budget:manage','hrm:budget:read',
       'hrm:review:create','hrm:review:manage','hrm:review:read',
       'hrm:skill:create','hrm:skill:read',
       'hrm:recruitment:create','hrm:recruitment:read','hrm:recruitment:manage',
       'hrm:onboarding:create','hrm:onboarding:read','hrm:onboarding:manage',
       'hrm:medical:read','hrm:declaration:read','hrm:kpi:create','hrm:kpi:read',
       'hrm:leave:read','hrm:timesheet:read','hrm:payroll:read'
     ],
     'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000d0004', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'MANAGER', 'Team Manager',
     ARRAY[
       'hrm:employee:read','hrm:contract:read','hrm:dependent:read',
       'hrm:leave:read','hrm:leave:approve',
       'hrm:timesheet:read','hrm:timesheet:validate',
       'hrm:mission:create','hrm:mission:manage','hrm:mission:read',
       'hrm:expense:read','hrm:expense:manage',
       'hrm:review:create','hrm:review:manage','hrm:review:read',
       'hrm:training:read','hrm:skill:read',
       'hrm:recruitment:read','hrm:recruitment:manage'
     ],
     'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000d0005', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'EMPLOYEE', 'Employee (self-service)',
     ARRAY[
       'hrm:employee:read','hrm:contract:read','hrm:dependent:read',
       'hrm:leave:create','hrm:leave:read',
       'hrm:loan:create','hrm:loan:read',
       'hrm:expense:create','hrm:expense:read',
       'hrm:timesheet:create','hrm:timesheet:read',
       'hrm:mission:read','hrm:training:read','hrm:review:read',
       'hrm:medical:read','hrm:payroll:read','hrm:skill:read'
     ],
     'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000d0006', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'RECRUITER', 'Recruiter',
     ARRAY[
       'hrm:recruitment:create','hrm:recruitment:read','hrm:recruitment:manage',
       'hrm:onboarding:create','hrm:onboarding:read','hrm:onboarding:manage',
       'hrm:employee:create','hrm:employee:read','hrm:contract:create','hrm:contract:read',
       'hrm:skill:read'
     ],
     'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000d0007', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'OCCUPATIONAL_DOCTOR', 'Occupational Doctor',
     ARRAY[
       'hrm:medical:create','hrm:medical:read',
       'hrm:employee:read','hrm:contract:read','hrm:dependent:read'
     ],
     'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000d0008', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'HR_CONTROLLER', 'HR Management Controller',
     ARRAY[
       'hrm:employee:read','hrm:contract:read','hrm:dependent:read',
       'hrm:leave:read','hrm:timesheet:read','hrm:mission:read',
       'hrm:payroll:read','hrm:loan:read','hrm:expense:read',
       'hrm:review:read','hrm:training:read','hrm:budget:read',
       'hrm:skill:read','hrm:medical:read','hrm:recruitment:read',
       'hrm:onboarding:read','hrm:declaration:read','hrm:kpi:read','hrm:kpi:create'
     ],
     'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000d0009', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'ACCOUNTANT', 'Accountant',
     ARRAY[
       'accounting:read','accounting:write','accounting:post',
       'hrm:loan:approve','hrm:loan:read',
       'hrm:expense:read','hrm:expense:manage',
       'hrm:payroll:validate','hrm:payroll:read'
     ],
     'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000d000a', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'PAYROLL_MANAGER', 'Payroll Manager',
     ARRAY[
       'hrm:employee:read','hrm:contract:read','hrm:dependent:read','hrm:payroll:run',
       'hrm:payroll:validate','hrm:payroll:read','hrm:leave:read','hrm:timesheet:read',
       'hrm:expense:read','hrm:loan:read','hrm:kpi:read','hrm:declaration:create',
       'hrm:declaration:manage','hrm:declaration:read'
     ],
     'ORGANIZATION')
ON CONFLICT (tenant_id, code) DO NOTHING;

-- 2.g HR_MATRICULE document sequence for the demo organisation. Without it
-- POST /api/v1/hrm/employees crashes with DocumentSequenceNotFoundException.
INSERT INTO settings.document_sequence (
    id, tenant_id, created_at, updated_at, organization_id, agency_id,
    document_type, prefix, suffix, padding_width, next_number
) VALUES (
    '00000000-0000-0000-0000-0000000f0001', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000a0002', NULL,
    'HRM_MATRICULE', 'EMP', NULL, 6, 1
)
ON CONFLICT (tenant_id, organization_id, agency_id, document_type) DO NOTHING;

-- 2.h Role assignments
INSERT INTO roles_core.user_role_assignment (
    id, tenant_id, created_at, updated_at,
    user_id, role_id, scope, scope_type, scope_id
) VALUES
    -- SuperAdmin -> SUPER_ADMIN @ TENANT
    ('00000000-0000-0000-0000-0000000e0001', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0001', '00000000-0000-0000-0000-0000000d0001',
     'TENANT', 'TENANT', NULL),
    -- HR Admin -> HR_ADMIN @ ORGANIZATION
    ('00000000-0000-0000-0000-0000000e0002', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0002', '00000000-0000-0000-0000-0000000d0002',
     'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
     '00000000-0000-0000-0000-0000000a0002'),
    -- Employee -> EMPLOYEE @ ORGANIZATION
    ('00000000-0000-0000-0000-0000000e0003', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0003', '00000000-0000-0000-0000-0000000d0005',
     'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
     '00000000-0000-0000-0000-0000000a0002')
ON CONFLICT (tenant_id, user_id, role_id, scope) DO NOTHING;
