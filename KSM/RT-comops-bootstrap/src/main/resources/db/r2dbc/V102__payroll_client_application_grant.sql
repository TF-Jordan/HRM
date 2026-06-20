-- V102: grant the PAYROLL service to the BFF ClientApplication(s).
--
-- The ClientApplicationServiceEntitlementWebFilter requires the calling client application
-- (X-Client-Id / X-Api-Key) to be allowlisted for the resolved service, in addition to the
-- organization subscription. Now that /api/v1/payroll/** resolves to the PAYROLL service, any
-- client application already trusted with HRM must also be granted PAYROLL — otherwise every
-- payroll call returns 403 CLIENT_APPLICATION_SERVICE_NOT_ALLOWED.
--
-- Idempotent: only appends PAYROLL when HRM is present and PAYROLL is missing.

UPDATE kernel.client_application
   SET allowed_service_codes = allowed_service_codes || ARRAY['PAYROLL'],
       updated_at = now()
 WHERE allowed_service_codes @> ARRAY['HRM']
   AND NOT (allowed_service_codes @> ARRAY['PAYROLL']);
