CREATE TABLE IF NOT EXISTS hrm_social_declaration (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL, type VARCHAR(20) NOT NULL, periode VARCHAR(7) NOT NULL,
    format VARCHAR(20) NOT NULL, statut VARCHAR(20) NOT NULL, fichier_id UUID,
    generated_at TIMESTAMP WITH TIME ZONE, submitted_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_hrm_social_decl_tenant_org ON hrm_social_declaration (tenant_id, organization_id);

CREATE TABLE IF NOT EXISTS hrm_rh_kpi_snapshot (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    organization_id UUID NOT NULL, periode VARCHAR(7) NOT NULL,
    effectif_total INT NOT NULL, effectif_actif INT NOT NULL,
    taux_turnover NUMERIC(8,4), taux_absenteisme NUMERIC(8,4),
    masse_salariale NUMERIC(19,4), couverture_competences NUMERIC(8,4)
);
CREATE INDEX IF NOT EXISTS idx_hrm_rh_kpi_tenant_org ON hrm_rh_kpi_snapshot (tenant_id, organization_id);
