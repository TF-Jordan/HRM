-- V095: Consolidate demo roles after frontend role merge.
--
-- The frontend no longer has dedicated workspaces for MANAGER, HR_DIRECTOR,
-- RECRUITER and ACCOUNTANT — they all map to the HR-ADMIN space. We remove the
-- demo login accounts for those four personas (their hrm_employee + contract
-- records are kept as useful data for listings, payroll, etc.).
--
-- A new HR_CONTROLLER account is added so every surviving frontend role has a
-- testable login.
--
-- Remaining demo accounts (all use password `Demo@2024!`):
--
--   super.admin@hrcore.demo   -> SUPER_ADMIN          (admin)
--   hr.admin@hrcore.demo      -> HR_ADMIN              (hr-admin)
--   payroll@hrcore.demo       -> PAYROLL_MANAGER        (payroll-manager)
--   employee@hrcore.demo      -> EMPLOYEE               (employee)
--   doctor@hrcore.demo        -> OCCUPATIONAL_DOCTOR    (doctor)
--   controller@hrcore.demo    -> HR_CONTROLLER           (controller)

-- ============================================================
-- 1. Remove role assignments for suppressed personas
-- ============================================================
DELETE FROM roles_core.user_role_assignment
WHERE tenant_id = '00000000-0000-0000-0000-0000000a0001'
  AND user_id IN (
    '00000000-0000-0000-0000-0000000c0010',  -- manager
    '00000000-0000-0000-0000-0000000c0011',  -- drh
    '00000000-0000-0000-0000-0000000c0012',  -- recruiter
    '00000000-0000-0000-0000-0000000c0013'   -- accountant
  );

-- ============================================================
-- 2. Remove user accounts for suppressed personas
-- ============================================================
DELETE FROM auth_core.user_account
WHERE tenant_id = '00000000-0000-0000-0000-0000000a0001'
  AND username IN ('manager', 'drh', 'recruiter', 'accountant');

-- ============================================================
-- 3. Remove actors for suppressed personas (login-only actors,
--    the hrm_employee rows survive because they reference the actor)
--    NOTE: we keep the actors because hrm_employee.actor_id references
--    them. We only removed their login + role assignment.
-- ============================================================
-- (actors kept intentionally — hrm_employee depends on them)

-- ============================================================
-- 4. Add controller persona: actor + user account + role assignment
-- ============================================================
INSERT INTO actor.actor (
    id, tenant_id, created_at, updated_at,
    first_name, last_name, email, phone_number, gender, nationality
) VALUES (
    '00000000-0000-0000-0000-0000000b0016', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    'Alain', 'Nkoulou', 'controller@hrcore.demo', '+237699000016', 'MALE', 'CMR'
)
ON CONFLICT (tenant_id, email) DO NOTHING;

INSERT INTO auth_core.user_account (
    id, tenant_id, created_at, updated_at, actor_id, username, email,
    auth_provider, status, password_hash, force_password_change
) VALUES (
    '00000000-0000-0000-0000-0000000c0016', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000b0016', 'controller', 'controller@hrcore.demo',
    'LOCAL', 'ACTIVE',
    '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false
)
ON CONFLICT (tenant_id, username) DO NOTHING;

INSERT INTO roles_core.user_role_assignment (
    id, tenant_id, created_at, updated_at,
    user_id, role_id, scope, scope_type, scope_id
) VALUES (
    '00000000-0000-0000-0000-0000000e0016', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000c0016', '00000000-0000-0000-0000-0000000d0008',
    'ORGANIZATION:00000000-0000-0000-0000-0000000a0002', 'ORGANIZATION',
    '00000000-0000-0000-0000-0000000a0002'
)
ON CONFLICT (tenant_id, user_id, role_id, scope) DO NOTHING;

-- ============================================================
-- 5. Add controller as hrm_employee so dashboards and "mine" pages work
-- ============================================================
INSERT INTO hrm_employee (
    id, tenant_id, created_at, updated_at, organization_id, agency_id,
    actor_id, matricule, categorie, echelon, date_embauche, status,
    department_code, mode_paiement, num_mobile_money, operateur_mm, actor_display_name
) VALUES (
    '00000000-0000-0000-0000-0000000e0116', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
    '00000000-0000-0000-0000-0000000b0016', 'EMP-DEMO-016',
    8, 'A', DATE '2021-03-01', 'ACTIVE',
    'FIN', 'BANK_TRANSFER', NULL, NULL, 'Alain Nkoulou'
)
ON CONFLICT (tenant_id, matricule) DO NOTHING;

INSERT INTO hrm_contract (
    id, tenant_id, created_at, updated_at, organization_id, agency_id,
    employee_id, type, date_debut, date_fin, salaire_base, avantages_nature,
    periode_essai, status
) VALUES (
    '00000000-0000-0000-0000-0000000f0116', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
    '00000000-0000-0000-0000-0000000e0116', 'CDI', DATE '2021-03-01', NULL, 1100000, 100000, 90, 'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;
