CREATE UNIQUE INDEX IF NOT EXISTS ux_treasury_bank_statement_number
  ON treasury.bank_statement (tenant_id, bank_account_id, statement_number);

CREATE UNIQUE INDEX IF NOT EXISTS ux_treasury_reconciliation_reference
  ON treasury.reconciliation (tenant_id, bank_account_id, reference_number);

CREATE INDEX IF NOT EXISTS idx_treasury_bank_statement_org_account
  ON treasury.bank_statement (tenant_id, organization_id, bank_account_id);

CREATE INDEX IF NOT EXISTS idx_treasury_check_payment_org_account
  ON treasury.check_payment (tenant_id, organization_id, bank_account_id);

CREATE INDEX IF NOT EXISTS idx_treasury_reconciliation_org_account
  ON treasury.reconciliation (tenant_id, organization_id, bank_account_id);
