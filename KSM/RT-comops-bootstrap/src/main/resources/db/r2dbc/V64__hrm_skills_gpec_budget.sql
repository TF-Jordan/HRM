CREATE TABLE IF NOT EXISTS hrm_skill (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    name VARCHAR(255) NOT NULL, categorie VARCHAR(100), description VARCHAR(500)
);
CREATE INDEX IF NOT EXISTS idx_hrm_skill_tenant ON hrm_skill (tenant_id);

CREATE TABLE IF NOT EXISTS hrm_employee_skill (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES hrm_skill(id) ON DELETE CASCADE,
    niveau_actuel INT NOT NULL, niveau_attendu INT NOT NULL, date_evaluation DATE
);
CREATE INDEX IF NOT EXISTS idx_hrm_employee_skill_tenant_emp ON hrm_employee_skill (tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS hrm_training_budget (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL, agency_id UUID, annee INT NOT NULL,
    montant_alloue NUMERIC(19,4) NOT NULL, montant_engage NUMERIC(19,4) NOT NULL DEFAULT 0,
    montant_realise NUMERIC(19,4) NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_hrm_training_budget_tenant_org ON hrm_training_budget (tenant_id, organization_id, annee);
