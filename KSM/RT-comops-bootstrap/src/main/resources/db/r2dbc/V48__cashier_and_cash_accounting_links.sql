alter table cashier.cash_register
    add column if not exists accounting_account_id uuid null,
    add column if not exists accounting_account_number varchar(120) null;

create index if not exists idx_cashier_cash_register_accounting_account
    on cashier.cash_register (accounting_account_id);

alter table cashier.wallet_account
    add column if not exists linked_third_party_id uuid null;

create index if not exists idx_cashier_wallet_account_linked_tp
    on cashier.wallet_account (linked_third_party_id);

alter table cashier.cash_movement
    add column if not exists accounting_posting_id uuid null,
    add column if not exists accounting_entry_type varchar(80) null,
    add column if not exists register_account_number varchar(120) null,
    add column if not exists counterparty_account_number varchar(120) null;

create index if not exists idx_cashier_movement_accounting_posting
    on cashier.cash_movement (accounting_posting_id);

alter table accounting.accounting_extension_cash_register_posting
    add column if not exists register_id uuid null,
    add column if not exists register_account_id uuid null,
    add column if not exists register_account_number varchar(120) null,
    add column if not exists posting_type varchar(80) null,
    add column if not exists session_id uuid null,
    add column if not exists movement_id uuid null,
    add column if not exists debit_account_number varchar(120) null,
    add column if not exists credit_account_number varchar(120) null,
    add column if not exists counterparty_account_number varchar(120) null,
    add column if not exists note text null;

create index if not exists idx_accounting_cash_register_posting_register_id
    on accounting.accounting_extension_cash_register_posting (register_id);

create index if not exists idx_accounting_cash_register_posting_register_account_id
    on accounting.accounting_extension_cash_register_posting (register_account_id);
