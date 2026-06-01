-- HRM Core - Step 7: Recruitment & Onboarding tables

CREATE TABLE IF NOT EXISTS hrm_job_offer (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    poste VARCHAR(255) NOT NULL,
    departement VARCHAR(255),
    localisation VARCHAR(255),
    competences_requises TEXT,
    date_limite DATE,
    package_salarial VARCHAR(500),
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_job_offer_tenant_org
    ON hrm_job_offer (tenant_id, organization_id);

CREATE TABLE IF NOT EXISTS hrm_application (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    job_offer_id UUID NOT NULL REFERENCES hrm_job_offer(id) ON DELETE CASCADE,
    candidat_nom VARCHAR(255) NOT NULL,
    candidat_prenom VARCHAR(255) NOT NULL,
    candidat_email VARCHAR(255),
    candidat_telephone VARCHAR(50),
    cv_file_id UUID,
    lettre_motivation_file_id UUID,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_application_tenant_job
    ON hrm_application (tenant_id, job_offer_id);

CREATE TABLE IF NOT EXISTS hrm_interview (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    application_id UUID NOT NULL REFERENCES hrm_application(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    date_heure TIMESTAMP WITH TIME ZONE NOT NULL,
    lieu VARCHAR(255),
    interviewer_party_id UUID,
    interviewer_display_name VARCHAR(255),
    notes TEXT,
    resultat VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_interview_tenant_app
    ON hrm_interview (tenant_id, application_id);

CREATE TABLE IF NOT EXISTS hrm_onboarding_task (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_to_party_id UUID,
    echeance DATE,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_onboarding_task_tenant_emp
    ON hrm_onboarding_task (tenant_id, employee_id);
