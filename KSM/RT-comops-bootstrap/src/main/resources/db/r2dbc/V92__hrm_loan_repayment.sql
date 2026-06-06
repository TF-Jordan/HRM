-- V092: real loan repayment history.
--
-- Each row is an amount actually withheld against a loan during a payroll run (recorded by the
-- payroll engine when LoanAdvance.deduire(...) is applied). This complements the loan's current
-- solde_restant with an auditable timeline of deductions (date, period, run, resulting balance),
-- enabling a real repayment schedule instead of a purely projected one.

CREATE TABLE IF NOT EXISTS hrm_loan_repayment (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    loan_id UUID NOT NULL REFERENCES hrm_loan_advance(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL,
    run_id UUID,
    period VARCHAR(7),
    payroll_entry_id UUID,
    montant NUMERIC(15,2) NOT NULL,
    solde_apres NUMERIC(15,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_loan_repayment_tenant_loan
    ON hrm_loan_repayment (tenant_id, loan_id, created_at);

CREATE INDEX IF NOT EXISTS idx_hrm_loan_repayment_tenant_employee
    ON hrm_loan_repayment (tenant_id, employee_id, created_at);
