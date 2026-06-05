-- HRM Contract: store the employee's job title / fonction, used on the legal payslip
-- header and on the work-certificate template ("a été employé(e) en qualité de …").

ALTER TABLE hrm_contract
    ADD COLUMN IF NOT EXISTS position VARCHAR(160);
