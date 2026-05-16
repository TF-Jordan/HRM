create table if not exists accounting.accounting_extension_plan_account (
    id uuid primary key,
    organization_id uuid not null,
    account_number varchar(255) not null,
    label varchar(255) not null,
    account_class varchar(80) not null,
    active boolean not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_plan_account_org
    on accounting.accounting_extension_plan_account (organization_id, account_number);

create table if not exists accounting.accounting_extension_account (
    id uuid primary key,
    organization_id uuid not null,
    account_number varchar(255) not null,
    label varchar(255) not null,
    account_type varchar(120) not null,
    external_id uuid null,
    active boolean not null,
    notes text null,
    created_at timestamptz not null,
    updated_at timestamptz null
);

create index if not exists idx_accounting_extension_account_org
    on accounting.accounting_extension_account (organization_id, account_number);

create table if not exists accounting.accounting_extension_journal (
    id uuid primary key,
    organization_id uuid not null,
    code varchar(120) not null,
    label varchar(255) not null,
    type varchar(120) not null,
    active boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz null
);

create index if not exists idx_accounting_extension_journal_org
    on accounting.accounting_extension_journal (organization_id, code);
