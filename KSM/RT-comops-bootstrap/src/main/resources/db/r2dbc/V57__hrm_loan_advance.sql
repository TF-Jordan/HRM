-- HRM Core - Step 3: Loan advance table

CREATE TABLE IF NOT EXISTS hrm_loan_advance (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    montant NUMERIC(15,2) NOT NULL,
    solde_restant NUMERIC(15,2) NOT NULL,
    mensualite NUMERIC(15,2) NOT NULL,
    status VARCHAR(15) NOT NULL,
    date_debut DATE NOT NULL,
    nb_echeances INT NOT NULL,
    motif TEXT,
    approved_by UUID
);

CREATE INDEX IF NOT EXISTS idx_hrm_loan_advance_tenant_employee
    ON hrm_loan_advance (tenant_id, employee_id);

CREATE INDEX IF NOT EXISTS idx_hrm_loan_advance_tenant_employee_status
    ON hrm_loan_advance (tenant_id, employee_id, status);
