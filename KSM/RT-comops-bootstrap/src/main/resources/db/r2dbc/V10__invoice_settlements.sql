ALTER TABLE accounting.invoice
  ADD COLUMN IF NOT EXISTS payment_status text NOT NULL DEFAULT 'UNPAID';

ALTER TABLE accounting.invoice
  ADD COLUMN IF NOT EXISTS settled_amount numeric(19,2) NOT NULL DEFAULT 0.00;

ALTER TABLE accounting.invoice
  ADD COLUMN IF NOT EXISTS outstanding_amount numeric(19,2);

ALTER TABLE accounting.invoice
  ADD COLUMN IF NOT EXISTS settled_at timestamptz;

UPDATE accounting.invoice
SET outstanding_amount = COALESCE(total_amount, 0)
WHERE outstanding_amount IS NULL;

ALTER TABLE accounting.invoice
  ALTER COLUMN outstanding_amount SET NOT NULL;

CREATE TABLE IF NOT EXISTS treasury.invoice_settlement (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  organization_id uuid NOT NULL,
  bank_account_id uuid NOT NULL,
  invoice_id uuid NOT NULL,
  settlement_number text NOT NULL,
  payment_method text NOT NULL,
  amount numeric(19,2) NOT NULL,
  currency text NOT NULL,
  status text NOT NULL,
  UNIQUE (tenant_id, organization_id, settlement_number)
);

CREATE INDEX IF NOT EXISTS idx_treasury_invoice_settlement_invoice_id
  ON treasury.invoice_settlement (tenant_id, organization_id, invoice_id, created_at DESC);
