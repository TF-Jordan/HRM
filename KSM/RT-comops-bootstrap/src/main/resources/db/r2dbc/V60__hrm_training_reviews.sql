-- HRM Core - Step 6: Training & Performance Reviews tables

CREATE TABLE IF NOT EXISTS hrm_training (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    intitule VARCHAR(255) NOT NULL,
    organisme VARCHAR(255),
    date_debut DATE NOT NULL,
    date_fin DATE,
    cout NUMERIC(15,2),
    nb_places INT,
    lieu VARCHAR(255),
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_training_tenant_org
    ON hrm_training (tenant_id, organization_id);

CREATE TABLE IF NOT EXISTS hrm_training_enrollment (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    training_id UUID NOT NULL REFERENCES hrm_training(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    note_evaluation NUMERIC(5,2),
    attestation_file_id UUID
);

CREATE INDEX IF NOT EXISTS idx_hrm_training_enrollment_training
    ON hrm_training_enrollment (tenant_id, training_id);

CREATE INDEX IF NOT EXISTS idx_hrm_training_enrollment_employee
    ON hrm_training_enrollment (tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS hrm_performance_review (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    evaluateur_party_id UUID,
    evaluateur_display_name VARCHAR(255),
    periode VARCHAR(7) NOT NULL,
    note_globale NUMERIC(5,2),
    commentaires TEXT,
    plan_action TEXT,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hrm_performance_review_tenant_org
    ON hrm_performance_review (tenant_id, organization_id);

CREATE INDEX IF NOT EXISTS idx_hrm_performance_review_employee
    ON hrm_performance_review (tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS hrm_review_objective (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    review_id UUID NOT NULL REFERENCES hrm_performance_review(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    poids NUMERIC(5,2),
    note_atteinte NUMERIC(5,2),
    commentaire TEXT
);

CREATE INDEX IF NOT EXISTS idx_hrm_review_objective_review
    ON hrm_review_objective (tenant_id, review_id);
