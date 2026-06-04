-- Payroll Core — wage-garnishment orders (saisies sur salaire).

CREATE TABLE IF NOT EXISTS payroll_garnishment_order (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    beneficiary VARCHAR(160) NOT NULL,
    reference VARCHAR(60),
    total_amount NUMERIC(15,2) NOT NULL,
    remaining_balance NUMERIC(15,2) NOT NULL,
    monthly_amount NUMERIC(15,2) NOT NULL,
    status VARCHAR(15) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_payroll_garnishment_employee
    ON payroll_garnishment_order (tenant_id, employee_id, status);
CREATE INDEX IF NOT EXISTS idx_payroll_garnishment_org
    ON payroll_garnishment_order (tenant_id, organization_id);
