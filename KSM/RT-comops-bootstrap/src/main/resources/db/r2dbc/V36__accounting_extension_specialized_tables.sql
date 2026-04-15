create table if not exists accounting.accounting_extension_entry (
    id uuid primary key,
    organization_id uuid not null,
    journal_id uuid not null,
    reference varchar(255) not null,
    entry_date timestamptz not null,
    status varchar(80) not null,
    lines_json text not null,
    created_at timestamptz not null,
    validated_at timestamptz null,
    cancelled_at timestamptz null,
    active boolean not null
);

create index if not exists idx_accounting_extension_entry_org
    on accounting.accounting_extension_entry (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_fixed_asset (
    id uuid primary key,
    organization_id uuid not null,
    reference varchar(255) not null,
    label varchar(255) not null,
    acquisition_cost numeric(19, 2) not null,
    useful_life_months integer not null,
    accumulated_depreciation numeric(19, 2) not null,
    status varchar(80) not null,
    acquired_at timestamptz not null,
    last_depreciated_at timestamptz null
);

create index if not exists idx_accounting_extension_fixed_asset_org
    on accounting.accounting_extension_fixed_asset (organization_id, acquired_at desc);

create table if not exists accounting.accounting_extension_tax_declaration (
    id uuid primary key,
    organization_id uuid not null,
    tax_type varchar(120) not null,
    period_label varchar(120) not null,
    taxable_base numeric(19, 2) not null,
    tax_amount numeric(19, 2) not null,
    status varchar(80) not null,
    created_at timestamptz not null,
    submitted_at timestamptz null
);

create index if not exists idx_accounting_extension_tax_declaration_org
    on accounting.accounting_extension_tax_declaration (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_attachment (
    id uuid primary key,
    organization_id uuid not null,
    target_type varchar(120) not null,
    target_id uuid not null,
    filename varchar(255) not null,
    content_type varchar(255) not null,
    size_bytes bigint not null,
    content bytea not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_attachment_org
    on accounting.accounting_extension_attachment (organization_id, created_at desc);
