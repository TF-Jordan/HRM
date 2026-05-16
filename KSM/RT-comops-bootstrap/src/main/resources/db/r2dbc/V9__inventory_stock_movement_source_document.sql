ALTER TABLE inventory.stock_movement
    ADD COLUMN IF NOT EXISTS source_document_type VARCHAR(64),
    ADD COLUMN IF NOT EXISTS source_document_number VARCHAR(128);
