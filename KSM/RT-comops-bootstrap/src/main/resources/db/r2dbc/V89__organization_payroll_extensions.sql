-- Organization payroll extensions, used by payroll-core's legal payslip and document generation:
--   country_code           — selects the applicable pay-element catalogue (CM by default)
--   cnps_employer_number   — printed on the legal payslip employer header
--   at_risk_rate           — sector-specific employer AT (accidents du travail) rate

ALTER TABLE organization.organization
    ADD COLUMN IF NOT EXISTS country_code         VARCHAR(3),
    ADD COLUMN IF NOT EXISTS cnps_employer_number VARCHAR(40),
    ADD COLUMN IF NOT EXISTS at_risk_rate         NUMERIC(8,5);
