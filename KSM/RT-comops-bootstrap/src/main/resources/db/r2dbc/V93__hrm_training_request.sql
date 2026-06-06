-- HRM Core - Self-service training requests (employee → manager/DRH approval).

CREATE TABLE IF NOT EXISTS hrm_training_request (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    training_id UUID NOT NULL REFERENCES hrm_training(id) ON DELETE CASCADE,
    motivation TEXT,
    status VARCHAR(20) NOT NULL,
    decision_reason TEXT,
    enrollment_id UUID,
    decided_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_hrm_training_request_employee
    ON hrm_training_request (tenant_id, employee_id);

CREATE INDEX IF NOT EXISTS idx_hrm_training_request_org_status
    ON hrm_training_request (tenant_id, organization_id, status);

-- Grant 'hrm:training:request' so EMPLOYEE roles (and admins/managers) can
-- submit a training request for approval.
UPDATE roles_core.role
   SET permissions = array_append(permissions, 'hrm:training:request'),
       updated_at  = now()
 WHERE 'hrm:training:request' <> ALL(permissions)
   AND code IN ('SUPER_ADMIN', 'TENANT_ADMIN', 'HR_ADMIN', 'MANAGER', 'EMPLOYEE');
