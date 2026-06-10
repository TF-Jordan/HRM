-- Payroll cycle rejection: the HR admin can return a CALCULATED/REVIEW cycle to the
-- payroll manager with a mandatory justification, and validation becomes HR-admin only.

-- 1. Rejection trail on the payroll run.
ALTER TABLE payroll_run ADD COLUMN IF NOT EXISTS rejection_reason TEXT;
ALTER TABLE payroll_run ADD COLUMN IF NOT EXISTS rejected_by UUID;
ALTER TABLE payroll_run ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP WITH TIME ZONE;

-- 2. Segregation of duties: only the HR admin validates a payroll cycle. The payroll
--    manager keeps `hrm:payroll:run` (calculate/recalculate) but loses validation rights,
--    so the validate/reject controls no longer surface in the payroll-manager workspace.
UPDATE roles_core.role
   SET permissions = array_remove(permissions, 'hrm:payroll:validate'),
       updated_at  = now()
 WHERE 'hrm:payroll:validate' = ANY(permissions)
   AND code = 'PAYROLL_MANAGER';
