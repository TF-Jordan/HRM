create table if not exists accounting.accounting_extension_cash_register_posting (
    id uuid primary key,
    organization_id uuid not null,
    register_reference varchar(255) not null,
    amount numeric(19, 2) not null,
    currency varchar(16) not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_cash_register_posting_org
    on accounting.accounting_extension_cash_register_posting (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_bank_statement_posting (
    id uuid primary key,
    organization_id uuid not null,
    statement_reference varchar(255) not null,
    amount numeric(19, 2) not null,
    currency varchar(16) not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_bank_statement_posting_org
    on accounting.accounting_extension_bank_statement_posting (organization_id, created_at desc);
