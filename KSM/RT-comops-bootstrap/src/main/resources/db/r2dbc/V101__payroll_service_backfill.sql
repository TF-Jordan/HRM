-- V101: Payroll becomes a first-class subscribable service.
--
-- /api/v1/payroll/** is now gated by the PAYROLL service subscription (route resolver +
-- PlatformServiceCode.PAYROLL). The legacy HRM service used to bundle payroll, so every
-- organization currently subscribed to HRM must keep payroll access: back-fill a PAYROLL
-- subscription for them. Standalone-payroll orgs already subscribe to PAYROLL (V100).
--
-- Idempotent: one PAYROLL row per org, skipped when it already exists.

INSERT INTO organization.organization_service_subscription (
    id, tenant_id, created_at, updated_at, organization_id, service_code
)
SELECT gen_random_uuid(), hrm.tenant_id, now(), now(), hrm.organization_id, 'PAYROLL'
  FROM organization.organization_service_subscription hrm
 WHERE hrm.service_code = 'HRM'
   AND NOT EXISTS (
        SELECT 1
          FROM organization.organization_service_subscription pay
         WHERE pay.tenant_id = hrm.tenant_id
           AND pay.organization_id = hrm.organization_id
           AND pay.service_code = 'PAYROLL')
ON CONFLICT (tenant_id, organization_id, service_code) DO NOTHING;
