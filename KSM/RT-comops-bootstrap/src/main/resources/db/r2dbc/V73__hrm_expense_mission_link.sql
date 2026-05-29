-- HRM Expense reports - optional link to the mission order being expensed,
-- plus an index for the accountant / DAF org-wide validation queue.

ALTER TABLE hrm_expense_report
    ADD COLUMN IF NOT EXISTS mission_order_id UUID REFERENCES hrm_mission_order(id);

CREATE INDEX IF NOT EXISTS idx_hrm_expense_report_tenant_status
    ON hrm_expense_report (tenant_id, status);

CREATE INDEX IF NOT EXISTS idx_hrm_expense_report_mission
    ON hrm_expense_report (mission_order_id);
