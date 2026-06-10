# Payroll-core onboarding hooks for administration-core

This module is read-only consumed by administration-core during tenant onboarding.
There is no onboarding logic in payroll-core — only the hooks needed to provision
a tenant for **standalone payroll** (payroll without the HR module).

## 1. Discover what payroll exposes

```
GET /api/v1/payroll/onboarding/manifest
```

Returns a self-describing manifest (version-stamped) with:

- `permissions` — every `hrm:payroll:*` permission code with the label
  administration-core should display in its permission catalog;
- `suggestedRoleTemplates` — `PAYROLL_ADMIN` and `PAYROLL_EMPLOYEE` (their full
  permission set, scope and description). These templates carry **no HRM
  permission**, so a tenant subscribing to payroll alone never gets menus
  pointing at endpoints that aren't there;
- `dataSourceModes` — `HRM` (default) vs `LOCAL` (standalone);
- `onboardingFlow` — the ordered list of endpoints to call.

The manifest is a pure read; any authenticated caller can fetch it.

## 2. Provision a standalone-payroll tenant

The flow happens entirely through administration-core's existing APIs except for
the data-source flip, which is a payroll-core endpoint.

| Step | Owner | Endpoint |
|---|---|---|
| 1. Create tenant + organization | administration-core | `POST /api/v1/administration/governance/organizations/{organizationId}` |
| 2. Provision the role templates (`PAYROLL_ADMIN`, `PAYROLL_EMPLOYEE`) listed in our manifest | administration-core | `POST /api/v1/administration/roles` (per template) |
| 3. Assign `PAYROLL_ADMIN` to the first user | administration-core | `POST /api/v1/administration/users/{userId}/roles` |
| 4. Flip the organization payroll source to LOCAL | payroll-core | `PUT /api/v1/payroll/employees/data-source?organizationId={id}` body `{"source":"LOCAL"}` |
| 5. (Optional) Import the initial employee CSV | payroll-core | `POST /api/v1/payroll/employees/import?organizationId={id}` body `{"csv":"..."}` |

Step 4 is idempotent: re-running it never duplicates a row (one row per
organization, upserted in `payroll_data_source`). Step 5 is also idempotent at
the row level (upsert by matricule).

## 3. Permission codes

| Code | Label | Use |
|---|---|---|
| `hrm:payroll:read` | Read payroll | List cycles, entries, payslips, declarations, KPIs |
| `hrm:payroll:run` | Run payroll | Calculate / recalculate, manage employees, variables, tax brackets, lookup tables |
| `hrm:payroll:validate` | Validate payroll | Validate / reject / approve a cycle, initiate payment, close |

The `hrm:` prefix is preserved for backward compatibility with frontends already
in the wild — the codes are not bound to hrm-core in any way.

## 4. Contract stability

- The manifest is **versioned** (`version: "1"` today). Breaking changes — removing
  a permission, renaming a role template — will bump the version and ship in the
  same release as a migration of administration-core's onboarding template, so
  the two stay in lockstep.
- New optional fields can be added under the same version (additive change).
- The `onboardingFlow` field is informational; the actual contract is the
  permission set + the data-source flip endpoint.
