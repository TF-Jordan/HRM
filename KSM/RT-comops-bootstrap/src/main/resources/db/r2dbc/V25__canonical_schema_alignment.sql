-- =============================================================================
-- V25: Canonical schema alignment
--
-- Brings actor.actor, actor.business_actor_profile, organization.organization,
-- organization.agency, tp.third_party, and the new common_core schema (address,
-- contact) in line with the canonical domain model.  All operations use
-- ADD COLUMN IF NOT EXISTS so the migration is safe to re-run and compatible
-- with environments where prior partial migrations may have added some columns.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- actor.actor — add missing canonical columns
-- ---------------------------------------------------------------------------
ALTER TABLE actor.actor
    ADD COLUMN IF NOT EXISTS organization_id uuid,
    ADD COLUMN IF NOT EXISTS name           text,
    ADD COLUMN IF NOT EXISTS description    text,
    ADD COLUMN IF NOT EXISTS type           text,
    ADD COLUMN IF NOT EXISTS photo_uri      text,
    ADD COLUMN IF NOT EXISTS photo_id       uuid,
    ADD COLUMN IF NOT EXISTS addresses      uuid[],
    ADD COLUMN IF NOT EXISTS contacts       uuid[];

UPDATE actor.actor
SET name = COALESCE(name, trim(concat_ws(' ', first_name, last_name))),
    description = COALESCE(description, biography),
    type = COALESCE(type, 'ACTOR')
WHERE name IS NULL
   OR description IS NULL
   OR type IS NULL;

-- ---------------------------------------------------------------------------
-- actor.business_actor_profile — add missing canonical columns
-- ---------------------------------------------------------------------------
ALTER TABLE actor.business_actor_profile
    ADD COLUMN IF NOT EXISTS governance_status    text,
    ADD COLUMN IF NOT EXISTS governed_by_user_id  uuid,
    ADD COLUMN IF NOT EXISTS governed_at          timestamptz,
    ADD COLUMN IF NOT EXISTS governance_reason    text,
    ADD COLUMN IF NOT EXISTS code                 text,
    ADD COLUMN IF NOT EXISTS is_individual        boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS is_available         boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS is_verified          boolean     NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS is_active            boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS type                 text,
    ADD COLUMN IF NOT EXISTS role                 text,
    ADD COLUMN IF NOT EXISTS qualifications       text[],
    ADD COLUMN IF NOT EXISTS payment_methods      text[],
    ADD COLUMN IF NOT EXISTS addresses            uuid[],
    ADD COLUMN IF NOT EXISTS biography            text,
    ADD COLUMN IF NOT EXISTS deleted_at           timestamptz;

UPDATE actor.business_actor_profile
SET code = COALESCE(code, upper(regexp_replace(COALESCE(business_id, name, actor_id::text), '[^A-Za-z0-9]+', '_', 'g'))),
    type = COALESCE(type, 'BUSINESS_ACTOR'),
    role = COALESCE(role, 'OWNER'),
    biography = COALESCE(biography, business_profile),
    qualifications = COALESCE(qualifications, ARRAY[]::text[]),
    payment_methods = COALESCE(payment_methods, ARRAY[]::text[]),
    addresses = COALESCE(addresses, ARRAY[]::uuid[]),
    is_available = COALESCE(is_available, true),
    is_active = COALESCE(is_active, true)
WHERE code IS NULL
   OR type IS NULL
   OR role IS NULL
   OR biography IS NULL
   OR qualifications IS NULL
   OR payment_methods IS NULL
   OR addresses IS NULL;

-- ---------------------------------------------------------------------------
-- organization.organization — add missing canonical columns
-- ---------------------------------------------------------------------------
ALTER TABLE organization.organization
    ADD COLUMN IF NOT EXISTS governance_status              text,
    ADD COLUMN IF NOT EXISTS governed_by_user_id            uuid,
    ADD COLUMN IF NOT EXISTS governed_at                    timestamptz,
    ADD COLUMN IF NOT EXISTS governance_reason              text,
    ADD COLUMN IF NOT EXISTS service                        text,
    ADD COLUMN IF NOT EXISTS is_individual_business         boolean     NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS email                          text,
    ADD COLUMN IF NOT EXISTS short_name                     text,
    ADD COLUMN IF NOT EXISTS long_name                      text,
    ADD COLUMN IF NOT EXISTS description                    text,
    ADD COLUMN IF NOT EXISTS logo_uri                       text,
    ADD COLUMN IF NOT EXISTS logo_id                        uuid,
    ADD COLUMN IF NOT EXISTS website_url                    text,
    ADD COLUMN IF NOT EXISTS social_network                 text,
    ADD COLUMN IF NOT EXISTS business_registration_number   text,
    ADD COLUMN IF NOT EXISTS tax_number                     text,
    ADD COLUMN IF NOT EXISTS capital_share                  numeric(19,2),
    ADD COLUMN IF NOT EXISTS ceo_name                       text,
    ADD COLUMN IF NOT EXISTS year_founded                   integer,
    ADD COLUMN IF NOT EXISTS keywords                       text[],
    ADD COLUMN IF NOT EXISTS number_of_employees            integer,
    ADD COLUMN IF NOT EXISTS legal_form                     text,
    ADD COLUMN IF NOT EXISTS is_active                      boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS status                         text,
    ADD COLUMN IF NOT EXISTS deleted_at                     timestamptz;

UPDATE organization.organization
SET service = COALESCE(service, organization_type),
    short_name = COALESCE(short_name, display_name),
    long_name = COALESCE(long_name, legal_name),
    status = COALESCE(status, governance_status, CASE WHEN is_active THEN 'ACTIVE' ELSE 'INACTIVE' END),
    legal_form = COALESCE(legal_form, business_registration_number),
    keywords = COALESCE(keywords, ARRAY[]::text[])
WHERE service IS NULL
   OR short_name IS NULL
   OR long_name IS NULL
   OR status IS NULL
   OR keywords IS NULL;

-- ---------------------------------------------------------------------------
-- organization.agency — add missing canonical columns
-- ---------------------------------------------------------------------------
ALTER TABLE organization.agency
    ADD COLUMN IF NOT EXISTS governance_status          text,
    ADD COLUMN IF NOT EXISTS governed_by_user_id        uuid,
    ADD COLUMN IF NOT EXISTS governed_at                timestamptz,
    ADD COLUMN IF NOT EXISTS governance_reason          text,
    ADD COLUMN IF NOT EXISTS owner_id                   uuid,
    ADD COLUMN IF NOT EXISTS manager_id                 uuid,
    ADD COLUMN IF NOT EXISTS location                   text,
    ADD COLUMN IF NOT EXISTS description                text,
    ADD COLUMN IF NOT EXISTS transferable               boolean     NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS logo_uri                   text,
    ADD COLUMN IF NOT EXISTS logo_id                    uuid,
    ADD COLUMN IF NOT EXISTS short_name                 text,
    ADD COLUMN IF NOT EXISTS long_name                  text,
    ADD COLUMN IF NOT EXISTS is_individual_business     boolean     NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS is_headquarter             boolean     NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS country                    text,
    ADD COLUMN IF NOT EXISTS city                       text,
    ADD COLUMN IF NOT EXISTS latitude                   double precision,
    ADD COLUMN IF NOT EXISTS longitude                  double precision,
    ADD COLUMN IF NOT EXISTS open_time                  text,
    ADD COLUMN IF NOT EXISTS close_time                 text,
    ADD COLUMN IF NOT EXISTS phone                      text,
    ADD COLUMN IF NOT EXISTS email                      text,
    ADD COLUMN IF NOT EXISTS whatsapp                   text,
    ADD COLUMN IF NOT EXISTS greeting_message           text,
    ADD COLUMN IF NOT EXISTS average_revenue            numeric(19,2),
    ADD COLUMN IF NOT EXISTS capital_share              numeric(19,2),
    ADD COLUMN IF NOT EXISTS registration_number        text,
    ADD COLUMN IF NOT EXISTS social_network             text,
    ADD COLUMN IF NOT EXISTS tax_number                 text,
    ADD COLUMN IF NOT EXISTS keywords                   text[],
    ADD COLUMN IF NOT EXISTS is_active                  boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS is_public                  boolean     NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS is_business                boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS total_affiliated_customers integer,
    ADD COLUMN IF NOT EXISTS deleted_at                 timestamptz;

UPDATE organization.agency
SET short_name = COALESCE(short_name, name),
    long_name = COALESCE(long_name, name),
    is_active = COALESCE(is_active, active),
    email = COALESCE(email, NULL),
    keywords = COALESCE(keywords, ARRAY[]::text[])
WHERE short_name IS NULL
   OR long_name IS NULL
   OR keywords IS NULL;

-- ---------------------------------------------------------------------------
-- tp.third_party — add missing canonical columns
-- ---------------------------------------------------------------------------
ALTER TABLE tp.third_party
    ADD COLUMN IF NOT EXISTS code                       text,
    ADD COLUMN IF NOT EXISTS type                       text,
    ADD COLUMN IF NOT EXISTS legal_form                 text,
    ADD COLUMN IF NOT EXISTS unique_identification_number text,
    ADD COLUMN IF NOT EXISTS trade_registration_number  text,
    ADD COLUMN IF NOT EXISTS name                       text,
    ADD COLUMN IF NOT EXISTS acronym                    text,
    ADD COLUMN IF NOT EXISTS long_name                  text,
    ADD COLUMN IF NOT EXISTS logo_uri                   text,
    ADD COLUMN IF NOT EXISTS logo_id                    uuid,
    ADD COLUMN IF NOT EXISTS accounting_account_numbers text[],
    ADD COLUMN IF NOT EXISTS authorized_payment_methods text[],
    ADD COLUMN IF NOT EXISTS authorized_credit_limit    numeric(19,2),
    ADD COLUMN IF NOT EXISTS max_discount_rate          numeric(5,2),
    ADD COLUMN IF NOT EXISTS vat_subject                boolean     NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS operations_balance         numeric(19,2),
    ADD COLUMN IF NOT EXISTS opening_balance            numeric(19,2),
    ADD COLUMN IF NOT EXISTS pay_term_number            integer,
    ADD COLUMN IF NOT EXISTS pay_term_type              text,
    ADD COLUMN IF NOT EXISTS third_party_family         text,
    ADD COLUMN IF NOT EXISTS classification             text,
    ADD COLUMN IF NOT EXISTS tax_number                 text,
    ADD COLUMN IF NOT EXISTS loyalty_points             integer     NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS loyalty_points_used        integer     NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS loyalty_points_expired     integer     NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS enabled                    boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS deleted_at                 timestamptz;

UPDATE tp.third_party
SET code = COALESCE(code, reference_code),
    type = COALESCE(type,
            CASE
                WHEN prospect THEN 'PROSPECT'
                WHEN roles IS NOT NULL AND cardinality(roles) > 0 THEN upper(roles[1])
                ELSE upper(party_type)
            END),
    name = COALESCE(name, display_name),
    long_name = COALESCE(long_name, name, display_name),
    accounting_account_numbers = COALESCE(
            accounting_account_numbers,
            CASE
                WHEN accounting_account IS NOT NULL AND accounting_account <> '' THEN ARRAY[accounting_account]
                ELSE ARRAY[]::text[]
            END),
    authorized_payment_methods = COALESCE(authorized_payment_methods, ARRAY[]::text[]),
    third_party_family = COALESCE(third_party_family,
            CASE
                WHEN prospect THEN 'PROSPECT'
                WHEN roles IS NOT NULL AND cardinality(roles) > 0 THEN upper(roles[1])
                ELSE NULL
            END),
    enabled = COALESCE(enabled, active)
WHERE code IS NULL
   OR type IS NULL
   OR name IS NULL
   OR long_name IS NULL
   OR accounting_account_numbers IS NULL
   OR authorized_payment_methods IS NULL
   OR third_party_family IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_third_party_code
    ON tp.third_party (tenant_id, organization_id, code);

-- ---------------------------------------------------------------------------
-- common_core schema — address and contact (new tables)
-- ---------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS common_core;

CREATE TABLE IF NOT EXISTS common_core.address (
    id                   uuid        PRIMARY KEY,
    tenant_id            uuid        NOT NULL,
    created_at           timestamptz NOT NULL,
    updated_at           timestamptz NOT NULL,
    deleted_at           timestamptz,
    addressable_type     text        NOT NULL,
    addressable_id       uuid        NOT NULL,
    type                 text,
    address_line_1       text,
    address_line_2       text,
    city                 text,
    state                text,
    locality             text,
    country_id           uuid,
    zip_code             text,
    postal_code          text,
    po_box               text,
    is_default           boolean     NOT NULL DEFAULT false,
    neighborhood         text,
    informal_description text,
    latitude             double precision,
    longitude            double precision
);

CREATE INDEX IF NOT EXISTS idx_address_addressable
    ON common_core.address (tenant_id, addressable_type, addressable_id);

CREATE TABLE IF NOT EXISTS common_core.contact (
    id                       uuid        PRIMARY KEY,
    tenant_id                uuid        NOT NULL,
    created_at               timestamptz NOT NULL,
    updated_at               timestamptz NOT NULL,
    deleted_at               timestamptz,
    contactable_type         text        NOT NULL,
    contactable_id           uuid        NOT NULL,
    first_name               text,
    last_name                text,
    title                    text,
    is_email_verified        boolean     NOT NULL DEFAULT false,
    is_phone_number_verified boolean     NOT NULL DEFAULT false,
    is_favorite              boolean     NOT NULL DEFAULT false,
    phone_number             text,
    secondary_phone_number   text,
    fax_number               text,
    email                    text,
    secondary_email          text,
    email_verified_at        timestamptz,
    phone_verified_at        timestamptz
);

CREATE INDEX IF NOT EXISTS idx_contact_contactable
    ON common_core.contact (tenant_id, contactable_type, contactable_id);
