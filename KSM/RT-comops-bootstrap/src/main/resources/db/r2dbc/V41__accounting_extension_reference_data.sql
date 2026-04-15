create table if not exists accounting.accounting_extension_setting (
    id uuid primary key,
    organization_id uuid not null,
    code varchar(255) not null,
    value text not null,
    updated_at timestamptz not null
);

create index if not exists idx_accounting_extension_setting_org
    on accounting.accounting_extension_setting (organization_id, updated_at desc);

create table if not exists accounting.accounting_extension_currency (
    id uuid primary key,
    organization_id uuid not null,
    code varchar(32) not null,
    label varchar(255) not null,
    symbol varchar(32) not null,
    active boolean not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_currency_org
    on accounting.accounting_extension_currency (organization_id, code);

create table if not exists accounting.accounting_extension_exchange_rate (
    id uuid primary key,
    organization_id uuid not null,
    source_currency varchar(32) not null,
    target_currency varchar(32) not null,
    rate numeric(19, 6) not null,
    rate_date date not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_exchange_rate_org
    on accounting.accounting_extension_exchange_rate (organization_id, rate_date desc);

create table if not exists accounting.accounting_extension_tax_definition (
    id uuid primary key,
    organization_id uuid not null,
    code varchar(120) not null,
    label varchar(255) not null,
    rate numeric(19, 6) not null,
    active boolean not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_tax_definition_org
    on accounting.accounting_extension_tax_definition (organization_id, code);
