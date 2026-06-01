alter table accounting.accounting_extension_draft_entry
    add column if not exists period_id uuid null,
    add column if not exists legacy_type varchar(80) not null default 'AUTRE',
    add column if not exists legacy_status varchar(80) not null default 'BROUILLON',
    add column if not exists source_id varchar(255) null,
    add column if not exists source_type varchar(120) null,
    add column if not exists piece_number varchar(255) null,
    add column if not exists label text null,
    add column if not exists total_amount numeric(19, 2) null,
    add column if not exists currency varchar(20) null,
    add column if not exists notes text null,
    add column if not exists attachment_ids_json text null,
    add column if not exists created_by varchar(255) null,
    add column if not exists validated_by varchar(255) null,
    add column if not exists validated_at timestamptz null,
    add column if not exists rejected_by varchar(255) null,
    add column if not exists rejected_at timestamptz null,
    add column if not exists rejection_reason text null,
    add column if not exists entry_id uuid null;

update accounting.accounting_extension_draft_entry
set legacy_type = coalesce(legacy_type, 'AUTRE'),
    legacy_status = case
        when posted_at is not null then 'VALIDE'
        else coalesce(legacy_status, 'BROUILLON')
    end,
    source_id = coalesce(source_id, reference),
    source_type = coalesce(source_type, 'MANUAL'),
    piece_number = coalesce(piece_number, reference),
    label = coalesce(label, reference),
    total_amount = coalesce(total_amount, 0),
    currency = coalesce(currency, 'XAF'),
    attachment_ids_json = coalesce(attachment_ids_json, '[]'),
    created_by = coalesce(created_by, 'system');
