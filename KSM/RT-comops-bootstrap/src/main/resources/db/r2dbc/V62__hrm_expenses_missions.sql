-- HRM Core - Step 8: Expenses & Missions tables

CREATE TABLE IF NOT EXISTS hrm_mission_order (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    destination VARCHAR(255) NOT NULL,
    objet VARCHAR(500) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    montant_avance NUMERIC(19,4),
    centre_cout VARCHAR(255),
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_mission_order_tenant_emp
    ON hrm_mission_order (tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS hrm_expense_report (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    periode VARCHAR(20) NOT NULL,
    total_montant NUMERIC(19,4),
    motif VARCHAR(500),
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_expense_report_tenant_emp
    ON hrm_expense_report (tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS hrm_expense_line (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    expense_report_id UUID NOT NULL REFERENCES hrm_expense_report(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    montant NUMERIC(19,4) NOT NULL,
    categorie VARCHAR(100),
    justificatif_file_id UUID
);

CREATE INDEX IF NOT EXISTS idx_hrm_expense_line_tenant_report
    ON hrm_expense_line (tenant_id, expense_report_id);
