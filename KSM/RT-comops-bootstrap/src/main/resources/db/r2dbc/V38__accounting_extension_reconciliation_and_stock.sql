create table if not exists accounting.accounting_extension_bank_reconciliation (
    id uuid primary key,
    organization_id uuid not null,
    reconciliation_reference varchar(255) not null,
    bank_account_number varchar(255) not null,
    matched_amount numeric(19, 2) not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_bank_reconciliation_org
    on accounting.accounting_extension_bank_reconciliation (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_stock_movement_posting (
    id uuid primary key,
    organization_id uuid not null,
    movement_reference varchar(255) not null,
    movement_type varchar(120) not null,
    valuation_amount numeric(19, 2) not null,
    currency varchar(16) not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_stock_movement_posting_org
    on accounting.accounting_extension_stock_movement_posting (organization_id, created_at desc);
