CREATE SCHEMA IF NOT EXISTS actor;
CREATE SCHEMA IF NOT EXISTS organization;
CREATE SCHEMA IF NOT EXISTS tp;
CREATE SCHEMA IF NOT EXISTS auth_core;
CREATE SCHEMA IF NOT EXISTS roles_core;
CREATE SCHEMA IF NOT EXISTS product;
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS resource;
CREATE SCHEMA IF NOT EXISTS settings;
CREATE SCHEMA IF NOT EXISTS sales;
CREATE SCHEMA IF NOT EXISTS accounting;
CREATE SCHEMA IF NOT EXISTS treasury;

CREATE TABLE IF NOT EXISTS actor.actor (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  first_name text NOT NULL,
  last_name text NOT NULL,
  phone_number text,
  email text,
  gender text,
  nationality text,
  birth_date date,
  profession text,
  biography text,
  deleted_at timestamptz,
  UNIQUE (tenant_id, email)
);

CREATE TABLE IF NOT EXISTS organization.organization (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  business_actor_id uuid NOT NULL,
  code text NOT NULL,
  legal_name text NOT NULL,
  display_name text NOT NULL,
  organization_type text NOT NULL,
  UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS organization.agency (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  code text NOT NULL,
  name text NOT NULL,
  agency_type text NOT NULL,
  active boolean NOT NULL DEFAULT true,
  UNIQUE (tenant_id, organization_id, code)
);

CREATE TABLE IF NOT EXISTS organization.opening_hours_rule (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  day_of_week text NOT NULL,
  opens_at time,
  closes_at time,
  closed boolean NOT NULL DEFAULT false,
  UNIQUE (tenant_id, organization_id, agency_id, day_of_week)
);

CREATE TABLE IF NOT EXISTS organization.point_of_interest (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  name text NOT NULL,
  poi_type text NOT NULL,
  latitude double precision,
  longitude double precision,
  UNIQUE (tenant_id, organization_id, agency_id, name)
);

CREATE TABLE IF NOT EXISTS tp.third_party (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  party_type text NOT NULL,
  party_id uuid NOT NULL,
  reference_code text NOT NULL,
  display_name text NOT NULL,
  roles text[] NOT NULL,
  prospect boolean NOT NULL DEFAULT false,
  UNIQUE (tenant_id, organization_id, reference_code)
);

CREATE TABLE IF NOT EXISTS auth_core.user_account (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  actor_id uuid,
  username text NOT NULL,
  email text NOT NULL,
  auth_provider text NOT NULL,
  status text NOT NULL,
  UNIQUE (tenant_id, username)
);

CREATE TABLE IF NOT EXISTS roles_core.role (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  code text NOT NULL,
  name text NOT NULL,
  permissions text[] NOT NULL,
  UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS roles_core.user_role_assignment (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  user_id uuid NOT NULL,
  role_id uuid NOT NULL,
  scope text NOT NULL,
  UNIQUE (tenant_id, user_id, role_id, scope)
);

CREATE TABLE IF NOT EXISTS product.product (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  sku text NOT NULL,
  name text NOT NULL,
  family_code text NOT NULL,
  variant_label text NOT NULL,
  barcode text,
  description text,
  unit_price numeric(19,2) NOT NULL,
  currency text NOT NULL,
  status text NOT NULL DEFAULT 'ACTIVE',
  UNIQUE (tenant_id, organization_id, sku)
);

ALTER TABLE product.product ADD COLUMN IF NOT EXISTS barcode text;
ALTER TABLE product.product ADD COLUMN IF NOT EXISTS description text;
ALTER TABLE product.product ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT 'ACTIVE';

CREATE TABLE IF NOT EXISTS inventory.stock_movement (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  product_id uuid NOT NULL,
  third_party_id uuid,
  reference_number text NOT NULL,
  movement_type text NOT NULL,
  quantity numeric(19,3) NOT NULL,
  UNIQUE (tenant_id, organization_id, reference_number)
);

CREATE TABLE IF NOT EXISTS inventory.product_transformation (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  source_product_id uuid NOT NULL,
  target_product_id uuid NOT NULL,
  reference_number text NOT NULL,
  source_quantity numeric(19,3) NOT NULL,
  target_quantity numeric(19,3) NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory.warehouse_transfer (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  source_agency_id uuid NOT NULL,
  target_agency_id uuid NOT NULL,
  product_id uuid NOT NULL,
  reference_number text NOT NULL,
  quantity numeric(19,3) NOT NULL,
  status text NOT NULL
);

CREATE TABLE IF NOT EXISTS resource.material_resource (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  resource_code text NOT NULL,
  name text NOT NULL,
  category text NOT NULL,
  serial_number text NOT NULL,
  status text NOT NULL,
  latitude double precision,
  longitude double precision,
  ip_address text,
  mac_address text,
  UNIQUE (tenant_id, organization_id, resource_code)
);

CREATE TABLE IF NOT EXISTS settings.document_sequence (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid,
  agency_id uuid,
  document_type text NOT NULL,
  prefix text,
  suffix text,
  padding_width integer NOT NULL,
  next_number bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS sales.sales_order (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  customer_third_party_id uuid NOT NULL,
  product_id uuid NOT NULL,
  order_number text NOT NULL,
  quantity numeric(19,3) NOT NULL,
  unit_price numeric(19,2) NOT NULL,
  total_quantity numeric(19,3),
  subtotal_amount numeric(19,2),
  total_amount numeric(19,2),
  currency text NOT NULL,
  status text NOT NULL,
  UNIQUE (tenant_id, organization_id, order_number)
);

ALTER TABLE sales.sales_order ADD COLUMN IF NOT EXISTS total_quantity numeric(19,3);
ALTER TABLE sales.sales_order ADD COLUMN IF NOT EXISTS subtotal_amount numeric(19,2);
ALTER TABLE sales.sales_order ADD COLUMN IF NOT EXISTS total_amount numeric(19,2);

CREATE TABLE IF NOT EXISTS sales.sales_order_line (
  id uuid PRIMARY KEY,
  sales_order_id uuid NOT NULL,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  product_id uuid NOT NULL,
  quantity numeric(19,3) NOT NULL,
  unit_price numeric(19,2) NOT NULL,
  line_amount numeric(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounting.invoice (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  customer_third_party_id uuid NOT NULL,
  order_id uuid,
  product_id uuid NOT NULL,
  invoice_number text NOT NULL,
  quantity numeric(19,3) NOT NULL,
  unit_price numeric(19,2) NOT NULL,
  total_quantity numeric(19,3),
  subtotal_amount numeric(19,2),
  total_amount numeric(19,2),
  currency text NOT NULL,
  status text NOT NULL,
  UNIQUE (tenant_id, organization_id, invoice_number)
);

ALTER TABLE accounting.invoice ADD COLUMN IF NOT EXISTS total_quantity numeric(19,3);
ALTER TABLE accounting.invoice ADD COLUMN IF NOT EXISTS subtotal_amount numeric(19,2);
ALTER TABLE accounting.invoice ADD COLUMN IF NOT EXISTS total_amount numeric(19,2);

CREATE TABLE IF NOT EXISTS accounting.invoice_line (
  id uuid PRIMARY KEY,
  invoice_id uuid NOT NULL,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  product_id uuid NOT NULL,
  quantity numeric(19,3) NOT NULL,
  unit_price numeric(19,2) NOT NULL,
  line_amount numeric(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS treasury.bank_account (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  bank_third_party_id uuid,
  bank_name text NOT NULL,
  account_number text NOT NULL,
  iban text NOT NULL,
  currency text NOT NULL,
  status text NOT NULL,
  UNIQUE (tenant_id, organization_id, account_number)
);

CREATE TABLE IF NOT EXISTS treasury.bank_statement (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  bank_account_id uuid NOT NULL,
  statement_number text NOT NULL,
  statement_date date NOT NULL,
  opening_balance numeric(19,2) NOT NULL,
  closing_balance numeric(19,2) NOT NULL,
  UNIQUE (tenant_id, bank_account_id, statement_number)
);

CREATE TABLE IF NOT EXISTS treasury.check_payment (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  bank_account_id uuid NOT NULL,
  check_number text NOT NULL,
  amount numeric(19,2) NOT NULL,
  beneficiary text NOT NULL,
  status text NOT NULL,
  UNIQUE (tenant_id, bank_account_id, check_number)
);

CREATE TABLE IF NOT EXISTS treasury.reconciliation (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  bank_account_id uuid NOT NULL,
  statement_id uuid NOT NULL,
  reference_number text NOT NULL,
  status text NOT NULL,
  closed_at timestamptz,
  UNIQUE (tenant_id, bank_account_id, reference_number)
);
