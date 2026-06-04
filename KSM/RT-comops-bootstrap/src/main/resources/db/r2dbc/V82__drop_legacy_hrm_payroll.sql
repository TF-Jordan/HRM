-- Phase 10 cleanup: the payroll domain has moved to the autonomous payroll-core module
-- (payroll_* tables). The legacy hrm_payroll_* tables are now unused — drop them.
-- Safe: payroll runs were generated at runtime (no seeded data) and nothing references these tables.

DROP TABLE IF EXISTS hrm_payslip_line;
DROP TABLE IF EXISTS hrm_payroll_entry;
DROP TABLE IF EXISTS hrm_payroll_run;
