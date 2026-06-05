-- V090: Payroll calculation inputs for the demo organization (MUFID Union).
--
-- The autonomous payroll engine (RT-comops-payroll-core) reads its inputs through
-- HrmEmployeeDataAdapter. The base demo seed (V076) already provides 11 active
-- employees with active CDI/CDD contracts (base salary), and V081 seeds the
-- Cameroon pay-element catalogue (CNPS, IRPP, CAC, CFC, RAV, TDL + employer charges).
--
-- This migration completes EVERYTHING else the engine and the legal documents read,
-- so that POST /api/v1/payroll/runs produces correct, declaration-ready results:
--   1. Employer legal payroll identifiers on the organization (CNPS no, country, AT rate)
--   2. Employee CNPS numbers + bank account references (payment orders / DIPE / CNPS)
--   3. Personal info (marital status) + dependent children (IRPP family quotient)
--   4. Annual leave balances (leave encashment views, final settlements)
--   5. One active loan advance in repayment (monthly deduction path)
--   6. Monthly pay variables (overtime + bonuses) for recent periods
--
-- Idempotent: guarded UPDATEs + ON CONFLICT DO NOTHING. Sentinel UUIDs reserved for
-- demo fixtures only (prefix 00000000-0000-0000-0000-0000000aXXXX).

-- =========================================================================
-- 1. Employer legal payroll identifiers on the demo organization
--    (printed on legal payslips / used by declaration headers)
-- =========================================================================
UPDATE organization.organization
SET country_code         = 'CM',
    cnps_employer_number = 'J0123456-A',
    at_risk_rate         = 1.75,
    updated_at           = now()
WHERE id = '00000000-0000-0000-0000-0000000a0002';

-- =========================================================================
-- 2. Employee CNPS numbers + bank account references
--    num_cnps        -> CNPS / DIPE social declarations
--    compte_bancaire -> payment-order accountRef for BANK_TRANSFER employees
--    (MTN / ORANGE employees already carry num_mobile_money from V076)
-- =========================================================================
UPDATE hrm_employee SET num_cnps = '01100000110', compte_bancaire = '10005-00001-0110-72', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0110';
UPDATE hrm_employee SET num_cnps = '01100000111', compte_bancaire = '10005-00001-0111-19', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0111';
UPDATE hrm_employee SET num_cnps = '01100000112', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0112';
UPDATE hrm_employee SET num_cnps = '01100000113', compte_bancaire = '10005-00001-0113-44', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0113';
UPDATE hrm_employee SET num_cnps = '01100000114', compte_bancaire = '10005-00001-0114-08', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0114';
UPDATE hrm_employee SET num_cnps = '01100000115', compte_bancaire = '10005-00001-0115-31', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0115';
UPDATE hrm_employee SET num_cnps = '01100000120', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0120';
UPDATE hrm_employee SET num_cnps = '01100000121', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0121';
UPDATE hrm_employee SET num_cnps = '01100000122', compte_bancaire = '10005-00001-0122-55', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0122';
UPDATE hrm_employee SET num_cnps = '01100000123', compte_bancaire = '10005-00001-0123-67', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0123';
UPDATE hrm_employee SET num_cnps = '01100000124', updated_at = now() WHERE id = '00000000-0000-0000-0000-0000000e0124';

-- =========================================================================
-- 3. Personal info (marital status) — drives the IRPP family quotient.
--    Mixed statuses so single / married / divorced paths are all exercised.
-- =========================================================================
INSERT INTO hrm_employee_personal_info (
    id, tenant_id, created_at, updated_at, employee_id,
    situation_matrimoniale, niu_fiscal, ville, region
) VALUES
    ('00000000-0000-0000-0000-0000000a1110', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0110', 'MARRIED',  'P011000110A', 'Yaoundé', 'Centre'),
    ('00000000-0000-0000-0000-0000000a1111', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0111', 'MARRIED',  'P011000111B', 'Yaoundé', 'Centre'),
    ('00000000-0000-0000-0000-0000000a1112', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0112', 'SINGLE',   'P011000112C', 'Douala',  'Littoral'),
    ('00000000-0000-0000-0000-0000000a1113', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0113', 'MARRIED',  'P011000113D', 'Yaoundé', 'Centre'),
    ('00000000-0000-0000-0000-0000000a1114', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0114', 'MARRIED',  'P011000114E', 'Yaoundé', 'Centre'),
    ('00000000-0000-0000-0000-0000000a1115', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0115', 'SINGLE',   'P011000115F', 'Yaoundé', 'Centre'),
    ('00000000-0000-0000-0000-0000000a1120', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0120', 'MARRIED',  'P011000120G', 'Douala',  'Littoral'),
    ('00000000-0000-0000-0000-0000000a1121', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0121', 'SINGLE',   'P011000121H', 'Douala',  'Littoral'),
    ('00000000-0000-0000-0000-0000000a1122', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0122', 'SINGLE',   'P011000122I', 'Yaoundé', 'Centre'),
    ('00000000-0000-0000-0000-0000000a1123', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0123', 'DIVORCED', 'P011000123J', 'Yaoundé', 'Centre'),
    ('00000000-0000-0000-0000-0000000a1124', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000e0124', 'MARRIED',  'P011000124K', 'Bafoussam', 'Ouest')
ON CONFLICT (employee_id) DO NOTHING;

-- =========================================================================
-- 4. Dependent children (under 21) for married employees — family quotient.
-- =========================================================================
INSERT INTO hrm_dependent (
    id, tenant_id, created_at, updated_at, organization_id, employee_id,
    nom, prenom, date_naissance, lien_parente
) VALUES
    -- Bernard Foga (0110) — 2 children
    ('00000000-0000-0000-0000-0000000a1210', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0110',
     'Foga', 'Emma', DATE '2015-04-12', 'ENFANT'),
    ('00000000-0000-0000-0000-0000000a1211', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0110',
     'Foga', 'Liam', DATE '2018-09-03', 'ENFANT'),
    -- Faïsal Mbarga (0111) — 3 children
    ('00000000-0000-0000-0000-0000000a1212', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0111',
     'Mbarga', 'Sarah', DATE '2012-01-20', 'ENFANT'),
    ('00000000-0000-0000-0000-0000000a1213', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0111',
     'Mbarga', 'Noah', DATE '2014-06-15', 'ENFANT'),
    ('00000000-0000-0000-0000-0000000a1214', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0111',
     'Mbarga', 'Léna', DATE '2019-11-30', 'ENFANT'),
    -- Pierre Etoa (0113) — 1 child
    ('00000000-0000-0000-0000-0000000a1215', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0113',
     'Etoa', 'Maya', DATE '2017-03-08', 'ENFANT'),
    -- Sylvie Nguemo (0114) — 2 children
    ('00000000-0000-0000-0000-0000000a1216', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0114',
     'Nguemo', 'Ethan', DATE '2013-07-22', 'ENFANT'),
    ('00000000-0000-0000-0000-0000000a1217', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0114',
     'Nguemo', 'Chloé', DATE '2016-12-05', 'ENFANT'),
    -- Aminata Diallo (0120) — 1 child
    ('00000000-0000-0000-0000-0000000a1218', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0120',
     'Diallo', 'Ibrahim', DATE '2020-02-18', 'ENFANT'),
    -- Yannick Atangana (0124) — 2 children
    ('00000000-0000-0000-0000-0000000a1219', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0124',
     'Atangana', 'Grace', DATE '2011-05-09', 'ENFANT'),
    ('00000000-0000-0000-0000-0000000a121a', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0124',
     'Atangana', 'David', DATE '2015-10-27', 'ENFANT')
ON CONFLICT (id) DO NOTHING;

-- =========================================================================
-- 5. Annual leave balances (current year) — leave encashment / final settlement.
-- =========================================================================
INSERT INTO hrm_leave_balance (
    id, tenant_id, created_at, updated_at, organization_id, employee_id,
    type, acquis, pris, annee
) VALUES
    ('00000000-0000-0000-0000-0000000a1310', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0110', 'ANNUAL', 18.00, 4.00, 2026),
    ('00000000-0000-0000-0000-0000000a1311', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0111', 'ANNUAL', 24.00, 8.00, 2026),
    ('00000000-0000-0000-0000-0000000a1312', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0112', 'ANNUAL', 15.00, 2.00, 2026),
    ('00000000-0000-0000-0000-0000000a1313', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0113', 'ANNUAL', 22.00, 6.00, 2026),
    ('00000000-0000-0000-0000-0000000a1314', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0114', 'ANNUAL', 24.00, 10.00, 2026),
    ('00000000-0000-0000-0000-0000000a1315', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0115', 'ANNUAL', 18.00, 3.00, 2026),
    ('00000000-0000-0000-0000-0000000a1320', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0120', 'ANNUAL', 20.00, 5.00, 2026),
    ('00000000-0000-0000-0000-0000000a1321', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0121', 'ANNUAL', 12.00, 1.00, 2026),
    ('00000000-0000-0000-0000-0000000a1322', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0122', 'ANNUAL', 14.00, 2.00, 2026),
    ('00000000-0000-0000-0000-0000000a1323', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0123', 'ANNUAL', 18.00, 7.00, 2026),
    ('00000000-0000-0000-0000-0000000a1324', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0124', 'ANNUAL', 16.00, 3.00, 2026)
ON CONFLICT (tenant_id, employee_id, type, annee) DO NOTHING;

-- =========================================================================
-- 6. One active loan advance in repayment — exercises the monthly loan
--    deduction subtracted from net pay (Pierre Etoa, 0113).
-- =========================================================================
INSERT INTO hrm_loan_advance (
    id, tenant_id, created_at, updated_at, organization_id, agency_id, employee_id,
    montant, solde_restant, mensualite, status, date_debut, nb_echeances, motif, approved_by
) VALUES
    ('00000000-0000-0000-0000-0000000a1413', '00000000-0000-0000-0000-0000000a0001', now(), now(),
     '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000a0003',
     '00000000-0000-0000-0000-0000000e0113',
     600000, 400000, 100000, 'IN_REPAYMENT', DATE '2026-02-01', 6,
     'Avance équipement informatique', '00000000-0000-0000-0000-0000000c0002')
ON CONFLICT (id) DO NOTHING;

-- =========================================================================
-- 7. Monthly pay variables (overtime + bonuses) for recent periods, so the
--    overtime calculator and bonus path are exercised when those periods run.
--    Employees without a row simply have zero variables (engine default).
-- =========================================================================
INSERT INTO payroll_pay_variable (
    id, tenant_id, created_at, updated_at, organization_id, employee_id, periode,
    overtime_hours_day, overtime_hours_night, overtime_hours_sunday_holiday,
    bonuses, unpaid_absence_days, advances, worked_days_override, locked
) VALUES
    -- 2026-05
    ('00000000-0000-0000-0000-0000000a1450', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0110', '2026-05', 10.00, 0.00, 0.00, 50000, 0.00, 0, NULL, false),
    ('00000000-0000-0000-0000-0000000a1451', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0120', '2026-05', 6.00, 4.00, 0.00, 0, 1.00, 0, NULL, false),
    ('00000000-0000-0000-0000-0000000a1452', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0123', '2026-05', 0.00, 0.00, 8.00, 75000, 0.00, 0, NULL, false),
    -- 2026-06
    ('00000000-0000-0000-0000-0000000a1460', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0110', '2026-06', 8.00, 2.00, 0.00, 60000, 0.00, 0, NULL, false),
    ('00000000-0000-0000-0000-0000000a1461', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0112', '2026-06', 12.00, 0.00, 0.00, 0, 0.00, 0, NULL, false),
    ('00000000-0000-0000-0000-0000000a1462', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0120', '2026-06', 0.00, 0.00, 6.00, 100000, 0.00, 0, NULL, false),
    ('00000000-0000-0000-0000-0000000a1463', '00000000-0000-0000-0000-0000000a0001', now(), now(), '00000000-0000-0000-0000-0000000a0002', '00000000-0000-0000-0000-0000000e0124', '2026-06', 4.00, 4.00, 0.00, 40000, 2.00, 0, NULL, false)
ON CONFLICT (tenant_id, employee_id, periode) DO NOTHING;
