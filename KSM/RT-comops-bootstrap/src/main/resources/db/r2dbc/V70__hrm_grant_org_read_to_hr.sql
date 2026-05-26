-- HRM: allow HR roles that can read employees to also list organization
-- agencies, so the frontend can resolve agencyId -> site name.
-- The agency listing endpoint is guarded by 'organizations:write', so we
-- grant that permission to every role that already has 'hrm:employee:read'.
UPDATE roles_core.role
SET permissions = array_append(permissions, 'organizations:write'),
    updated_at = now()
WHERE 'hrm:employee:read' = ANY(permissions)
  AND NOT ('organizations:write' = ANY(permissions));
