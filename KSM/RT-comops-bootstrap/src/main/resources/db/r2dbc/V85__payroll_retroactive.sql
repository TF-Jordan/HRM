-- Payroll Core — retroactive pay adjustments (rappels de salaire).

CREATE TABLE IF NOT EXISTS payroll_retroactive_adjustment (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    origin_period VARCHAR(7) NOT NULL,
    target_period VARCHAR(7) NOT NULL,
    reason VARCHAR(160),
    currency VARCHAR(3) NOT NULL,
    old_gross NUMERIC(15,2) NOT NULL DEFAULT 0,
    new_gross NUMERIC(15,2) NOT NULL DEFAULT 0,
    delta_gross NUMERIC(15,2) NOT NULL DEFAULT 0,
    old_net NUMERIC(15,2) NOT NULL DEFAULT 0,
    new_net NUMERIC(15,2) NOT NULL DEFAULT 0,
    delta_net NUMERIC(15,2) NOT NULL DEFAULT 0,
    status VARCHAR(15) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_payroll_retroactive_employee
    ON payroll_retroactive_adjustment (tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS idx_payroll_retroactive_org
    ON payroll_retroactive_adjustment (tenant_id, organization_id);
