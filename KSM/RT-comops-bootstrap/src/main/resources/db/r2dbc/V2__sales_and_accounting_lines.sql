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

CREATE INDEX IF NOT EXISTS idx_sales_order_line_sales_order_id
  ON sales.sales_order_line (sales_order_id);

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

CREATE INDEX IF NOT EXISTS idx_accounting_invoice_line_invoice_id
  ON accounting.invoice_line (invoice_id);
