-- HRM Mission Orders - grant 'hrm:mission:accept' permission so EMPLOYEE roles
-- (and admins) can self-accept or decline a mission order issued to them.

UPDATE roles_core.role
   SET permissions = array_append(permissions, 'hrm:mission:accept'),
       updated_at  = now()
 WHERE 'hrm:mission:accept' <> ALL(permissions)
   AND code IN ('SUPER_ADMIN', 'TENANT_ADMIN', 'HR_ADMIN', 'MANAGER', 'EMPLOYEE');
