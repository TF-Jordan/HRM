# Flows 11-15 Postman Pack

Collection file:
- `iwm-backend/docs/postman/flows-14-15-api-first.postman_collection.json`
- `iwm-backend/docs/postman/auth-core-full-iam.postman_collection.json`

What this pack is based on:
- implemented AuthCore/YowAuth0 endpoints in `RT-comops-auth-core`
- implemented controllers and request DTOs in `RT-comops-cashier-core`
- implemented controllers and request DTOs in `RT-comops-billing-core`
- implemented controllers and request DTOs in `RT-comops-accounting-core`
- implemented controllers and request DTOs in `RT-comops-treasury-core`
- implemented controllers and request DTOs in `RT-comops-administration-core`
- real authentication and tenant-context filters in `RT-comops-kernel-core`
- integration-tested request sequences in `RT-comops-bootstrap`

Required collection variables:
- `baseUrl`
- `managementBaseUrl`
- `clientId`
- `apiKey`
- `managementApiKey`
- `tenantId`
- `adminUsername`
- `adminPassword`
- `operatorUsername`
- `operatorPassword`
- `candidateUsername`
- `candidatePassword`
- `customerThirdPartyId`
- `productId`
- `agencyId`
- `cashierEmail`
- `cashierCustomerId`

AuthCore collection variables:
- `authCoreUsername`
- `authCoreEmail`
- `authCorePhone`
- `authCorePassword`
- `oidcClientId`
- `oidcClientSecret`
- `oidcServiceCode`

AuthCore flow order:
- `00` OIDC discovery metadata
- `01-02` captcha issue and verification
- `03` strong sign-up with `BUSINESS` + `FREELANCE` onboarding payload
- `04-05` phone verification by SMS OTP
- `06-07` generic WhatsApp OTP issue and verification
- `08-09` MFA enable and confirmation
- `10-11` login by phone with MFA challenge and confirmation
- `12` identity onboarding update to `ORGANIZATION` completed
- `13-16` OIDC userinfo, token exchange, userinfo with exchanged token, introspection

Expected user capabilities:
- `adminUsername`:
  - administration governance and settings permissions
- `operatorUsername`:
  - `organizations:write`
  - `treasury:manage`
  - `accounting:write`
- `candidateUsername`:
  - authenticated user context, enough to call `/api/actors/onboarding`

Important runtime notes from the code:
- every `/api/**` request is authenticated
- working login requests require:
  - `X-Client-Id`
  - `X-Api-Key`
  - `X-Tenant-Id`
- permissioned business routes also need:
  - `Authorization: Bearer <token from /api/auth/login>`
- treasury routes under `/api/treasury/**` are organization-service scoped in the route resolver:
  - include `X-Organization-Id`
- the same organization context is used in practice for cashier, billing, and accounting business routes:
  - include `X-Organization-Id`
- management routes use:
  - `X-Management-Api-Key`

Intentional limits of the pack:
- cashier flows assume an existing `agencyId` and `cashierCustomerId`
- billing and accounting flows assume an existing `customerThirdPartyId` and `productId`
- the collection now includes invoice creation and posting, so `14.09` can reuse the `invoiceId` produced by `13.01` then `13.03`
- the pack does not try to bootstrap users, roles, or client applications from scratch
- it assumes the users already exist with the required permissions
