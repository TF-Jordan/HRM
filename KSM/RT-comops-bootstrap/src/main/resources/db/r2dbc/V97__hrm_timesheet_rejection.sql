-- HRM Timesheet: add rejection support

ALTER TABLE hrm_timesheet ADD COLUMN IF NOT EXISTS rejection_comment TEXT;
