-- Standalone payroll mode: payroll-owned employee records (CSV import / CRUD) and the
-- per-organization data-source switch (HRM vs LOCAL) that tells the run engine where to
-- read employees from.

CREATE TABLE IF NOT EXISTS payroll_employee (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    actor_id UUID,
    matricule VARCHAR(64) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    social_security_no VARCHAR(64),
    categorie INTEGER NOT NULL DEFAULT 1,
    echelon VARCHAR(16),
    department_code VARCHAR(64),
    hire_date DATE NOT NULL,
    departure_date DATE,
    marital_status VARCHAR(16) NOT NULL DEFAULT 'SINGLE',
    dependent_children INTEGER NOT NULL DEFAULT 0,
    base_salary NUMERIC(18, 2) NOT NULL,
    benefits_in_kind NUMERIC(18, 2) NOT NULL DEFAULT 0,
    position VARCHAR(255),
    payment_channel VARCHAR(32) NOT NULL DEFAULT 'CASH',
    account_ref VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_employee_matricule
    ON payroll_employee (tenant_id, organization_id, matricule);
CREATE INDEX IF NOT EXISTS idx_payroll_employee_org
    ON payroll_employee (tenant_id, organization_id);
CREATE INDEX IF NOT EXISTS idx_payroll_employee_actor
    ON payroll_employee (tenant_id, actor_id) WHERE actor_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS payroll_data_source (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    source VARCHAR(8) NOT NULL DEFAULT 'HRM'
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_data_source_org
    ON payroll_data_source (tenant_id, organization_id);
