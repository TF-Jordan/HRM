-- =============================================================================
-- V26: Canonical secondary entities alignment
-- =============================================================================

CREATE TABLE IF NOT EXISTS organization.business_domain (
    id          uuid        PRIMARY KEY,
    tenant_id   uuid        NOT NULL,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz NOT NULL,
    deleted_at  timestamptz,
    code        text        NOT NULL,
    service     text,
    parent_id   uuid,
    name        text        NOT NULL,
    image_uri   text,
    image_id    uuid,
    type        text,
    type_label  text,
    description text
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_business_domain_code
    ON organization.business_domain (tenant_id, code);

CREATE TABLE IF NOT EXISTS organization.certification (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    deleted_at      timestamptz,
    organization_id uuid        NOT NULL,
    type            text,
    name            text        NOT NULL,
    description     text,
    obtainment_date timestamptz
);

CREATE INDEX IF NOT EXISTS idx_certification_organization
    ON organization.certification (tenant_id, organization_id);

CREATE TABLE IF NOT EXISTS organization.proposed_activity (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    deleted_at      timestamptz,
    organization_id uuid        NOT NULL,
    type            text,
    name            text        NOT NULL,
    rate            numeric(19,2),
    description     text
);

CREATE INDEX IF NOT EXISTS idx_proposed_activity_organization
    ON organization.proposed_activity (tenant_id, organization_id);

CREATE TABLE IF NOT EXISTS organization.agency_affiliation (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    deleted_at      timestamptz,
    organization_id uuid        NOT NULL,
    agency_id       uuid        NOT NULL,
    actor_id        uuid        NOT NULL,
    type            text,
    is_active       boolean     NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_agency_affiliation_lookup
    ON organization.agency_affiliation (tenant_id, organization_id, agency_id, actor_id);

CREATE TABLE IF NOT EXISTS organization.organization_actor (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    deleted_at      timestamptz,
    organization_id uuid        NOT NULL,
    actor_id        uuid        NOT NULL,
    type            text
);

CREATE INDEX IF NOT EXISTS idx_organization_actor_lookup
    ON organization.organization_actor (tenant_id, organization_id, actor_id);

CREATE TABLE IF NOT EXISTS organization.organization_domain (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    deleted_at      timestamptz,
    organization_id uuid        NOT NULL,
    domain_id       uuid        NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_organization_domain_link
    ON organization.organization_domain (tenant_id, organization_id, domain_id);

CREATE TABLE IF NOT EXISTS organization.agency_domain (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    deleted_at      timestamptz,
    organization_id uuid        NOT NULL,
    agency_id       uuid        NOT NULL,
    domain_id       uuid        NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_agency_domain_link
    ON organization.agency_domain (tenant_id, agency_id, domain_id);

CREATE TABLE IF NOT EXISTS tp.interaction (
    id               uuid        PRIMARY KEY,
    tenant_id        uuid        NOT NULL,
    created_at       timestamptz NOT NULL,
    updated_at       timestamptz NOT NULL,
    deleted_at       timestamptz,
    interaction_id   uuid,
    prospect_id      uuid        NOT NULL,
    interaction_date timestamptz,
    notes            text
);

CREATE INDEX IF NOT EXISTS idx_tp_interaction_prospect
    ON tp.interaction (tenant_id, prospect_id, interaction_date);
