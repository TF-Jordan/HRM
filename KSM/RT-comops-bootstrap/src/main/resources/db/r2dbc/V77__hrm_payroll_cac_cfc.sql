-- HRM Payroll: add CFC column to hrm_payroll_entry and CAC/CFC aggregates to hrm_payroll_run

ALTER TABLE hrm_payroll_entry
    ADD COLUMN IF NOT EXISTS cfc NUMERIC(15,2) NOT NULL DEFAULT 0;

ALTER TABLE hrm_payroll_run
    ADD COLUMN IF NOT EXISTS total_cac NUMERIC(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_cfc NUMERIC(15,2) NOT NULL DEFAULT 0;
