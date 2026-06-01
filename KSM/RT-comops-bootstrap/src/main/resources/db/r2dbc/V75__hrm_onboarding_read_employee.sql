-- HRM Onboarding - grant 'hrm:onboarding:read' to the EMPLOYEE role so a
-- new hire can consult their own onboarding checklist (/recruitment/onboarding).
-- KSM still enforces the per-employee scope; this only unlocks the BFF gate.

UPDATE roles_core.role
   SET permissions = array_append(permissions, 'hrm:onboarding:read'),
       updated_at  = now()
 WHERE 'hrm:onboarding:read' <> ALL(permissions)
   AND code = 'EMPLOYEE';
