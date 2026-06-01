CREATE TABLE IF NOT EXISTS product.category_i18n (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    category_id UUID NOT NULL REFERENCES product.product_category(id) ON DELETE CASCADE,
    locale VARCHAR(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    CONSTRAINT uk_category_i18n_locale UNIQUE (category_id, locale)
);

CREATE TABLE IF NOT EXISTS product.product_spec (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    product_id UUID NOT NULL REFERENCES product.product(id) ON DELETE CASCADE,
    weight_kg NUMERIC(19,3),
    length_cm NUMERIC(19,3),
    width_cm NUMERIC(19,3),
    height_cm NUMERIC(19,3),
    materials TEXT,
    CONSTRAINT uk_product_spec_product UNIQUE (product_id)
);

CREATE TABLE IF NOT EXISTS product.variant (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    product_id UUID NOT NULL REFERENCES product.product(id) ON DELETE CASCADE,
    sku VARCHAR(128) NOT NULL,
    barcode VARCHAR(128),
    label VARCHAR(255) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_variant_tenant_sku
    ON product.variant (tenant_id, lower(sku));

CREATE UNIQUE INDEX IF NOT EXISTS uk_variant_default_per_product
    ON product.variant (product_id)
    WHERE is_default = TRUE;

CREATE TABLE IF NOT EXISTS product.variant_attribute (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    variant_id UUID NOT NULL REFERENCES product.variant(id) ON DELETE CASCADE,
    attribute_name VARCHAR(128) NOT NULL,
    attribute_value VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS product.variant_price (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    variant_id UUID NOT NULL REFERENCES product.variant(id) ON DELETE CASCADE,
    price_type VARCHAR(64) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    effective_from TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_variant_price_lookup
    ON product.variant_price (tenant_id, variant_id, price_type, effective_from DESC);

CREATE TABLE IF NOT EXISTS product.batch (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    product_id UUID NOT NULL REFERENCES product.product(id) ON DELETE CASCADE,
    lot_number VARCHAR(128) NOT NULL,
    manufacturing_date DATE,
    expiry_date DATE,
    quantity INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_batch_lot UNIQUE (product_id, lot_number)
);

CREATE TABLE IF NOT EXISTS product.media_asset (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id UUID NOT NULL,
    file_id UUID NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    alt_text VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_media_asset_target
    ON product.media_asset (tenant_id, target_type, target_id, position);
