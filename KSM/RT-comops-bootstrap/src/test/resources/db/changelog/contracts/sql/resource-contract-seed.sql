INSERT INTO actor.actor (id, tenant_id, created_at, updated_at, first_name, last_name, email)
VALUES ('23000000-0000-0000-0000-000000000010', '23000000-0000-0000-0000-000000000001', now(), now(), 'Resource', 'Contract', 'resource.contract@example.com');

INSERT INTO auth_core.user_account (id, tenant_id, created_at, updated_at, actor_id, username, email, auth_provider, status, password_hash)
VALUES ('23000000-0000-0000-0000-000000000011', '23000000-0000-0000-0000-000000000001', now(), now(), '23000000-0000-0000-0000-000000000010', 'resource-contract', 'resource.contract@example.com', 'LOCAL', 'ACTIVE', '{noop}contract-password');

INSERT INTO roles_core.role (id, tenant_id, created_at, updated_at, code, name, permissions)
VALUES ('23000000-0000-0000-0000-000000000012', '23000000-0000-0000-0000-000000000001', now(), now(), 'ROLE-RESOURCE-CONTRACT', 'Resource Contract Role', ARRAY['resources:write']);

INSERT INTO roles_core.user_role_assignment (id, tenant_id, created_at, updated_at, user_id, role_id, scope)
VALUES ('23000000-0000-0000-0000-000000000013', '23000000-0000-0000-0000-000000000001', now(), now(), '23000000-0000-0000-0000-000000000011', '23000000-0000-0000-0000-000000000012', 'TENANT');

INSERT INTO organization.organization (id, tenant_id, created_at, updated_at, business_actor_id, code, legal_name, display_name, organization_type)
VALUES ('23000000-0000-0000-0000-000000000110', '23000000-0000-0000-0000-000000000001', now(), now(), '23000000-0000-0000-0000-000000000010', 'ORG-RESOURCE-CONTRACT', 'Resource Contract Org', 'Resource Contract Org', 'PRIVATE_COMPANY');

INSERT INTO organization.organization_service_subscription (id, tenant_id, created_at, updated_at, organization_id, service_code)
VALUES ('23000000-0000-0000-0000-000000000113', '23000000-0000-0000-0000-000000000001', now(), now(), '23000000-0000-0000-0000-000000000110', 'RESOURCE');

INSERT INTO organization.agency (id, tenant_id, created_at, updated_at, organization_id, code, name, agency_type, active)
VALUES ('23000000-0000-0000-0000-000000000111', '23000000-0000-0000-0000-000000000001', now(), now(), '23000000-0000-0000-0000-000000000110', 'AGY-RESOURCE-CONTRACT', 'Resource Contract Agency', 'BRANCH', true);

INSERT INTO resource.material_resource (id, tenant_id, created_at, updated_at, organization_id, agency_id, resource_code, name, category, serial_number, status, latitude, longitude, ip_address, mac_address)
VALUES ('23000000-0000-0000-0000-000000000112', '23000000-0000-0000-0000-000000000001', now(), now(), '23000000-0000-0000-0000-000000000110', '23000000-0000-0000-0000-000000000111', 'RES-CONTRACT-0001', 'Resource Contract Router', 'NETWORK', 'SERIAL-CONTRACT-0001', 'AVAILABLE', 4.0500, 9.7000, '10.20.30.40', 'AA:BB:CC:DD:EE:FF');
