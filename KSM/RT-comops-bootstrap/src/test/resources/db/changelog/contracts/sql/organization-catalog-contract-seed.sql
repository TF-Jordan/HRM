INSERT INTO actor.actor (id, tenant_id, created_at, updated_at, first_name, last_name, email)
VALUES
('24020000-0000-0000-0000-000000000010', '24020000-0000-0000-0000-000000000001', now(), now(),
 'Business', 'Catalog', 'business.catalog@example.com'),
('24020000-0000-0000-0000-000000000012', '24020000-0000-0000-0000-000000000001', now(), now(),
 'External', 'Catalog', 'external.catalog@example.com');

INSERT INTO auth_core.user_account (id, tenant_id, created_at, updated_at, actor_id, username, email, auth_provider, status)
VALUES ('24020000-0000-0000-0000-000000000011', '24020000-0000-0000-0000-000000000001', now(), now(),
        '24020000-0000-0000-0000-000000000010', 'catalog-contract', 'business.catalog@example.com', 'LOCAL',
        'ACTIVE');

INSERT INTO roles_core.role (id, tenant_id, created_at, updated_at, code, name, permissions)
VALUES ('24020000-0000-0000-0000-000000000013', '24020000-0000-0000-0000-000000000001', now(), now(),
        'ROLE-CATALOG-CONTRACT', 'Catalog Contract Role',
        ARRAY['organizations:write','third-parties:write','products:write']);

INSERT INTO roles_core.user_role_assignment (id, tenant_id, created_at, updated_at, user_id, role_id, scope)
VALUES ('24020000-0000-0000-0000-000000000014', '24020000-0000-0000-0000-000000000001', now(), now(),
        '24020000-0000-0000-0000-000000000011', '24020000-0000-0000-0000-000000000013', 'TENANT');
