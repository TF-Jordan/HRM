UPDATE kernel.client_application
SET allowed_service_codes = ARRAY(
  SELECT DISTINCT code
  FROM unnest(allowed_service_codes || ARRAY['BILLING']) AS code
)
WHERE allowed_service_codes @> ARRAY['ACCOUNTING']
  AND NOT allowed_service_codes @> ARRAY['BILLING'];

UPDATE kernel.client_application
SET allowed_service_codes = ARRAY(
  SELECT DISTINCT code
  FROM unnest(allowed_service_codes || ARRAY['BANKING']) AS code
)
WHERE allowed_service_codes @> ARRAY['TREASURY']
  AND NOT allowed_service_codes @> ARRAY['BANKING'];

UPDATE kernel.client_application
SET allowed_service_codes = ARRAY(
  SELECT DISTINCT code
  FROM unnest(allowed_service_codes || ARRAY['CASHIER']) AS code
)
WHERE allowed_service_codes @> ARRAY['TREASURY']
  AND NOT allowed_service_codes @> ARRAY['CASHIER'];

INSERT INTO organization.organization_service_subscription (
  id,
  tenant_id,
  created_at,
  updated_at,
  organization_id,
  service_code,
  request_quota_limit,
  request_quota_window_seconds
)
SELECT
  gen_random_uuid(),
  tenant_id,
  created_at,
  now(),
  organization_id,
  'BILLING',
  request_quota_limit,
  request_quota_window_seconds
FROM organization.organization_service_subscription source
WHERE source.service_code = 'ACCOUNTING'
  AND NOT EXISTS (
    SELECT 1
    FROM organization.organization_service_subscription target
    WHERE target.tenant_id = source.tenant_id
      AND target.organization_id = source.organization_id
      AND target.service_code = 'BILLING'
  );

INSERT INTO organization.organization_service_subscription (
  id,
  tenant_id,
  created_at,
  updated_at,
  organization_id,
  service_code,
  request_quota_limit,
  request_quota_window_seconds
)
SELECT
  gen_random_uuid(),
  tenant_id,
  created_at,
  now(),
  organization_id,
  'BANKING',
  request_quota_limit,
  request_quota_window_seconds
FROM organization.organization_service_subscription source
WHERE source.service_code = 'TREASURY'
  AND NOT EXISTS (
    SELECT 1
    FROM organization.organization_service_subscription target
    WHERE target.tenant_id = source.tenant_id
      AND target.organization_id = source.organization_id
      AND target.service_code = 'BANKING'
  );

INSERT INTO organization.organization_service_subscription (
  id,
  tenant_id,
  created_at,
  updated_at,
  organization_id,
  service_code,
  request_quota_limit,
  request_quota_window_seconds
)
SELECT
  gen_random_uuid(),
  tenant_id,
  created_at,
  now(),
  organization_id,
  'CASHIER',
  request_quota_limit,
  request_quota_window_seconds
FROM organization.organization_service_subscription source
WHERE source.service_code = 'TREASURY'
  AND NOT EXISTS (
    SELECT 1
    FROM organization.organization_service_subscription target
    WHERE target.tenant_id = source.tenant_id
      AND target.organization_id = source.organization_id
      AND target.service_code = 'CASHIER'
  );
