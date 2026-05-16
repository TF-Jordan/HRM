create schema if not exists cashier;

create table if not exists cashier.cash_register (
    id uuid primary key,
    organization_id uuid not null,
    agency_id uuid null,
    code varchar(120) not null,
    label varchar(255) not null,
    status varchar(80) not null,
    assigned_cashier_id uuid null,
    created_at timestamptz not null
);
create index if not exists idx_cashier_cash_register_org on cashier.cash_register (organization_id);

create table if not exists cashier.cashier_profile (
    id uuid primary key,
    organization_id uuid not null,
    agency_id uuid null,
    kernel_user_id uuid null,
    email varchar(255) null,
    full_name varchar(255) not null,
    kind varchar(80) not null,
    active boolean not null,
    created_at timestamptz not null
);
create index if not exists idx_cashier_profile_org on cashier.cashier_profile (organization_id);

create table if not exists cashier.cashier_assignment (
    id uuid primary key,
    organization_id uuid not null,
    agency_id uuid not null,
    cashier_id uuid not null,
    assigned_at timestamptz not null
);
create index if not exists idx_cashier_assignment_org on cashier.cashier_assignment (organization_id);

create table if not exists cashier.cash_session (
    id uuid primary key,
    organization_id uuid not null,
    agency_id uuid null,
    register_id uuid not null,
    cashier_id uuid not null,
    status varchar(80) not null,
    opening_amount numeric(19,4) not null,
    closing_amount numeric(19,4) null,
    currency varchar(16) not null,
    opened_at timestamptz not null,
    closed_at timestamptz null,
    locked boolean not null,
    note text null
);
create index if not exists idx_cashier_session_org on cashier.cash_session (organization_id);

create table if not exists cashier.wallet_account (
    id uuid primary key,
    organization_id uuid not null,
    owner_id uuid not null,
    owner_name varchar(255) not null,
    number varchar(120) not null,
    balance numeric(19,4) not null,
    currency varchar(16) not null,
    type varchar(80) not null
);
create index if not exists idx_cashier_wallet_account_org on cashier.wallet_account (organization_id);

create table if not exists cashier.fund_request (
    id uuid primary key,
    organization_id uuid not null,
    register_id uuid not null,
    cashier_id uuid not null,
    amount numeric(19,4) not null,
    status varchar(80) not null,
    reason text null,
    created_at timestamptz not null
);
create index if not exists idx_cashier_fund_request_org on cashier.fund_request (organization_id);

create table if not exists cashier.bill (
    id uuid primary key,
    organization_id uuid not null,
    customer_id uuid not null,
    reference varchar(160) not null,
    total_amount numeric(19,4) not null,
    paid_amount numeric(19,4) not null,
    currency varchar(16) not null,
    status varchar(80) not null,
    created_at timestamptz not null
);
create index if not exists idx_cashier_bill_org on cashier.bill (organization_id);

create table if not exists cashier.cash_movement (
    id uuid primary key,
    organization_id uuid not null,
    session_id uuid null,
    register_id uuid null,
    account_id uuid null,
    type varchar(80) not null,
    amount numeric(19,4) not null,
    currency varchar(16) not null,
    reference varchar(160) not null,
    status varchar(80) not null,
    created_at timestamptz not null
);
create index if not exists idx_cashier_movement_org on cashier.cash_movement (organization_id);

create table if not exists cashier.cash_reconciliation (
    id uuid primary key,
    organization_id uuid not null,
    session_id uuid not null,
    register_id uuid not null,
    status varchar(80) not null,
    review text null,
    justification text null,
    created_at timestamptz not null,
    reviewed_at timestamptz null
);
create index if not exists idx_cashier_reconciliation_org on cashier.cash_reconciliation (organization_id);

create table if not exists cashier.audit_entry (
    id uuid primary key,
    organization_id uuid not null,
    action varchar(120) not null,
    target_type varchar(120) not null,
    target_id uuid null,
    details text null,
    created_at timestamptz not null
);
create index if not exists idx_cashier_audit_entry_org on cashier.audit_entry (organization_id);

create table if not exists cashier.cash_notification (
    id uuid primary key,
    organization_id uuid not null,
    channel varchar(80) not null,
    subject varchar(255) not null,
    recipient varchar(255) not null,
    status varchar(80) not null,
    created_at timestamptz not null
);
create index if not exists idx_cashier_notification_org on cashier.cash_notification (organization_id);

create table if not exists cashier.cash_document (
    id uuid primary key,
    organization_id uuid not null,
    type varchar(80) not null,
    target_id uuid null,
    reference varchar(160) not null,
    created_at timestamptz not null
);
create index if not exists idx_cashier_document_org on cashier.cash_document (organization_id);
