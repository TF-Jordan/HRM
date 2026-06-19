-- V100: Demo seed for the standalone-payroll tenant.
--
-- Adds a second organization under the existing tenant ("PAYONLY S.A.") that lives
-- entirely on the payroll-local data path, with:
--   • one role PAYROLL_ADMIN scoped ORGANIZATION (3 hrm:payroll:* permissions),
--   • one demo user payroll.standalone@hrcore.demo (password Demo@2024!) wearing it,
--   • payroll_data_source = LOCAL so payroll-core reads payroll_employee instead of hrm-core,
--   • three pre-imported payroll employees so the user can run a cycle on first login.
--
-- All inserts are idempotent: ON CONFLICT DO NOTHING / DO UPDATE — safe to re-run.

-- ───────────────────────────────────────── Business actor + organization

INSERT INTO actor.actor (
    id, tenant_id, created_at, updated_at,
    first_name, last_name, email, phone_number, gender, nationality
) VALUES (
    '00000000-0000-0000-0000-0000000b0050', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    'PAYONLY', 'S.A.', 'contact@payonly.demo', '+237699000050', 'OTHER', 'CMR')
ON CONFLICT (tenant_id, email) DO NOTHING;

INSERT INTO organization.organization (
    id, tenant_id, created_at, updated_at,
    business_actor_id, code, legal_name, display_name, organization_type
) VALUES (
    '00000000-0000-0000-0000-0000000a0050', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000b0050',
    'PAYONLY', 'PAYONLY S.A.', 'PAYONLY (paie autonome)', 'PRIVATE_COMPANY')
ON CONFLICT (tenant_id, code) DO NOTHING;

-- Subscribe ONLY to the payroll-relevant services. No HRM subscription on purpose:
-- the standalone tenant is fenced out of /api/v1/hrm/* by the entitlement filter.
INSERT INTO organization.organization_service_subscription (
    id, tenant_id, created_at, updated_at, organization_id, service_code
) VALUES
    ('00000000-0000-0000-0000-0000000a0051', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0050', 'ORGANIZATION'),
    ('00000000-0000-0000-0000-0000000a0052', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0050', 'SETTINGS'),
    ('00000000-0000-0000-0000-0000000a0053', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0050', 'PAYROLL')
ON CONFLICT (tenant_id, organization_id, service_code) DO NOTHING;

-- ───────────────────────────────────────── Demo user (login)

INSERT INTO actor.actor (
    id, tenant_id, created_at, updated_at,
    first_name, last_name, email, phone_number, gender, nationality
) VALUES (
    '00000000-0000-0000-0000-0000000b0052', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    'Sandrine', 'Payonly', 'payroll.standalone@hrcore.demo', '+237699000051', 'FEMALE', 'CMR')
ON CONFLICT (tenant_id, email) DO NOTHING;

INSERT INTO auth_core.user_account (
    id, tenant_id, created_at, updated_at, actor_id, username, email,
    auth_provider, status, password_hash, force_password_change
) VALUES (
    '00000000-0000-0000-0000-0000000c0050', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000b0052', 'payroll.standalone', 'payroll.standalone@hrcore.demo',
    'LOCAL', 'ACTIVE',
    -- bcrypt('Demo@2024!', strength=10), same hash reused across V68/V95 demo accounts.
    '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false)
ON CONFLICT (tenant_id, username) DO NOTHING;

-- ───────────────────────────────────────── Role + assignment

INSERT INTO roles_core.role (id, tenant_id, created_at, updated_at, code, name, permissions, scope_type)
VALUES (
    '00000000-0000-0000-0000-0000000d0050', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    'PAYROLL_ADMIN', 'Payroll Administrator (standalone)',
    ARRAY['hrm:payroll:read', 'hrm:payroll:run', 'hrm:payroll:validate'],
    'ORGANIZATION')
ON CONFLICT (tenant_id, code) DO NOTHING;

INSERT INTO roles_core.user_role_assignment (
    id, tenant_id, created_at, updated_at, user_id, role_id, scope, scope_type, scope_id
) VALUES (
    '00000000-0000-0000-0000-0000000e0050', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000c0050', '00000000-0000-0000-0000-0000000d0050',
    'ORGANIZATION:00000000-0000-0000-0000-0000000a0050', 'ORGANIZATION',
    '00000000-0000-0000-0000-0000000a0050')
ON CONFLICT (tenant_id, user_id, role_id, scope) DO NOTHING;

-- ───────────────────────────────────────── Payroll data source: LOCAL

INSERT INTO payroll_data_source (id, tenant_id, created_at, updated_at, organization_id, source)
VALUES (
    '00000000-0000-0000-0000-0000000f0050', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000a0050', 'LOCAL')
ON CONFLICT DO NOTHING;

-- ───────────────────────────────────────── Pre-imported payroll employees

INSERT INTO payroll_employee (
    id, tenant_id, created_at, updated_at, organization_id, agency_id, actor_id,
    matricule, display_name, email, social_security_no, categorie, echelon,
    department_code, hire_date, departure_date, marital_status, dependent_children,
    base_salary, benefits_in_kind, position, payment_channel, account_ref, active
) VALUES
    ('00000000-0000-0000-0000-0000000f1001', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0050', NULL, NULL,
     'PAY-001', 'Jean Mballa', 'jean.mballa@payonly.demo', '123456789', 6, 'B',
     'FIN', DATE '2022-01-15', NULL, 'MARRIED', 2,
     450000, 50000, 'Comptable', 'BANK_TRANSFER', 'CM21-10005-00001-12345678901-23', true),
    ('00000000-0000-0000-0000-0000000f1002', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0050', NULL, NULL,
     'PAY-002', 'Awa Ngono', 'awa.ngono@payonly.demo', '987654321', 4, 'A',
     'ADM', DATE '2023-06-01', NULL, 'SINGLE', 0,
     300000, 0, 'Assistante', 'MTN_MOBILE_MONEY', '677000000', true),
    ('00000000-0000-0000-0000-0000000f1003', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0050', NULL, NULL,
     'PAY-003', 'Paul Etogo', 'paul.etogo@payonly.demo', '555123456', 7, 'C',
     'IT', DATE '2021-09-20', NULL, 'MARRIED', 3,
     650000, 80000, 'Lead Developer', 'BANK_TRANSFER', 'CM21-10005-00001-98765432101-89', true)
ON CONFLICT (tenant_id, organization_id, matricule) DO NOTHING;
