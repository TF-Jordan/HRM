create table if not exists accounting.accounting_extension_item (
    item_key varchar(255) primary key,
    scope varchar(80) not null,
    item_type varchar(120) not null,
    item_id uuid not null,
    organization_id uuid null,
    payload text not null,
    updated_at timestamptz not null default now()
);

create index if not exists idx_accounting_extension_item_scope_type
    on accounting.accounting_extension_item (scope, item_type);

create index if not exists idx_accounting_extension_item_org
    on accounting.accounting_extension_item (organization_id);
