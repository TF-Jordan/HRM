create schema if not exists treasury;

create table if not exists treasury.bank (
    id uuid primary key,
    organization_id uuid not null,
    code varchar(120) not null,
    name varchar(255) not null,
    active boolean not null,
    created_at timestamptz not null
);
create index if not exists idx_treasury_bank_org on treasury.bank (organization_id);

create table if not exists treasury.transaction_type (
    id uuid primary key,
    organization_id uuid not null,
    code varchar(120) not null,
    label varchar(255) not null,
    inbound boolean not null,
    created_at timestamptz not null
);
create index if not exists idx_treasury_transaction_type_org on treasury.transaction_type (organization_id);

create table if not exists treasury.statement_line (
    id uuid primary key,
    statement_id uuid not null,
    reference varchar(160) not null,
    amount numeric(19,4) not null,
    currency varchar(16) not null,
    direction varchar(32) not null,
    status varchar(80) not null,
    reconciled boolean not null,
    created_at timestamptz not null
);
create index if not exists idx_treasury_statement_line_statement on treasury.statement_line (statement_id, created_at);

create table if not exists treasury.audit_log (
    id uuid primary key,
    organization_id uuid not null,
    action varchar(120) not null,
    target_type varchar(120) not null,
    target_id uuid null,
    details text null,
    created_at timestamptz not null
);
create index if not exists idx_treasury_audit_log_org on treasury.audit_log (organization_id, created_at desc);
