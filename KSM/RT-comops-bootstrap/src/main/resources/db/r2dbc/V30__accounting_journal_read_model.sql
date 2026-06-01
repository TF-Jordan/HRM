CREATE TABLE IF NOT EXISTS accounting.accounting_journal (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL REFERENCES organization.organization(id) ON DELETE CASCADE,
    code VARCHAR(32) NOT NULL,
    label VARCHAR(255) NOT NULL,
    type VARCHAR(64) NOT NULL,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_accounting_journal_scope_code
    ON accounting.accounting_journal (tenant_id, organization_id, code);
