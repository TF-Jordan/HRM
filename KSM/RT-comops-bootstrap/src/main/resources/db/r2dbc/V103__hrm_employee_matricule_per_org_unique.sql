-- Scope the employee-matricule uniqueness per organisation.
--
-- V55 created `idx_hrm_employee_tenant_matricule` UNIQUE (tenant_id, matricule),
-- i.e. tenant-wide. But matricules are allocated from a per-organisation document
-- sequence (HRM_MATRICULE), each starting at 1. A tenant hosting several orgs
-- therefore produces the same code (e.g. EMP000001) in two orgs and the second
-- INSERT fails with a duplicate-key violation.
--
-- A matricule only needs to be unique within its organisation, so move the
-- uniqueness to (tenant_id, organization_id, matricule). The new constraint is
-- strictly more permissive than the old one, so existing rows cannot violate it.
DROP INDEX IF EXISTS idx_hrm_employee_tenant_matricule;

CREATE UNIQUE INDEX IF NOT EXISTS idx_hrm_employee_tenant_org_matricule
    ON hrm_employee (tenant_id, organization_id, matricule);
