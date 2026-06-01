create table if not exists accounting.accounting_extension_operation (
    id uuid primary key,
    organization_id uuid not null,
    operation_type varchar(120) not null,
    reference varchar(255) not null,
    amount numeric(19, 2) not null,
    currency varchar(16) not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_operation_org
    on accounting.accounting_extension_operation (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_journal_audit (
    id uuid primary key,
    organization_id uuid not null,
    action varchar(120) not null,
    target_type varchar(120) not null,
    target_id uuid not null,
    details text not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_journal_audit_org
    on accounting.accounting_extension_journal_audit (organization_id, created_at desc);
