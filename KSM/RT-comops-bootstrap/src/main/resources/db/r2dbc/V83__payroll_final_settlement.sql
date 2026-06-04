-- Payroll Core — final settlements (soldes de tout compte).

CREATE TABLE IF NOT EXISTS payroll_final_settlement (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    periode VARCHAR(7) NOT NULL,
    departure_date DATE NOT NULL,
    reason VARCHAR(30) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    seniority_years INT NOT NULL DEFAULT 0,
    prorated_salary NUMERIC(15,2) NOT NULL DEFAULT 0,
    leave_compensation NUMERIC(15,2) NOT NULL DEFAULT 0,
    notice_indemnity NUMERIC(15,2) NOT NULL DEFAULT 0,
    severance_indemnity NUMERIC(15,2) NOT NULL DEFAULT 0,
    gratification NUMERIC(15,2) NOT NULL DEFAULT 0,
    gross_settlement NUMERIC(15,2) NOT NULL DEFAULT 0,
    loan_deducted NUMERIC(15,2) NOT NULL DEFAULT 0,
    net_settlement NUMERIC(15,2) NOT NULL DEFAULT 0,
    status VARCHAR(15) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_payroll_final_settlement_employee
    ON payroll_final_settlement (tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS idx_payroll_final_settlement_org
    ON payroll_final_settlement (tenant_id, organization_id);
