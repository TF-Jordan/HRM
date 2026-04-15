create table if not exists accounting.accounting_extension_snapshot (
    snapshot_key varchar(120) primary key,
    payload text not null,
    updated_at timestamptz not null default now()
);
