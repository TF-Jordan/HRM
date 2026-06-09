-- V96: Add address, phone, and convention collective fields to organization for legal payroll documents.
-- Cameroon labour code Art. 68-69 requires employer address on payslips.

ALTER TABLE organization.organization ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE organization.organization ADD COLUMN IF NOT EXISTS city TEXT;
ALTER TABLE organization.organization ADD COLUMN IF NOT EXISTS postal_code TEXT;
ALTER TABLE organization.organization ADD COLUMN IF NOT EXISTS phone TEXT;
ALTER TABLE organization.organization ADD COLUMN IF NOT EXISTS convention_collective TEXT;
