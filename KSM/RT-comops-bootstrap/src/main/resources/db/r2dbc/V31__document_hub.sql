CREATE TABLE IF NOT EXISTS file_core.document_link (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL REFERENCES organization.organization(id) ON DELETE CASCADE,
    target_type VARCHAR(64) NOT NULL,
    target_id UUID NOT NULL,
    file_id UUID NOT NULL REFERENCES file_core.stored_file(id) ON DELETE CASCADE,
    document_category VARCHAR(64) NOT NULL,
    label VARCHAR(255),
    attached_by_user_id UUID,
    attached_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_document_link_target
    ON file_core.document_link (tenant_id, target_type, target_id);

CREATE INDEX IF NOT EXISTS idx_document_link_organization
    ON file_core.document_link (tenant_id, organization_id, document_category);
