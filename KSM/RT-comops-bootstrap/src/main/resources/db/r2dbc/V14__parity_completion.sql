CREATE TABLE IF NOT EXISTS settings.app_business_settings (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid,
  negotiate_selling_price boolean NOT NULL DEFAULT false,
  selling_price_include_vat boolean NOT NULL DEFAULT false,
  authorize_exceptional_discount boolean NOT NULL DEFAULT false,
  grantable_discount_rate double precision NOT NULL DEFAULT 0,
  print_logo boolean NOT NULL DEFAULT true,
  paper_format text NOT NULL DEFAULT 'A4',
  length_of_vat_invoice_number integer NOT NULL DEFAULT 8,
  prefix_of_vat_invoice_number text,
  low_stock_alert boolean NOT NULL DEFAULT false,
  preventive_maintenance_alert boolean NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_business_settings_org_default
  ON settings.app_business_settings (tenant_id, organization_id)
  WHERE agency_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_business_settings_org_agency
  ON settings.app_business_settings (tenant_id, organization_id, agency_id)
  WHERE agency_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS organization.opening_hours_exception (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  exception_date date NOT NULL,
  label text NOT NULL,
  opens_at time,
  closes_at time,
  closed boolean NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_opening_hours_exception_agency_date_label
  ON organization.opening_hours_exception (tenant_id, agency_id, exception_date, label);

CREATE TABLE IF NOT EXISTS organization.point_of_interest_link (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  point_of_interest_id uuid NOT NULL,
  distance_meters integer,
  description text
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_point_of_interest_link_agency_poi
  ON organization.point_of_interest_link (tenant_id, agency_id, point_of_interest_id);

ALTER TABLE inventory.stock_movement
  ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT 'VALIDATED';

ALTER TABLE inventory.product_transformation
  ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT 'VALIDATED';

CREATE TABLE IF NOT EXISTS inventory.inventory_session (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  agency_id uuid NOT NULL,
  product_id uuid NOT NULL,
  reference_number text NOT NULL,
  counted_quantity numeric(19,3) NOT NULL,
  status text NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_inventory_session_ref
  ON inventory.inventory_session (tenant_id, organization_id, reference_number);

CREATE INDEX IF NOT EXISTS idx_inventory_session_org
  ON inventory.inventory_session (tenant_id, organization_id, agency_id);

CREATE TABLE IF NOT EXISTS treasury.bank_transaction (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  bank_account_id uuid NOT NULL,
  statement_id uuid,
  reference_number text NOT NULL,
  transaction_type text NOT NULL,
  transaction_date date NOT NULL,
  amount numeric(19,2) NOT NULL,
  description text NOT NULL,
  created_by uuid,
  status text NOT NULL,
  reconciled_at timestamptz
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_bank_transaction_ref
  ON treasury.bank_transaction (tenant_id, bank_account_id, reference_number);

CREATE INDEX IF NOT EXISTS idx_bank_transaction_account_date
  ON treasury.bank_transaction (tenant_id, bank_account_id, transaction_date DESC);

CREATE INDEX IF NOT EXISTS idx_bank_transaction_account_status
  ON treasury.bank_transaction (tenant_id, bank_account_id, status);
