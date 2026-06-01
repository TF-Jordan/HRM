-- HRM_MATRICULE document sequence for the demo organisation. Without it
-- POST /api/v1/hrm/employees crashes with DocumentSequenceNotFoundException
-- when SettingsPort.generateMatricule tries to allocate an employee code.
-- Kept idempotent so re-applying the seed never breaks.

INSERT INTO settings.document_sequence (
    id, tenant_id, created_at, updated_at, organization_id, agency_id,
    document_type, prefix, suffix, padding_width, next_number
) VALUES (
    '00000000-0000-0000-0000-0000000f0001', '00000000-0000-0000-0000-0000000a0001', now(), now(),
    '00000000-0000-0000-0000-0000000a0002', NULL,
    'HRM_MATRICULE', 'EMP', NULL, 6, 1
)
ON CONFLICT DO NOTHING;
