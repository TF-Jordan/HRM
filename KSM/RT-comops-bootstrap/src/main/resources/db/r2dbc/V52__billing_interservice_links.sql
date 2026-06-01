ALTER TABLE billing.commercial_document
  ADD COLUMN IF NOT EXISTS linked_accounting_invoice_id UUID;

ALTER TABLE billing.commercial_document
  ADD COLUMN IF NOT EXISTS linked_cashier_bill_id UUID;

CREATE INDEX IF NOT EXISTS idx_billing_document_linked_accounting_invoice
  ON billing.commercial_document (linked_accounting_invoice_id);

CREATE INDEX IF NOT EXISTS idx_billing_document_linked_cashier_bill
  ON billing.commercial_document (linked_cashier_bill_id);

ALTER TABLE billing.payment
  ADD COLUMN IF NOT EXISTS billing_document_id UUID;

ALTER TABLE billing.payment
  ADD COLUMN IF NOT EXISTS linked_service_code VARCHAR(64);

ALTER TABLE billing.payment
  ADD COLUMN IF NOT EXISTS linked_document_type VARCHAR(64);

ALTER TABLE billing.payment
  ADD COLUMN IF NOT EXISTS linked_document_id UUID;

CREATE INDEX IF NOT EXISTS idx_billing_payment_document
  ON billing.payment (billing_document_id);

CREATE INDEX IF NOT EXISTS idx_billing_payment_linked_document
  ON billing.payment (linked_service_code, linked_document_type, linked_document_id);
