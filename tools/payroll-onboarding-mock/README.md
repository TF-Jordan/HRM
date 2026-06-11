# Payroll onboarding mock-server

A tiny Node.js simulator that walks through the **standalone-payroll** onboarding
flow against a running KSM instance, exactly as `administration-core` should do
in production.

Useful for:

- **administration-core integration**: reproduces the contract documented in
  [`KSM/RT-comops-payroll-core/ONBOARDING_HOOKS.md`](../../KSM/RT-comops-payroll-core/ONBOARDING_HOOKS.md);
- **payroll-core regression**: verifies the manifest, the data-source flip and
  the CSV import work end-to-end without spinning up the frontend.

No dependencies — Node ≥ 20 (the global `fetch` is enough).

## Run it

```sh
# 1. Start KSM + Postgres locally (your usual setup).
# 2. From the repo root:

node tools/payroll-onboarding-mock/simulate.mjs \
  --base    http://localhost:8080 \
  --email   super.admin@hrcore.demo \
  --password 'Demo@2024!' \
  --org     00000000-0000-0000-0000-0000000a0002
```

Environment variables also work and override defaults:

| Var | Default | Purpose |
|---|---|---|
| `KSM_BASE` | `http://localhost:8080` | KSM base URL |
| `KSM_EMAIL` | `super.admin@hrcore.demo` | Login email |
| `KSM_PASSWORD` | `Demo@2024!` | Login password |
| `KSM_CLIENT_ID` | `hrm-frontend` | Header `X-Client-Id` |
| `KSM_API_KEY` | `hrm-bff-dev-secret-2024` | Header `X-Api-Key` |
| `KSM_TOKEN` | _(unset)_ | Bypass login by supplying a JWT directly |
| `KSM_ORG_ID` | _(unset)_ | Target organization for the onboarding |

Pass `--verbose` for stack traces on failure.

## What it does

```
── Step 0 ── Authenticate against KSM (acquire JWT)
── Step M ── Discover payroll-core capabilities          (GET /onboarding/manifest)
── Step 1 ── Create tenant + organization                 (administration-core — simulated)
── Step 2 ── Provision suggested role templates           (administration-core — simulated)
── Step 3 ── Assign PAYROLL_ADMIN to the first user       (administration-core — skipped)
── Step 4 ── Flip the organization payroll source         (PUT /employees/data-source)
── Step 5 ── Import the initial employee CSV              (POST /employees/import)
── Step V ── Verify the organization is now driven by the local table
```

Steps 1–3 are administration-core's territory; the simulator prints the
exact payload it would send so the contract is unambiguous, but does not
mutate role state (non-destructive). Steps 4–5 are real calls and do
modify the targeted organization — point the script at a sandbox org.

## Sample data

[`sample-employees.csv`](./sample-employees.csv) ships 5 fictitious employees
covering every payment channel (BANK_TRANSFER, MTN_MOBILE_MONEY, ORANGE_MONEY)
and a mix of marital statuses / dependents so the proration + family-quotient
paths get exercised by the next payroll run.

## Sister script — `verify-payonly-cycle.mjs`

Companion verifier that confirms a caller carrying **only** the three payroll
permissions (`hrm:payroll:read|run|validate`) can drive the **entire** payroll
cycle from a single workspace, with no HR admin involved.

It seeds a temporary `PAYROLL_ADMIN_VERIFY` role + user via SQL (since
administration-core isn't always wired up locally), logs in as that user, then
walks calculate → reject → recalculate → validate → approve → initiate
payment → sign PDF. Also asserts a `403` on `/hrm/employees` to prove the
caller is fenced off from the HR perimeter.

```sh
PGPASSWORD=iwm node tools/payroll-onboarding-mock/verify-payonly-cycle.mjs
# optional: pin the period (otherwise picks a random month in 2027 each run)
PERIOD=2027-04 node tools/payroll-onboarding-mock/verify-payonly-cycle.mjs
```

Expected output ends with eight `✓` lines and `(correctly forbidden)` on the
negative test. Anything else means the standalone-mode contract regressed.

## Exit codes

- `0` — every step succeeded; the org now reads its employees from the
  payroll-local table.
- `1` — one step failed; the failing step + KSM error message are printed
  before exiting.
