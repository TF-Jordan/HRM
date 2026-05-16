ALTER TABLE kernel.client_application
  ADD COLUMN IF NOT EXISTS allowed_service_codes text[] NOT NULL DEFAULT ARRAY[]::text[];

UPDATE kernel.client_application
SET allowed_service_codes = ARRAY[
  'ORGANIZATION',
  'SETTINGS',
  'COMMERCIAL',
  'PRODUCT',
  'INVENTORY',
  'SALES',
  'ACCOUNTING',
  'TREASURY',
  'RESOURCE'
]
WHERE allowed_service_codes = ARRAY[]::text[]
   OR allowed_service_codes IS NULL;
