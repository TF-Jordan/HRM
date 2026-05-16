INSERT INTO actor.actor (id, tenant_id, created_at, updated_at, first_name, last_name, email)
VALUES ('21000000-0000-0000-0000-000000000010', '21000000-0000-0000-0000-000000000001', now(), now(), 'Sales', 'Contract', 'sales.contract@example.com');

INSERT INTO auth_core.user_account (id, tenant_id, created_at, updated_at, actor_id, username, email, auth_provider, status, password_hash)
VALUES ('21000000-0000-0000-0000-000000000011', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000010', 'sales-contract', 'sales.contract@example.com', 'LOCAL', 'ACTIVE', '{noop}contract-password');

INSERT INTO roles_core.role (id, tenant_id, created_at, updated_at, code, name, permissions)
VALUES ('21000000-0000-0000-0000-000000000012', '21000000-0000-0000-0000-000000000001', now(), now(), 'ROLE-SALES-CONTRACT', 'Sales Contract Role', ARRAY['sales:write','inventory:write']);

INSERT INTO roles_core.user_role_assignment (id, tenant_id, created_at, updated_at, user_id, role_id, scope)
VALUES ('21000000-0000-0000-0000-000000000013', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000011', '21000000-0000-0000-0000-000000000012', 'TENANT');

INSERT INTO organization.organization (id, tenant_id, created_at, updated_at, business_actor_id, code, legal_name, display_name, organization_type)
VALUES ('21000000-0000-0000-0000-000000000110', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000010', 'ORG-SALES-CONTRACT', 'Sales Contract Org', 'Sales Contract Org', 'PRIVATE_COMPANY');

INSERT INTO organization.organization_service_subscription (id, tenant_id, created_at, updated_at, organization_id, service_code)
VALUES
('21000000-0000-0000-0000-000000000117', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', 'COMMERCIAL'),
('21000000-0000-0000-0000-000000000118', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', 'PRODUCT'),
('21000000-0000-0000-0000-000000000119', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', 'INVENTORY'),
('21000000-0000-0000-0000-000000000120', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', 'SALES');

INSERT INTO organization.agency (id, tenant_id, created_at, updated_at, organization_id, code, name, agency_type, active)
VALUES ('21000000-0000-0000-0000-000000000111', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', 'AGY-SALES-CONTRACT', 'Sales Contract Agency', 'BRANCH', true);

INSERT INTO tp.third_party (id, tenant_id, created_at, updated_at, organization_id, party_type, party_id, reference_code, display_name, roles, prospect)
VALUES ('21000000-0000-0000-0000-000000000112', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', 'ACTOR', '21000000-0000-0000-0000-000000000010', 'TP-SALES-CONTRACT', 'Sales Contract Customer', ARRAY['CLIENT'], false);

INSERT INTO product.product (id, tenant_id, created_at, updated_at, organization_id, sku, name, family_code, variant_label, unit_price, currency, status)
VALUES ('21000000-0000-0000-0000-000000000113', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', 'SKU-SALES-CONTRACT', 'Sales Contract Product', 'FAMILY-CONTRACT', 'STANDARD', 15.00, 'EUR', 'ACTIVE');

INSERT INTO settings.document_sequence (id, tenant_id, created_at, updated_at, organization_id, agency_id, document_type, prefix, suffix, padding_width, next_number)
VALUES
('21000000-0000-0000-0000-000000000114', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', NULL, 'SALES_ORDER', 'SO-C-', NULL, 4, 1),
('21000000-0000-0000-0000-000000000115', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', NULL, 'STOCK_MOVEMENT', 'MOV-C-', NULL, 4, 2);

INSERT INTO inventory.stock_movement (id, tenant_id, created_at, updated_at, organization_id, agency_id, product_id, third_party_id, reference_number, movement_type, quantity, source_document_type, source_document_number)
VALUES ('21000000-0000-0000-0000-000000000116', '21000000-0000-0000-0000-000000000001', now(), now(), '21000000-0000-0000-0000-000000000110', '21000000-0000-0000-0000-000000000111', '21000000-0000-0000-0000-000000000113', '21000000-0000-0000-0000-000000000112', 'MOV-C-0001', 'INBOUND', 8.000, NULL, NULL);
