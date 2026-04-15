ALTER TABLE settings.app_business_settings
  ADD COLUMN IF NOT EXISTS default_currency text NOT NULL DEFAULT 'XAF',
  ADD COLUMN IF NOT EXISTS legal_identity text,
  ADD COLUMN IF NOT EXISTS tax_identifier text,
  ADD COLUMN IF NOT EXISTS require_sales_order_approval boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS require_return_approval boolean NOT NULL DEFAULT false;
