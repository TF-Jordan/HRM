-- HRM Core - Add job title (poste) to employee
ALTER TABLE hrm_employee ADD COLUMN IF NOT EXISTS poste VARCHAR(120);
