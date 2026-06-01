create table if not exists accounting.accounting_extension_draft_entry (
    id uuid primary key,
    organization_id uuid not null,
    journal_id uuid not null,
    reference varchar(255) not null,
    entry_date timestamptz not null,
    lines_json text not null,
    created_at timestamptz not null,
    posted_at timestamptz null
);

create index if not exists idx_accounting_extension_draft_entry_org
    on accounting.accounting_extension_draft_entry (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_lettering (
    id uuid primary key,
    organization_id uuid not null,
    debit_entry_id uuid not null,
    credit_entry_id uuid not null,
    matched_amount numeric(19, 2) not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_lettering_org
    on accounting.accounting_extension_lettering (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_pointing (
    id uuid primary key,
    organization_id uuid not null,
    account_id uuid not null,
    entry_id uuid not null,
    notes text not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_pointing_org
    on accounting.accounting_extension_pointing (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_invoice_accounting (
    id uuid primary key,
    organization_id uuid not null,
    invoice_id uuid not null,
    customer_third_party_id uuid not null,
    customer_accounting_account varchar(255) null,
    accounting_status varchar(120) not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_invoice_accounting_org
    on accounting.accounting_extension_invoice_accounting (organization_id, created_at desc);
