-- V079: Extended employee profile — personal info + emergency contacts
--
-- hrm_employee_personal_info  : 1:1 with hrm_employee, nullable at first save.
-- hrm_employee_emergency_contact : 1:N with hrm_employee, ordered by priorite.

CREATE TABLE IF NOT EXISTS hrm_employee_personal_info (
    id                   UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id            UUID        NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    employee_id          UUID        NOT NULL UNIQUE REFERENCES hrm_employee(id) ON DELETE CASCADE,

    -- Personal identity extras
    lieu_naissance       VARCHAR(150),
    situation_matrimoniale VARCHAR(30),   -- SINGLE | MARRIED | DIVORCED | WIDOWED | SEPARATED
    type_piece           VARCHAR(30),    -- CNI | PASSPORT | DRIVING_LICENSE | RESIDENCE_PERMIT
    numero_piece         VARCHAR(50),
    date_emission_piece  DATE,
    niu_fiscal           VARCHAR(50),
    permis_conduire      VARCHAR(100),
    langues_parlees      VARCHAR(300),   -- comma-separated

    -- Contact extras
    email_personnel      VARCHAR(150),
    telephone_domicile   VARCHAR(30),
    whatsapp             VARCHAR(30),
    adresse_postale      VARCHAR(200),
    adresse_domicile     VARCHAR(200),
    ville                VARCHAR(100),
    region               VARCHAR(100),
    code_postal          VARCHAR(20)
);

CREATE INDEX IF NOT EXISTS idx_emp_personal_info_tenant_employee
    ON hrm_employee_personal_info (tenant_id, employee_id);

-- ──────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS hrm_employee_emergency_contact (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id   UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    employee_id UUID        NOT NULL REFERENCES hrm_employee(id) ON DELETE CASCADE,

    nom         VARCHAR(100) NOT NULL,
    prenom      VARCHAR(100),
    relation    VARCHAR(50),   -- SPOUSE | PARENT | SIBLING | FRIEND | DOCTOR | OTHER
    telephone   VARCHAR(30),
    email       VARCHAR(150),
    priorite    INT          NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_emp_emergency_contact_tenant_employee
    ON hrm_employee_emergency_contact (tenant_id, employee_id);
