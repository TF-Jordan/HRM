create schema if not exists billing;

create table if not exists billing.commercial_document (
    id uuid primary key,
    organization_id uuid not null,
    type varchar(80) not null,
    document_number varchar(120) not null,
    counterparty_third_party_id uuid not null,
    currency varchar(16) not null,
    status varchar(80) not null,
    created_at timestamptz not null
);

create index if not exists idx_billing_commercial_document_org_type
    on billing.commercial_document (organization_id, type);

create table if not exists billing.commercial_document_line (
    id uuid primary key,
    document_id uuid not null,
    line_index integer not null,
    product_id uuid not null,
    quantity numeric(19, 4) not null,
    unit_price numeric(19, 4) not null
);

create index if not exists idx_billing_commercial_document_line_doc
    on billing.commercial_document_line (document_id, line_index);

create table if not exists billing.payment (
    id uuid primary key,
    organization_id uuid not null,
    invoice_id uuid null,
    supplier_invoice_id uuid null,
    counterparty_third_party_id uuid null,
    reference varchar(160) not null,
    amount numeric(19, 4) not null,
    currency varchar(16) not null,
    status varchar(80) not null,
    paid_at timestamptz not null
);

create index if not exists idx_billing_payment_org
    on billing.payment (organization_id, paid_at desc);
