create schema if not exists file;

create table if not exists settings.operational_policy_profile (
    id uuid primary key,
    tenant_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    organization_id uuid not null,
    agency_id uuid null,
    assignment_requires_approval boolean not null,
    allow_cross_agency_asset_assignment boolean not null,
    site_opening_checklist_required boolean not null,
    mandatory_document_approval boolean not null,
    inventory_variance_tolerance_percent integer not null,
    maintenance_alert_threshold_days integer not null,
    low_utilization_threshold_percent integer not null,
    max_open_inventory_campaigns integer not null,
    require_inventory_supervisor_approval boolean not null,
    automatic_lifecycle_events boolean not null,
    strict_document_expiry boolean not null
);
create unique index if not exists uk_operational_policy_scope
    on settings.operational_policy_profile (tenant_id, organization_id, coalesce(agency_id, '00000000-0000-0000-0000-000000000000'::uuid));

create table if not exists organization.operational_site_profile (
    id uuid primary key,
    tenant_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    organization_id uuid not null,
    agency_id uuid not null,
    site_category varchar(80) not null,
    operating_model varchar(80) not null,
    opening_status varchar(80) not null,
    cash_enabled boolean not null,
    warehouse_enabled boolean not null,
    maintenance_enabled boolean not null,
    inventory_enabled boolean not null,
    document_compliance_required boolean not null,
    default_physical_space_id uuid null,
    readiness_notes text null,
    commissioned_at timestamptz null
);
create unique index if not exists uk_operational_site_profile_scope
    on organization.operational_site_profile (tenant_id, organization_id, agency_id);

create table if not exists organization.operational_responsibility (
    id uuid primary key,
    tenant_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    organization_id uuid not null,
    agency_id uuid not null,
    physical_space_id uuid null,
    actor_id uuid not null,
    responsibility_type varchar(80) not null,
    primary_responsibility boolean not null,
    active boolean not null,
    notes text null
);
create index if not exists idx_operational_responsibility_org on organization.operational_responsibility (tenant_id, organization_id);
create index if not exists idx_operational_responsibility_space on organization.operational_responsibility (tenant_id, physical_space_id);

create table if not exists resource.asset_profile (
    id uuid primary key,
    tenant_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    organization_id uuid not null,
    agency_id uuid not null,
    resource_id uuid not null,
    physical_space_id uuid null,
    owner_actor_id uuid null,
    supplier_third_party_id uuid null,
    asset_class varchar(80) not null,
    criticality varchar(80) not null,
    lifecycle_phase varchar(80) not null,
    compliance_status varchar(80) not null,
    acquisition_cost numeric(19,2) not null,
    current_value numeric(19,2) not null,
    depreciation_method varchar(80) not null,
    acquisition_date timestamptz null,
    warranty_until timestamptz null,
    expected_renewal_date timestamptz null,
    last_compliance_check_at timestamptz null,
    next_compliance_check_at timestamptz null,
    maintenance_contract_reference varchar(255) null,
    notes text null
);
create unique index if not exists uk_asset_profile_resource on resource.asset_profile (tenant_id, resource_id);
create index if not exists idx_asset_profile_org on resource.asset_profile (tenant_id, organization_id);

create table if not exists inventory.generalized_inventory_campaign (
    id uuid primary key,
    tenant_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    organization_id uuid not null,
    agency_id uuid null,
    warehouse_id uuid null,
    physical_space_id uuid null,
    supervisor_actor_id uuid null,
    campaign_code varchar(120) not null,
    campaign_type varchar(80) not null,
    status varchar(80) not null,
    approval_required boolean not null,
    scope_type varchar(80) not null,
    scheduled_at timestamptz null,
    started_at timestamptz null,
    completed_at timestamptz null,
    variance_percent numeric(9,2) not null,
    notes text null
);
create unique index if not exists uk_generalized_inventory_campaign_code on inventory.generalized_inventory_campaign (tenant_id, organization_id, campaign_code);
create index if not exists idx_generalized_inventory_campaign_org on inventory.generalized_inventory_campaign (tenant_id, organization_id);

create table if not exists file.document_governance_policy (
    id uuid primary key,
    tenant_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    organization_id uuid not null,
    agency_id uuid null,
    target_type varchar(80) not null,
    document_category varchar(120) not null,
    mandatory boolean not null,
    approval_required boolean not null,
    expiry_days integer null,
    reviewer_responsibility_type varchar(80) null
);
create unique index if not exists uk_document_governance_policy_scope
    on file.document_governance_policy (tenant_id, organization_id, coalesce(agency_id, '00000000-0000-0000-0000-000000000000'::uuid), target_type, document_category);

create table if not exists file.document_review (
    id uuid primary key,
    tenant_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    organization_id uuid not null,
    document_link_id uuid not null,
    reviewer_user_id uuid not null,
    review_status varchar(80) not null,
    reviewed_at timestamptz not null,
    expires_at timestamptz null,
    notes text null
);
create index if not exists idx_document_review_org on file.document_review (tenant_id, organization_id);
create index if not exists idx_document_review_link on file.document_review (tenant_id, document_link_id, reviewed_at desc);
