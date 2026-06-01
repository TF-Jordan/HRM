-- HRM Core - Step 4: Timesheet table

CREATE TABLE IF NOT EXISTS hrm_timesheet (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    periode VARCHAR(7) NOT NULL,
    heures_normales NUMERIC(7,2) NOT NULL DEFAULT 0,
    heures_supplementaires NUMERIC(7,2) NOT NULL DEFAULT 0,
    heures_nuit NUMERIC(7,2) NOT NULL DEFAULT 0,
    heures_weekend NUMERIC(7,2) NOT NULL DEFAULT 0,
    absences_non_justifiees NUMERIC(5,2) NOT NULL DEFAULT 0,
    status VARCHAR(15) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_timesheet_tenant_employee_periode
    ON hrm_timesheet (tenant_id, employee_id, periode);

CREATE INDEX IF NOT EXISTS idx_hrm_timesheet_tenant_org_periode
    ON hrm_timesheet (tenant_id, organization_id, periode);
