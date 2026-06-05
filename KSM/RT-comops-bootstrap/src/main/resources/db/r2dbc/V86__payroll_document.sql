-- Payroll Core — registry of generated, electronically-sealed documents (payslips, STC, attestations).

CREATE TABLE IF NOT EXISTS payroll_document (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    subject_id UUID NOT NULL,
    periode VARCHAR(7),
    file_id UUID NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    canonical_content TEXT NOT NULL,
    algorithm VARCHAR(30) NOT NULL,
    content_hash_hex VARCHAR(64) NOT NULL,
    signature_base64 TEXT NOT NULL,
    key_id VARCHAR(60),
    signed_by VARCHAR(120),
    signed_at TIMESTAMP WITH TIME ZONE,
    verification_code VARCHAR(20)
);
CREATE INDEX IF NOT EXISTS idx_payroll_document_employee
    ON payroll_document (tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS idx_payroll_document_subject
    ON payroll_document (tenant_id, subject_id);
CREATE INDEX IF NOT EXISTS idx_payroll_document_verification
    ON payroll_document (tenant_id, verification_code);
