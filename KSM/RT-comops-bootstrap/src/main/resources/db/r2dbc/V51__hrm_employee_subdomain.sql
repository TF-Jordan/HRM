-- HRM Core - Step 1: Employee sub-domain tables

CREATE TABLE IF NOT EXISTS hrm_employee (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    actor_id UUID NOT NULL,
    matricule VARCHAR(20) NOT NULL,
    num_cnps VARCHAR(20),
    categorie INT NOT NULL,
    echelon VARCHAR(5),
    date_embauche DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    department_code VARCHAR(20),
    mode_paiement VARCHAR(30) NOT NULL,
    compte_bancaire VARCHAR(30),
    num_mobile_money VARCHAR(15),
    operateur_mm VARCHAR(10),
    actor_display_name VARCHAR(150)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_hrm_employee_tenant_matricule
    ON hrm_employee (tenant_id, matricule);

CREATE INDEX IF NOT EXISTS idx_hrm_employee_tenant_org
    ON hrm_employee (tenant_id, organization_id);

CREATE INDEX IF NOT EXISTS idx_hrm_employee_tenant_org_agency
    ON hrm_employee (tenant_id, organization_id, agency_id);

CREATE INDEX IF NOT EXISTS idx_hrm_employee_tenant_org_status
    ON hrm_employee (tenant_id, organization_id, status);

CREATE INDEX IF NOT EXISTS idx_hrm_employee_actor
    ON hrm_employee (actor_id, tenant_id);

CREATE TABLE IF NOT EXISTS hrm_contract (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    type VARCHAR(10) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    salaire_base NUMERIC(15,2) NOT NULL,
    avantages_nature NUMERIC(15,2),
    periode_essai INT,
    status VARCHAR(15) NOT NULL,
    motif_fin VARCHAR(255),
    document_file_id UUID
);

CREATE INDEX IF NOT EXISTS idx_hrm_contract_tenant_employee
    ON hrm_contract (tenant_id, employee_id);

CREATE INDEX IF NOT EXISTS idx_hrm_contract_tenant_employee_status
    ON hrm_contract (tenant_id, employee_id, status);

CREATE TABLE IF NOT EXISTS hrm_dependent (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL,
    lien_parente VARCHAR(30) NOT NULL,
    certificat_file_id UUID
);

CREATE INDEX IF NOT EXISTS idx_hrm_dependent_tenant_employee
    ON hrm_dependent (tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS hrm_leave_balance (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,
    type VARCHAR(15) NOT NULL,
    acquis NUMERIC(5,2) NOT NULL,
    pris NUMERIC(5,2) NOT NULL,
    annee INT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_hrm_leave_balance_unique
    ON hrm_leave_balance (tenant_id, employee_id, type, annee);

CREATE INDEX IF NOT EXISTS idx_hrm_leave_balance_tenant_employee
    ON hrm_leave_balance (tenant_id, employee_id);
