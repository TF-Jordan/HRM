-- HRM Core - Step 5: Payroll tables

CREATE TABLE IF NOT EXISTS hrm_payroll_run (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    periode VARCHAR(7) NOT NULL,
    status VARCHAR(15) NOT NULL,
    total_brut NUMERIC(15,2) NOT NULL,
    total_net NUMERIC(15,2) NOT NULL,
    total_cnps_employe NUMERIC(15,2) NOT NULL,
    total_cnps_employeur NUMERIC(15,2) NOT NULL,
    total_irpp NUMERIC(15,2) NOT NULL,
    nb_employes INT NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE,
    validated_by UUID,
    validated_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_hrm_payroll_run_unique
    ON hrm_payroll_run (tenant_id, organization_id, periode) WHERE agency_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_hrm_payroll_run_unique_agency
    ON hrm_payroll_run (tenant_id, organization_id, agency_id, periode) WHERE agency_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_hrm_payroll_run_tenant_org
    ON hrm_payroll_run (tenant_id, organization_id);

CREATE TABLE IF NOT EXISTS hrm_payroll_entry (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    payroll_run_id UUID NOT NULL REFERENCES hrm_payroll_run(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    salaire_base NUMERIC(15,2) NOT NULL,
    brut NUMERIC(15,2) NOT NULL,
    net NUMERIC(15,2) NOT NULL,
    cnps_employe NUMERIC(15,2) NOT NULL,
    cnps_employeur NUMERIC(15,2) NOT NULL,
    irpp NUMERIC(15,2) NOT NULL,
    cac NUMERIC(15,2) NOT NULL,
    primes NUMERIC(15,2),
    retenues NUMERIC(15,2),
    avances_deduites NUMERIC(15,2),
    payment_status VARCHAR(15) NOT NULL,
    payment_channel VARCHAR(30) NOT NULL,
    account_ref VARCHAR(30)
);

CREATE INDEX IF NOT EXISTS idx_hrm_payroll_entry_run
    ON hrm_payroll_entry (tenant_id, payroll_run_id);

CREATE INDEX IF NOT EXISTS idx_hrm_payroll_entry_employee
    ON hrm_payroll_entry (tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS hrm_payslip_line (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    payroll_entry_id UUID NOT NULL REFERENCES hrm_payroll_entry(id) ON DELETE CASCADE,
    libelle VARCHAR(100) NOT NULL,
    type VARCHAR(15) NOT NULL,
    base NUMERIC(15,2),
    taux NUMERIC(8,5),
    montant NUMERIC(15,2) NOT NULL,
    ordre_affichage INT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_payslip_line_entry
    ON hrm_payslip_line (tenant_id, payroll_entry_id);
