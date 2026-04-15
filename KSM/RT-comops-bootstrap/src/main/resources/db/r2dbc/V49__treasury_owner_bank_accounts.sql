ALTER TABLE treasury.bank_account
  ADD COLUMN IF NOT EXISTS owner_third_party_id uuid;

CREATE INDEX IF NOT EXISTS idx_treasury_bank_account_owner
  ON treasury.bank_account(tenant_id, organization_id, owner_third_party_id)
  WHERE owner_third_party_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_treasury_bank_account_owner
  ON treasury.bank_account(tenant_id, organization_id, owner_third_party_id)
  WHERE owner_third_party_id IS NOT NULL;
