create table if not exists accounting.accounting_extension_invoice_upload (
    id uuid primary key,
    organization_id uuid not null,
    filename varchar(255) not null,
    content_type varchar(255) not null,
    size_bytes bigint not null,
    created_at timestamptz not null
);

create index if not exists idx_accounting_extension_invoice_upload_org
    on accounting.accounting_extension_invoice_upload (organization_id, created_at desc);

create table if not exists accounting.accounting_extension_imported_bank_statement_lines (
    id uuid primary key,
    organization_id uuid not null,
    rows_json text not null
);

create index if not exists idx_accounting_extension_imported_bank_statement_lines_org
    on accounting.accounting_extension_imported_bank_statement_lines (organization_id);
