-- HRM Core - Step 2: Leave request table

CREATE TABLE IF NOT EXISTS hrm_leave_request (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    type VARCHAR(15) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    nb_jours NUMERIC(5,2) NOT NULL,
    status VARCHAR(15) NOT NULL,
    motif TEXT,
    valideur_party_id UUID,
    valideur_display_name VARCHAR(150),
    date_validation TIMESTAMP WITH TIME ZONE,
    commentaire_valideur TEXT,
    justificatif_file_id UUID
);

CREATE INDEX IF NOT EXISTS idx_hrm_leave_request_tenant_employee
    ON hrm_leave_request (tenant_id, employee_id);

CREATE INDEX IF NOT EXISTS idx_hrm_leave_request_tenant_org_status
    ON hrm_leave_request (tenant_id, organization_id, status);

CREATE INDEX IF NOT EXISTS idx_hrm_leave_request_tenant_org_agency_status
    ON hrm_leave_request (tenant_id, organization_id, agency_id, status);
