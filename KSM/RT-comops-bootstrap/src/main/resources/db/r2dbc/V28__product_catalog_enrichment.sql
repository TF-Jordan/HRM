ALTER TABLE product.product
  ADD COLUMN IF NOT EXISTS category_code text;

CREATE TABLE IF NOT EXISTS product.product_category (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  code text NOT NULL,
  name text NOT NULL,
  parent_code text,
  description text
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_product_category_code
  ON product.product_category (tenant_id, organization_id, code);

CREATE TABLE IF NOT EXISTS product.product_price (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  product_id uuid NOT NULL,
  price_type text NOT NULL,
  amount numeric(19,2) NOT NULL,
  currency text NOT NULL,
  effective_from timestamptz NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_price_effective_lookup
  ON product.product_price (tenant_id, product_id, price_type, effective_from DESC);
