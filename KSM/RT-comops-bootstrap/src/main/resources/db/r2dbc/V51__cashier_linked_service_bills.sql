ALTER TABLE cashier.bill
  ADD COLUMN IF NOT EXISTS linked_service_code VARCHAR(64);

ALTER TABLE cashier.bill
  ADD COLUMN IF NOT EXISTS linked_document_type VARCHAR(64);

ALTER TABLE cashier.bill
  ADD COLUMN IF NOT EXISTS linked_document_id UUID;

ALTER TABLE cashier.bill
  ADD COLUMN IF NOT EXISTS linked_synced_amount NUMERIC(19, 2) NOT NULL DEFAULT 0;

UPDATE cashier.bill
SET linked_synced_amount = 0
WHERE linked_synced_amount IS NULL;

CREATE INDEX IF NOT EXISTS idx_cashier_bill_linked_document
  ON cashier.bill (linked_service_code, linked_document_type, linked_document_id);
