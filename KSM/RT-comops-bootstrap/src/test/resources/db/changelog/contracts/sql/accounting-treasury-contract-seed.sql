INSERT INTO actor.actor (id, tenant_id, created_at, updated_at, first_name, last_name, email)
VALUES ('22000000-0000-0000-0000-000000000010', '22000000-0000-0000-0000-000000000001', now(), now(), 'Accounting', 'Contract', 'accounting.contract@example.com');

INSERT INTO auth_core.user_account (id, tenant_id, created_at, updated_at, actor_id, username, email, auth_provider, status, password_hash)
VALUES ('22000000-0000-0000-0000-000000000011', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000010', 'accounting-contract', 'accounting.contract@example.com', 'LOCAL', 'ACTIVE', '{noop}contract-password');

INSERT INTO roles_core.role (id, tenant_id, created_at, updated_at, code, name, permissions)
VALUES ('22000000-0000-0000-0000-000000000012', '22000000-0000-0000-0000-000000000001', now(), now(), 'ROLE-ACCOUNTING-CONTRACT', 'Accounting Contract Role', ARRAY['accounting:write','treasury:manage']);

INSERT INTO roles_core.user_role_assignment (id, tenant_id, created_at, updated_at, user_id, role_id, scope)
VALUES ('22000000-0000-0000-0000-000000000013', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000011', '22000000-0000-0000-0000-000000000012', 'TENANT');

INSERT INTO organization.organization (id, tenant_id, created_at, updated_at, business_actor_id, code, legal_name, display_name, organization_type)
VALUES ('22000000-0000-0000-0000-000000000110', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000010', 'ORG-ACCOUNTING-CONTRACT', 'Accounting Contract Org', 'Accounting Contract Org', 'PRIVATE_COMPANY');

INSERT INTO organization.organization_service_subscription (id, tenant_id, created_at, updated_at, organization_id, service_code)
VALUES
('22000000-0000-0000-0000-000000000118', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', 'ACCOUNTING'),
('22000000-0000-0000-0000-000000000119', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', 'TREASURY'),
('22000000-0000-0000-0000-000000000120', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', 'PRODUCT'),
('22000000-0000-0000-0000-000000000121', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', 'COMMERCIAL');

INSERT INTO tp.third_party (id, tenant_id, created_at, updated_at, organization_id, party_type, party_id, reference_code, display_name, roles, prospect)
VALUES ('22000000-0000-0000-0000-000000000112', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', 'ACTOR', '22000000-0000-0000-0000-000000000010', 'TP-ACCOUNTING-CONTRACT', 'Accounting Contract Customer', ARRAY['CLIENT'], false);

INSERT INTO product.product (id, tenant_id, created_at, updated_at, organization_id, sku, name, family_code, variant_label, unit_price, currency, status)
VALUES ('22000000-0000-0000-0000-000000000113', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', 'SKU-ACCOUNTING-CONTRACT', 'Accounting Contract Product', 'FAMILY-CONTRACT', 'STANDARD', 25.00, 'EUR', 'ACTIVE');

INSERT INTO accounting.invoice (id, tenant_id, created_at, updated_at, organization_id, customer_third_party_id, order_id, product_id, invoice_number, quantity, unit_price, total_quantity, subtotal_amount, total_amount, currency, status, payment_status, settled_amount, outstanding_amount, settled_at)
VALUES ('22000000-0000-0000-0000-000000000114', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', '22000000-0000-0000-0000-000000000112', NULL, '22000000-0000-0000-0000-000000000113', 'INV-CONTRACT-0001', 2.000, 25.00, 2.000, 50.00, 50.00, 'EUR', 'DRAFT', 'UNPAID', 0.00, 50.00, NULL);

INSERT INTO accounting.invoice_line (id, invoice_id, tenant_id, created_at, updated_at, product_id, quantity, unit_price, line_amount)
VALUES ('22000000-0000-0000-0000-000000000115', '22000000-0000-0000-0000-000000000114', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000113', 2.000, 25.00, 50.00);

INSERT INTO treasury.bank_account (id, tenant_id, created_at, updated_at, organization_id, bank_third_party_id, bank_name, account_number, iban, currency, status)
VALUES ('22000000-0000-0000-0000-000000000116', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', NULL, 'Accounting Contract Bank', 'ACC-ACCOUNTING-CONTRACT', 'FR7630006000011234567890193', 'EUR', 'ACTIVE');

INSERT INTO settings.document_sequence (id, tenant_id, created_at, updated_at, organization_id, agency_id, document_type, prefix, suffix, padding_width, next_number)
VALUES ('22000000-0000-0000-0000-000000000117', '22000000-0000-0000-0000-000000000001', now(), now(), '22000000-0000-0000-0000-000000000110', NULL, 'INVOICE_SETTLEMENT', 'SET-C-', NULL, 4, 1);
