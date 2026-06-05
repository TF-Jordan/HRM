-- HRM Employee: persist the actual departure date and reason.
-- Employee.terminate(date, reason) was previously dropping both arguments,
-- making it impossible to prorate the final month's salary or build a STC from the real exit date.

ALTER TABLE hrm_employee
    ADD COLUMN IF NOT EXISTS date_sortie  DATE,
    ADD COLUMN IF NOT EXISTS motif_sortie VARCHAR(200);
