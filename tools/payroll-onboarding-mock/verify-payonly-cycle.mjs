// Vérifie qu'un caller avec UNIQUEMENT les 3 permissions paie peut piloter tout le cycle
// dans le workspace payroll-manager — pas de HR admin requis.

const BASE = "http://localhost:8080";
const ORG = "00000000-0000-0000-0000-0000000a0002";
const CID = "hrm-frontend";
const KEY = "hrm-bff-dev-secret-2024";

function H(token, tenant) {
  return {
    "Content-Type": "application/json",
    Accept: "application/json",
    "X-Client-Id": CID, "X-Api-Key": KEY,
    Authorization: `Bearer ${token}`,
    "X-Tenant-Id": tenant,
    "X-Organization-Id": ORG,
  };
}

async function login(email, password) {
  const d = await fetch(`${BASE}/api/auth/discover-contexts`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-Client-Id": CID, "X-Api-Key": KEY },
    body: JSON.stringify({ principal: email, password }),
  }).then((r) => r.json());
  const sel = await fetch(`${BASE}/api/auth/select-context`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-Client-Id": CID, "X-Api-Key": KEY },
    body: JSON.stringify({ selectionToken: d.data.selectionToken, contextId: d.data.contexts[0].contextId, organizationId: ORG }),
  }).then((r) => r.json());
  return { token: sel.data.session.accessToken, tenant: sel.data.selectedTenantId, authorities: sel.data.session.authorities };
}

const PAY_ONLY = ["hrm:payroll:read", "hrm:payroll:run", "hrm:payroll:validate"];
// Each invocation runs against a fresh period so the script is idempotent:
// once a cycle is past VALIDATED, it can no longer be recalculated, so a new month
// is needed every run unless the caller supplies an explicit one via $PERIOD.
const PERIOD = process.env.PERIOD ?? `2027-${String(Math.floor(Math.random() * 12) + 1).padStart(2, "0")}`;
console.log("test period =", PERIOD);

// 1) Login as super.admin so we have rights to scaffold a payroll-only user.
const sa = await login("super.admin@hrcore.demo", "Demo@2024!");
console.log("super.admin authorities:", sa.authorities.length, "of which payroll:",
  sa.authorities.filter(a => a.startsWith("hrm:payroll:")).slice(0, 5).join(", "));

// 2) Boot the org in payonly mode (idempotent).
await fetch(`${BASE}/api/v1/payroll/employees/data-source?organizationId=${ORG}`, {
  method: "PUT", headers: H(sa.token, sa.tenant), body: JSON.stringify({ source: "LOCAL" }),
}).then(r => r.json());

// 3) Inject a payroll-only role via SQL (faster than going through admin-core for this verification).
const { execSync } = await import("node:child_process");
const sql = `
DO $$
DECLARE
  v_tenant uuid := '00000000-0000-0000-0000-0000000a0001';
  v_org    uuid := '${ORG}';
  v_role_id uuid;
  v_actor_id uuid := gen_random_uuid();
  v_user_id  uuid := gen_random_uuid();
BEGIN
  -- payroll-only role with EXACTLY the 3 manifest permissions
  INSERT INTO roles_core.role (id, tenant_id, created_at, updated_at, code, name, permissions, scope_type)
  VALUES (gen_random_uuid(), v_tenant, now(), now(), 'PAYROLL_ADMIN_VERIFY',
          'Payroll Admin (verify)', ARRAY['hrm:payroll:read','hrm:payroll:run','hrm:payroll:validate'],
          'ORGANIZATION')
  ON CONFLICT (tenant_id, code) DO UPDATE SET permissions = EXCLUDED.permissions
  RETURNING id INTO v_role_id;

  -- actor + user account for the verify call
  INSERT INTO actor.actor (id, tenant_id, created_at, updated_at, first_name, last_name, email, phone_number, gender, nationality)
  VALUES (v_actor_id, v_tenant, now(), now(), 'Payroll', 'Verify', 'payroll.verify@hrcore.demo',
          '+237699009999', 'OTHER', 'CMR')
  ON CONFLICT (tenant_id, email) DO UPDATE SET updated_at = now() RETURNING id INTO v_actor_id;

  -- bcrypt of "Demo@2024!" reused from V68 seed
  INSERT INTO auth_core.user_account (id, tenant_id, created_at, updated_at, actor_id, username, email,
                                       auth_provider, status, password_hash, force_password_change)
  VALUES (v_user_id, v_tenant, now(), now(), v_actor_id, 'payroll.verify', 'payroll.verify@hrcore.demo',
          'LOCAL', 'ACTIVE',
          '$2a$10$lIanxErhV404PPf83q5awu6zy7zabn978JF7BjnOV4dk8mUz/wdGy', false)
  ON CONFLICT (tenant_id, username) DO UPDATE SET updated_at = now() RETURNING id INTO v_user_id;

  INSERT INTO roles_core.user_role_assignment (id, tenant_id, created_at, updated_at, user_id, role_id, scope, scope_type, scope_id)
  VALUES (gen_random_uuid(), v_tenant, now(), now(), v_user_id, v_role_id,
          'ORGANIZATION:' || v_org, 'ORGANIZATION', v_org)
  ON CONFLICT (tenant_id, user_id, role_id, scope) DO NOTHING;
END $$;
`;
execSync(`psql postgresql://iwm:iwm@localhost:5432/iwm -v ON_ERROR_STOP=1`, { input: sql });
console.log("payroll-only user provisioned");

// 4) Login as the payroll-only user.
const po = await login("payroll.verify@hrcore.demo", "Demo@2024!");
const ownedPay = po.authorities.filter(a => a.startsWith("hrm:")).sort();
console.log("payroll-only authorities:", ownedPay.join(", "));
// note: KSM scopes permissions with "#ORGANIZATION:<uuid>" suffixes; strip before comparing
const stripped = po.authorities.map(p => p.split("#")[0]);
const ok = PAY_ONLY.every(p => stripped.includes(p))
        && !stripped.some(p => p.startsWith("hrm:") && !p.startsWith("hrm:payroll:"));
console.log("scope check (only hrm:payroll:* present):", ok ? "PASS" : "FAIL");

// 5) Walk the full cycle with that JWT.
async function go(method, path, body) {
  const r = await fetch(BASE + path, { method, headers: H(po.token, po.tenant), body: body && JSON.stringify(body) });
  const j = await r.json().catch(() => null);
  return { status: r.status, body: j };
}
const tests = [];
let runId = null;

let r = await go("POST", "/api/v1/payroll/runs", { period: PERIOD, runType: "REGULAR" });
tests.push(["1. POST /runs (calculate)", r.status, r.body?.data?.status]);
runId = r.body?.data?.id;

r = await go("GET", `/api/v1/payroll/runs/${runId}/entries`);
tests.push(["2. GET /runs/{id}/entries", r.status, `${r.body?.data?.length || 0} entries`]);

r = await go("PUT", `/api/v1/payroll/runs/${runId}/reject`, { reason: "test rejet, ajustez la prime fonction" });
tests.push(["3. PUT /reject (reject with reason)", r.status, r.body?.data?.status]);

r = await go("POST", "/api/v1/payroll/runs", { period: PERIOD, runType: "REGULAR" });
tests.push(["4. POST /runs (recalculate after reject)", r.status, r.body?.data?.status]);

r = await go("PUT", `/api/v1/payroll/runs/${runId}/validate`);
tests.push(["5. PUT /validate", r.status, r.body?.data?.status]);

r = await go("PUT", `/api/v1/payroll/runs/${runId}/approve`);
tests.push(["6. PUT /approve", r.status, r.body?.data?.status]);

r = await go("PUT", `/api/v1/payroll/runs/${runId}/initiate-payment`);
tests.push(["7. PUT /initiate-payment", r.status, r.body?.data?.status]);

const entries = (await go("GET", `/api/v1/payroll/runs/${runId}/entries`)).body?.data ?? [];
r = await go("POST", `/api/v1/payroll/documents/payslip?entryId=${entries[0].id}`);
tests.push(["8. POST /documents/payslip (sign PDF)", r.status, r.body?.data?.fileName]);

// Print verdict
console.log("\n=== Cycle walked by a payroll-only caller ===");
for (const [name, status, info] of tests) {
  const okRow = status >= 200 && status < 300;
  console.log(`${okRow ? "✓" : "✗"} ${name.padEnd(48)} HTTP ${status}  ${info ?? ""}`);
}

// Negative test: a HR-only endpoint MUST 403 for this caller
r = await go("GET", "/api/v1/hrm/employees?organizationId=" + ORG);
console.log(`\nNegative — GET /hrm/employees   HTTP ${r.status}  ${r.status === 403 ? "(correctly forbidden)" : "(unexpected!)"}`);
