-- Payroll Core — autonomous payroll module schema.
-- All tables are prefixed payroll_* and are independent of the hrm_* schema; the module
-- reads employee data through an integration port, not via foreign keys to hrm tables.

-- Configurable pay-element catalogue (rubriques de paie)
CREATE TABLE IF NOT EXISTS payroll_pay_element (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    code VARCHAR(60) NOT NULL,
    label VARCHAR(160) NOT NULL,
    category VARCHAR(20) NOT NULL,
    method VARCHAR(20) NOT NULL,
    base_reference VARCHAR(40),
    rate NUMERIC(12,6),
    ceiling NUMERIC(15,2),
    floor_value NUMERIC(15,2),
    exemption_threshold NUMERIC(15,2),
    flat_amount NUMERIC(15,2),
    bracket_table_code VARCHAR(60),
    lookup_table_code VARCHAR(60),
    taxable BOOLEAN NOT NULL DEFAULT FALSE,
    social_contributable BOOLEAN NOT NULL DEFAULT FALSE,
    country_code VARCHAR(3) NOT NULL,
    display_order INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL,
    effective_to DATE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_pay_element_code
    ON payroll_pay_element (tenant_id, code);
CREATE INDEX IF NOT EXISTS idx_payroll_pay_element_country
    ON payroll_pay_element (tenant_id, country_code, display_order);

-- Progressive scales (e.g. IRPP) and their brackets
CREATE TABLE IF NOT EXISTS payroll_tax_bracket_table (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    code VARCHAR(60) NOT NULL,
    label VARCHAR(160) NOT NULL,
    country_code VARCHAR(3) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_tax_bracket_table_code
    ON payroll_tax_bracket_table (tenant_id, code);

CREATE TABLE IF NOT EXISTS payroll_tax_bracket (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    table_id UUID NOT NULL REFERENCES payroll_tax_bracket_table(id) ON DELETE CASCADE,
    ordre INT NOT NULL,
    lower_bound NUMERIC(15,2) NOT NULL,
    upper_bound NUMERIC(15,2),
    rate NUMERIC(12,6) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_payroll_tax_bracket_table
    ON payroll_tax_bracket (tenant_id, table_id, ordre);

-- Stepped forfait scales (e.g. RAV, TDL) and their entries
CREATE TABLE IF NOT EXISTS payroll_lookup_table (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    code VARCHAR(60) NOT NULL,
    label VARCHAR(160) NOT NULL,
    country_code VARCHAR(3) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_lookup_table_code
    ON payroll_lookup_table (tenant_id, code);

CREATE TABLE IF NOT EXISTS payroll_lookup_entry (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    table_id UUID NOT NULL REFERENCES payroll_lookup_table(id) ON DELETE CASCADE,
    ordre INT NOT NULL,
    lower_bound NUMERIC(15,2) NOT NULL,
    upper_bound NUMERIC(15,2),
    amount NUMERIC(15,2) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_payroll_lookup_entry_table
    ON payroll_lookup_entry (tenant_id, table_id, ordre);

-- Per-employee monthly variable inputs
CREATE TABLE IF NOT EXISTS payroll_pay_variable (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    periode VARCHAR(7) NOT NULL,
    overtime_hours_day NUMERIC(8,2) NOT NULL DEFAULT 0,
    overtime_hours_night NUMERIC(8,2) NOT NULL DEFAULT 0,
    overtime_hours_sunday_holiday NUMERIC(8,2) NOT NULL DEFAULT 0,
    bonuses NUMERIC(15,2) NOT NULL DEFAULT 0,
    unpaid_absence_days NUMERIC(8,2) NOT NULL DEFAULT 0,
    advances NUMERIC(15,2) NOT NULL DEFAULT 0,
    worked_days_override INT,
    locked BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_pay_variable_unique
    ON payroll_pay_variable (tenant_id, employee_id, periode);
CREATE INDEX IF NOT EXISTS idx_payroll_pay_variable_org_period
    ON payroll_pay_variable (tenant_id, organization_id, periode);

-- Payroll runs (cycles)
CREATE TABLE IF NOT EXISTS payroll_run (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    agency_id UUID,
    periode VARCHAR(7) NOT NULL,
    run_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    total_gross NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_employee_deductions NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_income_tax NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_net NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_employer_charges NUMERIC(15,2) NOT NULL DEFAULT 0,
    nb_employes INT NOT NULL DEFAULT 0,
    calculated_at TIMESTAMP WITH TIME ZONE,
    validated_by UUID,
    validated_at TIMESTAMP WITH TIME ZONE,
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    paid_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_run_unique_org
    ON payroll_run (tenant_id, organization_id, periode, run_type) WHERE agency_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_run_unique_agency
    ON payroll_run (tenant_id, organization_id, agency_id, periode, run_type) WHERE agency_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_payroll_run_org
    ON payroll_run (tenant_id, organization_id);

-- Per-employee run results
CREATE TABLE IF NOT EXISTS payroll_entry (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    payroll_run_id UUID NOT NULL REFERENCES payroll_run(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL,
    currency VARCHAR(3) NOT NULL,
    salaire_base NUMERIC(15,2) NOT NULL DEFAULT 0,
    brut NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_deductions NUMERIC(15,2) NOT NULL DEFAULT 0,
    income_tax NUMERIC(15,2) NOT NULL DEFAULT 0,
    employer_charges NUMERIC(15,2) NOT NULL DEFAULT 0,
    net NUMERIC(15,2) NOT NULL DEFAULT 0,
    payment_status VARCHAR(15) NOT NULL,
    payment_channel VARCHAR(30) NOT NULL,
    account_ref VARCHAR(40)
);
CREATE INDEX IF NOT EXISTS idx_payroll_entry_run
    ON payroll_entry (tenant_id, payroll_run_id);
CREATE INDEX IF NOT EXISTS idx_payroll_entry_employee
    ON payroll_entry (tenant_id, employee_id);

-- Payslip detail lines
CREATE TABLE IF NOT EXISTS payroll_payslip_line (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payroll_entry_id UUID NOT NULL REFERENCES payroll_entry(id) ON DELETE CASCADE,
    pay_element_code VARCHAR(60),
    libelle VARCHAR(160) NOT NULL,
    type VARCHAR(15) NOT NULL,
    base NUMERIC(15,2),
    taux NUMERIC(12,6),
    montant NUMERIC(15,2) NOT NULL,
    ordre_affichage INT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_payroll_payslip_line_entry
    ON payroll_payslip_line (tenant_id, payroll_entry_id, ordre_affichage);

-- Year-to-date accumulators per employee
CREATE TABLE IF NOT EXISTS payroll_annual_accumulator (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    year INT NOT NULL,
    cumulative_gross NUMERIC(15,2) NOT NULL DEFAULT 0,
    cumulative_deductions NUMERIC(15,2) NOT NULL DEFAULT 0,
    cumulative_income_tax NUMERIC(15,2) NOT NULL DEFAULT 0,
    cumulative_net NUMERIC(15,2) NOT NULL DEFAULT 0,
    cumulative_employer_charges NUMERIC(15,2) NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_annual_accumulator_unique
    ON payroll_annual_accumulator (tenant_id, employee_id, year);
