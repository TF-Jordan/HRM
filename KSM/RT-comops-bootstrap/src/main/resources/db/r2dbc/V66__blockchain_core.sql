-- Blockchain Core - signed transactions, proof-of-work blocks and anchoring

CREATE TABLE IF NOT EXISTS blockchain_wallet (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    label VARCHAR(160),
    public_key TEXT NOT NULL,
    fingerprint VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_blockchain_wallet_fingerprint
    ON blockchain_wallet (tenant_id, organization_id, fingerprint);

CREATE INDEX IF NOT EXISTS idx_blockchain_wallet_org
    ON blockchain_wallet (tenant_id, organization_id, active);

CREATE TABLE IF NOT EXISTS blockchain_block (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    chain_code VARCHAR(80) NOT NULL,
    height BIGINT NOT NULL,
    previous_hash VARCHAR(64) NOT NULL,
    merkle_root VARCHAR(64) NOT NULL,
    block_hash VARCHAR(64) NOT NULL,
    nonce BIGINT NOT NULL,
    difficulty INT NOT NULL,
    transaction_count INT NOT NULL,
    mined_by VARCHAR(160),
    mined_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_blockchain_block_height
    ON blockchain_block (tenant_id, organization_id, chain_code, height);

CREATE UNIQUE INDEX IF NOT EXISTS idx_blockchain_block_hash
    ON blockchain_block (tenant_id, organization_id, chain_code, block_hash);

CREATE INDEX IF NOT EXISTS idx_blockchain_block_latest
    ON blockchain_block (tenant_id, organization_id, chain_code, height DESC);

CREATE TABLE IF NOT EXISTS blockchain_transaction (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    chain_code VARCHAR(80) NOT NULL,
    transaction_type VARCHAR(80) NOT NULL,
    source_service VARCHAR(80),
    source_reference VARCHAR(160),
    payload TEXT,
    payload_hash VARCHAR(64) NOT NULL,
    sender_public_key TEXT NOT NULL,
    signature TEXT NOT NULL,
    transaction_hash VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    block_id UUID REFERENCES blockchain_block(id),
    block_height BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    mined_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_blockchain_transaction_hash
    ON blockchain_transaction (tenant_id, organization_id, chain_code, transaction_hash);

CREATE INDEX IF NOT EXISTS idx_blockchain_transaction_pending
    ON blockchain_transaction (tenant_id, organization_id, chain_code, status, created_at);

CREATE INDEX IF NOT EXISTS idx_blockchain_transaction_block
    ON blockchain_transaction (block_id, created_at);

CREATE INDEX IF NOT EXISTS idx_blockchain_transaction_source
    ON blockchain_transaction (tenant_id, organization_id, source_service, source_reference);
