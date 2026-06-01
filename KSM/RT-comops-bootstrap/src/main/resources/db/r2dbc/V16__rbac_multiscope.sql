ALTER TABLE roles_core.role
    ADD COLUMN IF NOT EXISTS scope_type VARCHAR(32) NOT NULL DEFAULT 'TENANT';

ALTER TABLE roles_core.user_role_assignment
    ADD COLUMN IF NOT EXISTS scope_type VARCHAR(32) NOT NULL DEFAULT 'TENANT',
    ADD COLUMN IF NOT EXISTS scope_id UUID;

UPDATE roles_core.user_role_assignment
SET scope_type = CASE
        WHEN scope ILIKE 'SYSTEM' THEN 'SYSTEM'
        WHEN scope ILIKE 'GLOBAL' OR scope ILIKE 'TENANT' THEN 'TENANT'
        WHEN scope ILIKE 'ORGANIZATION:%' THEN 'ORGANIZATION'
        WHEN scope ILIKE 'AGENCY:%' THEN 'AGENCY'
        ELSE 'TENANT'
    END,
    scope_id = CASE
        WHEN scope ILIKE 'ORGANIZATION:%' THEN split_part(scope, ':', 2)::uuid
        WHEN scope ILIKE 'AGENCY:%' THEN split_part(scope, ':', 2)::uuid
        ELSE NULL
    END
WHERE scope_type IS NULL
   OR scope_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_role_scope_type
    ON roles_core.role (tenant_id, scope_type);

CREATE INDEX IF NOT EXISTS idx_user_role_assignment_scope
    ON roles_core.user_role_assignment (tenant_id, user_id, scope_type, scope_id);
