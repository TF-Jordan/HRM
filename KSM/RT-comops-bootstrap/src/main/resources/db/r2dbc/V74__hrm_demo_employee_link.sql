-- HRM demo seed fix: link the demo `employee` user account (actor b0003,
-- EMPLOYEE @ org a0002 / agency a0003) to a real hrm_employee record so the
-- self-service surfaces (/leaves/my, /expenses/mine, /mission-orders/mine,
-- training enrolment…) resolve an employee for the logged-in actor.
-- Idempotent: only inserts when the link is missing.

INSERT INTO hrm_employee (
    id, tenant_id, created_at, updated_at, organization_id, agency_id,
    actor_id, matricule, categorie, echelon, date_embauche, status,
    department_code, mode_paiement, actor_display_name
)
SELECT
    '00000000-0000-0000-0000-0000000e0003',
    '00000000-0000-0000-0000-0000000a0001',
    now(), now(),
    '00000000-0000-0000-0000-0000000a0002',
    '00000000-0000-0000-0000-0000000a0003',
    '00000000-0000-0000-0000-0000000b0003',
    'EMP-DEMO-003',
    5, 'B', DATE '2023-03-01', 'ACTIVE',
    'IT', 'BANK_TRANSFER', 'Demo Employee'
WHERE NOT EXISTS (
    SELECT 1 FROM hrm_employee
    WHERE actor_id = '00000000-0000-0000-0000-0000000b0003'
);

-- Active CDI contract for the demo employee — populates the 360° hero and
-- contract list.
INSERT INTO hrm_contract (
    id, tenant_id, created_at, updated_at, organization_id, agency_id,
    employee_id, type, date_debut, date_fin, salaire_base, avantages_nature,
    periode_essai, status
)
SELECT
    '00000000-0000-0000-0000-0000000e1003',
    '00000000-0000-0000-0000-0000000a0001',
    now(), now(),
    '00000000-0000-0000-0000-0000000a0002',
    '00000000-0000-0000-0000-0000000a0003',
    '00000000-0000-0000-0000-0000000e0003',
    'CDI', DATE '2023-03-01', NULL, 450000, 50000, 90, 'ACTIVE'
WHERE EXISTS (SELECT 1 FROM hrm_employee WHERE id = '00000000-0000-0000-0000-0000000e0003')
  AND NOT EXISTS (SELECT 1 FROM hrm_contract WHERE id = '00000000-0000-0000-0000-0000000e1003');
