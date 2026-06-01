-- HRM Core - Step 9: Medical & HSE tables

CREATE TABLE IF NOT EXISTS hrm_medical_visit (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    date_visite DATE NOT NULL,
    medecin VARCHAR(255) NOT NULL,
    resultat_aptitude VARCHAR(30) NOT NULL,
    restrictions VARCHAR(500),
    prochaine_echeance DATE NOT NULL,
    certificat_file_id UUID
);
CREATE INDEX IF NOT EXISTS idx_hrm_medical_visit_tenant_emp ON hrm_medical_visit (tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS hrm_medical_certificate (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    type_certificat VARCHAR(100) NOT NULL,
    date_emission DATE NOT NULL,
    date_expiration DATE NOT NULL,
    statut VARCHAR(30) NOT NULL,
    fichier_id UUID
);
CREATE INDEX IF NOT EXISTS idx_hrm_medical_cert_tenant_emp ON hrm_medical_certificate (tenant_id, employee_id);
