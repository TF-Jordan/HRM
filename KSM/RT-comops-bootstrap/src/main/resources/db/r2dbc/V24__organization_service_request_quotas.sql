ALTER TABLE organization.organization_service_subscription
  ADD COLUMN IF NOT EXISTS request_quota_limit BIGINT NOT NULL DEFAULT 10000;

ALTER TABLE organization.organization_service_subscription
  ADD COLUMN IF NOT EXISTS request_quota_window_seconds BIGINT NOT NULL DEFAULT 60;

UPDATE organization.organization_service_subscription
SET request_quota_limit = 10000
WHERE request_quota_limit IS NULL OR request_quota_limit <= 0;

UPDATE organization.organization_service_subscription
SET request_quota_window_seconds = 60
WHERE request_quota_window_seconds IS NULL OR request_quota_window_seconds <= 0;
