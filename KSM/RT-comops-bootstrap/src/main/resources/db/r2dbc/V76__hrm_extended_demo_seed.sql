-- V076: extended demo seed.
-- Adds extra demo user_accounts (one per HR role) so every persona can be tested out
-- of the box, plus a handful of additional hrm_employee records (some with login, some
-- without) so the lists, payroll calc and managerial views have realistic data.
--
-- Demo credentials (all use the single shared password `Demo@2024!` — BCrypt strength
-- 10, hash identical to super.admin seeded in V068 for predictability in CI / demo):
--
--   super.admin@hrcore.demo  / Demo@2024!  (V068)
--   hr.admin@hrcore.demo     / Demo@2024!  (V068 — password reset here for uniformity)
--   manager@hrcore.demo      / Demo@2024!  (new — MANAGER role)
--   drh@hrcore.demo          / Demo@2024!  (new — HR_DIRECTOR)
--   recruiter@hrcore.demo    / Demo@2024!  (new — RECRUITER)
--   accountant@hrcore.demo   / Demo@2024!  (new — ACCOUNTANT)
--   doctor@hrcore.demo       / Demo@2024!  (new — OCCUPATIONAL_DOCTOR)
--   payroll@hrcore.demo      / Demo@2024!  (new — PAYROLL_MANAGER)
--   employee@hrcore.demo     / Demo@2024!  (V068 — password reset here for uniformity)
--
-- HR Admin permissions get extended (`iam:admin`, role-assignment perms) so the BFF
-- orchestration that auto-provisions a login account when creating an employee can
-- succeed under HR Admin's session — without requiring SuperAdmin.

-- ============================================================
-- 1. Extend HR Admin so they can create user accounts in their organization
-- ============================================================
UPDATE roles_core.role
SET permissions = permissions || ARRAY[
        'iam:admin',
        'administration:read',
        'administration:write',
        'administration:roles:read',
        'administration:assignments:write'
    ]::text[],
    updated_at = now()
WHERE code = 'HR_ADMIN'
  AND tenant_id = '00000000-0000-0000-0000-0000000a0001'
  AND NOT (permissions @> ARRAY['iam:admin']::text[]);

UPDATE roles_core.role
SET permissions = permissions || ARRAY[
        'iam:admin',
        'administration:read',
        'administration:roles:read',
        'administration:assignments:write'
    ]::text[],
    updated_at = now()
WHERE code = 'HR_DIRECTOR'
  AND tenant_id = '00000000-0000-0000-0000-0000000a0001'
  AND NOT (permissions @> ARRAY['iam:admin']::text[]);

UPDATE roles_core.role
SET permissions = permissions || ARRAY[
        'iam:admin',
        'administration:read',
        'administration:roles:read',
        'administration:assignments:write'
    ]::text[],
    updated_at = now()
WHERE code = 'RECRUITER'
  AND tenant_id = '00000000-0000-0000-0000-0000000a0001'
  AND NOT (permissions @> ARRAY['iam:admin']::text[]);

-- ============================================================
-- 2. Align the V068 password hashes to the shared Demo@2024! BCrypt hash so the
--    README only ever advertises one password for the demo personas.
-- ============================================================
UPDATE auth_core.user_account
SET password_hash = '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy',
    force_password_change = false,
    updated_at = now()
WHERE tenant_id = '00000000-0000-0000-0000-0000000a0001'
  AND username IN ('super.admin', 'hr.admin', 'employee');

-- ============================================================
-- 3. Additional demo actors (login accounts + employees)
-- ============================================================
INSERT INTO actor.actor (
    id, tenant_id, created_at, updated_at,
    first_name, last_name, email, phone_number, gender, nationality
) VALUES
    -- Login accounts (one per role)
    ('00000000-0000-0000-0000-0000000b0010', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Bernard', 'Foga', 'manager@hrcore.demo', '+237699000010', 'MALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0011', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Faïsal', 'Mbarga', 'drh@hrcore.demo', '+237699000011', 'MALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0012', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Karine', 'Diallo', 'recruiter@hrcore.demo', '+237699000012', 'FEMALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0013', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Pierre', 'Etoa', 'accountant@hrcore.demo', '+237699000013', 'MALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0014', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Sylvie', 'Nguemo', 'doctor@hrcore.demo', '+237699000014', 'FEMALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0015', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Diane', 'Tsoumou', 'payroll@hrcore.demo', '+237699000015', 'FEMALE', 'CMR'),
    -- HR-only employees (no login) — for listings, payroll, dashboard counts
    ('00000000-0000-0000-0000-0000000b0020', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Aminata', 'Diallo', 'aminata.diallo@hrcore.demo', '+237699000020', 'FEMALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0021', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Joseph', 'Kamga', 'joseph.kamga@hrcore.demo', '+237699000021', 'MALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0022', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Léa', 'Ondoa', 'lea.ondoa@hrcore.demo', '+237699000022', 'FEMALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0023', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Marc', 'Kouam', 'marc.kouam@hrcore.demo', '+237699000023', 'MALE', 'CMR'),
    ('00000000-0000-0000-0000-0000000b0024', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     'Yannick', 'Atangana', 'yannick.atangana@hrcore.demo', '+237699000024', 'MALE', 'CMR')
ON CONFLICT (tenant_id, email) DO NOTHING;

-- ============================================================
-- 4. User accounts for the new role-bound personas (all share Demo@2024!)
-- ============================================================
INSERT INTO auth_core.user_account (
    id, tenant_id, created_at, updated_at, actor_id, username, email,
    auth_provider, status, password_hash, force_password_change
) VALUES
    ('00000000-0000-0000-0000-0000000c0010', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0010', 'manager', 'manager@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false),
    ('00000000-0000-0000-0000-0000000c0011', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0011', 'drh', 'drh@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false),
    ('00000000-0000-0000-0000-0000000c0012', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0012', 'recruiter', 'recruiter@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false),
    ('00000000-0000-0000-0000-0000000c0013', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0013', 'accountant', 'accountant@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false),
    ('00000000-0000-0000-0000-0000000c0014', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0014', 'doctor', 'doctor@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false),
    ('00000000-0000-0000-0000-0000000c0015', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000b0015', 'payroll', 'payroll@hrcore.demo',
     'LOCAL', 'ACTIVE',
     '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false)
ON CONFLICT (tenant_id, username) DO NOTHING;

-- ============================================================
-- 5. Role assignments for the new personas
-- ============================================================
INSERT INTO roles_core.user_role_assignment (
    id, tenant_id, created_at, updated_at,
    user_id, role_id, scope, scope_type, scope_id
) VALUES
    -- Manager -> MANAGER @ ORGANIZATION
    ('00000000-0000-0000-0000-0000000e0010', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0010', '00000000-0000-0000-0000-0000000d0004',
     'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
     '00000000-0000-0000-0000-0000000a0002'),
    -- DRH -> HR_DIRECTOR @ ORGANIZATION
    ('00000000-0000-0000-0000-0000000e0011', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0011', '00000000-0000-0000-0000-0000000d0003',
     'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
     '00000000-0000-0000-0000-0000000a0002'),
    -- Recruiter -> RECRUITER @ ORGANIZATION
    ('00000000-0000-0000-0000-0000000e0012', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0012', '00000000-0000-0000-0000-0000000d0006',
     'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
     '00000000-0000-0000-0000-0000000a0002'),
    -- Accountant -> ACCOUNTANT @ ORGANIZATION
    ('00000000-0000-0000-0000-0000000e0013', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0013', '00000000-0000-0000-0000-0000000d0009',
     'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
     '00000000-0000-0000-0000-0000000a0002'),
    -- Doctor -> OCCUPATIONAL_DOCTOR @ ORGANIZATION
    ('00000000-0000-0000-0000-0000000e0014', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0014', '00000000-0000-0000-0000-0000000d0007',
     'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
     '00000000-0000-0000-0000-0000000a0002'),
    -- Payroll -> PAYROLL_MANAGER @ ORGANIZATION
    ('00000000-0000-0000-0000-0000000e0015', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000c0015', '00000000-0000-0000-0000-0000000d000a',
     'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
     '00000000-0000-0000-0000-0000000a0002')
ON CONFLICT (tenant_id, user_id, role_id, scope) DO NOTHING;

-- ============================================================
-- 6. hrm_employee records for the role-bound personas + extra orphan employees.
--    Each role-bound persona becomes an HR-known employee so their dashboards,
--    "mine" pages and payroll calc all resolve.
-- ============================================================
INSERT INTO hrm_employee (
    id, tenant_id, created_at, updated_at, organization_id, agency_id,
    actor_id, matricule, categorie, echelon, date_embauche, status,
    department_code, mode_paiement, num_mobile_money, operateur_mm, actor_display_name
) VALUES
    -- Linked to login accounts
    ('00000000-0000-0000-0000-0000000e0110', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0010', 'EMP-DEMO-010',
     7, 'C', DATE '2022-09-01', 'ACTIVE',
     'OPS', 'BANK_TRANSFER', NULL, NULL, 'Bernard Foga'),
    ('00000000-0000-0000-0000-0000000e0111', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0011', 'EMP-DEMO-011',
     10, 'A', DATE '2021-01-15', 'ACTIVE',
     'HR', 'BANK_TRANSFER', NULL, NULL, 'Faïsal Mbarga'),
    ('00000000-0000-0000-0000-0000000e0112', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0012', 'EMP-DEMO-012',
     6, 'B', DATE '2023-06-01', 'ACTIVE',
     'HR', 'MTN_MOBILE_MONEY', '+237699000012', 'MTN', 'Karine Diallo'),
    ('00000000-0000-0000-0000-0000000e0113', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0013', 'EMP-DEMO-013',
     8, 'B', DATE '2020-04-01', 'ACTIVE',
     'FIN', 'BANK_TRANSFER', NULL, NULL, 'Pierre Etoa'),
    ('00000000-0000-0000-0000-0000000e0114', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0014', 'EMP-DEMO-014',
     9, 'A', DATE '2019-11-01', 'ACTIVE',
     'MED', 'BANK_TRANSFER', NULL, NULL, 'Sylvie Nguemo'),
    ('00000000-0000-0000-0000-0000000e0115', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0015', 'EMP-DEMO-015',
     7, 'B', DATE '2022-02-01', 'ACTIVE',
     'FIN', 'BANK_TRANSFER', NULL, NULL, 'Diane Tsoumou'),
    -- Orphan employees (no login, just HR data)
    ('00000000-0000-0000-0000-0000000e0120', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0020', 'EMP-DEMO-020',
     8, 'A', DATE '2021-07-15', 'ACTIVE',
     'ENG', 'MTN_MOBILE_MONEY', '+237699000020', 'MTN', 'Aminata Diallo'),
    ('00000000-0000-0000-0000-0000000e0121', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0021', 'EMP-DEMO-021',
     5, 'C', DATE '2023-01-10', 'ACTIVE',
     'ENG', 'ORANGE_MONEY', '+237699000021', 'ORANGE', 'Joseph Kamga'),
    ('00000000-0000-0000-0000-0000000e0122', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0022', 'EMP-DEMO-022',
     6, 'B', DATE '2024-03-01', 'ACTIVE',
     'COM', 'BANK_TRANSFER', NULL, NULL, 'Léa Ondoa'),
    ('00000000-0000-0000-0000-0000000e0123', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0023', 'EMP-DEMO-023',
     7, 'A', DATE '2022-05-15', 'ACTIVE',
     'OPS', 'BANK_TRANSFER', NULL, NULL, 'Marc Kouam'),
    ('00000000-0000-0000-0000-0000000e0124', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000b0024', 'EMP-DEMO-024',
     6, 'C', DATE '2023-09-01', 'ACTIVE',
     'SUP', 'MTN_MOBILE_MONEY', '+237699000024', 'MTN', 'Yannick Atangana')
ON CONFLICT (tenant_id, matricule) DO NOTHING;

-- ============================================================
-- 7. Active CDI contracts so payroll & contract lists have meaningful data
-- ============================================================
INSERT INTO hrm_contract (
    id, tenant_id, created_at, updated_at, organization_id, agency_id,
    employee_id, type, date_debut, date_fin, salaire_base, avantages_nature,
    periode_essai, status
) VALUES
    ('00000000-0000-0000-0000-0000000f0110', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0110', 'CDI', DATE '2022-09-01', NULL, 850000, 80000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0111', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0111', 'CDI', DATE '2021-01-15', NULL, 1450000, 150000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0112', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0112', 'CDI', DATE '2023-06-01', NULL, 650000, 50000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0113', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0113', 'CDI', DATE '2020-04-01', NULL, 950000, 100000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0114', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0114', 'CDI', DATE '2019-11-01', NULL, 1250000, 120000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0115', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0115', 'CDI', DATE '2022-02-01', NULL, 850000, 80000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0120', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0120', 'CDI', DATE '2021-07-15', NULL, 750000, 70000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0121', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0121', 'CDD', DATE '2023-01-10', DATE '2026-01-09', 550000, 40000, 60, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0122', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0122', 'CDI', DATE '2024-03-01', NULL, 600000, 50000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0123', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0123', 'CDI', DATE '2022-05-15', NULL, 700000, 60000, 90, 'ACTIVE'),
    ('00000000-0000-0000-0000-0000000f0124', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0124', 'CDI', DATE '2023-09-01', NULL, 580000, 50000, 90, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
