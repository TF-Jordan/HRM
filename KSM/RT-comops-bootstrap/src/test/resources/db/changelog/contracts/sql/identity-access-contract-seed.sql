INSERT INTO actor.actor (id, tenant_id, created_at, updated_at, first_name, last_name, email)
VALUES ('24010000-0000-0000-0000-000000000010', '24010000-0000-0000-0000-000000000001', now(), now(),
        'Bootstrap', 'Identity', 'bootstrap.identity@example.com');

INSERT INTO auth_core.user_account (id, tenant_id, created_at, updated_at, actor_id, username, email, auth_provider, status, password_hash)
VALUES ('24010000-0000-0000-0000-000000000011', '24010000-0000-0000-0000-000000000001', now(), now(),
        '24010000-0000-0000-0000-000000000010', 'bootstrap-identity', 'bootstrap.identity@example.com', 'LOCAL',
        'ACTIVE', '{noop}contract-password');
