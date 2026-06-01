-- HRM Mission Orders - Employee acceptance workflow.
-- Adds PENDING_ACCEPTANCE / DECLINED transitions, parent linkage for avenants,
-- and the textual justification captured when an employee declines.

ALTER TABLE hrm_mission_order
    ADD COLUMN IF NOT EXISTS parent_order_id UUID REFERENCES hrm_mission_order(id);

ALTER TABLE hrm_mission_order
    ADD COLUMN IF NOT EXISTS decision_reason VARCHAR(2000);

ALTER TABLE hrm_mission_order
    ADD COLUMN IF NOT EXISTS decided_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_hrm_mission_order_tenant_status
    ON hrm_mission_order (tenant_id, status);

CREATE INDEX IF NOT EXISTS idx_hrm_mission_order_parent
    ON hrm_mission_order (parent_order_id);
