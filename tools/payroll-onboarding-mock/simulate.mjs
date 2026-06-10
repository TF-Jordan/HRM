#!/usr/bin/env node
// @ts-check
/**
 * Simulator: administration-core onboarding a standalone-payroll tenant.
 *
 * Walks through the 5-step flow documented in
 *   KSM/RT-comops-payroll-core/ONBOARDING_HOOKS.md
 * against a running KSM instance, printing a clear pass/fail report.
 *
 * Steps 1–3 (org creation, role provisioning, role assignment) live in
 * administration-core; this script *simulates* them by calling administration-core's
 * existing endpoints in dry-run mode (or skips them when the IDs already exist).
 * Steps 4–5 (data-source flip, CSV import) live in payroll-core and are exercised
 * for real.
 *
 * Usage:
 *   node tools/payroll-onboarding-mock/simulate.mjs \
 *     --base http://localhost:8080 \
 *     --email super.admin@hrcore.demo \
 *     --password 'Demo@2024!' \
 *     --org 00000000-0000-0000-0000-0000000a0002
 *
 * Env vars override defaults: KSM_BASE, KSM_EMAIL, KSM_PASSWORD,
 * KSM_CLIENT_ID, KSM_API_KEY, KSM_TOKEN (bypass login), KSM_ORG_ID.
 */

import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import path from "node:path";

const args = parseArgs(process.argv.slice(2));
const cfg = {
  base: args.base ?? process.env.KSM_BASE ?? "http://localhost:8080",
  email: args.email ?? process.env.KSM_EMAIL ?? "super.admin@hrcore.demo",
  password: args.password ?? process.env.KSM_PASSWORD ?? "Demo@2024!",
  clientId: process.env.KSM_CLIENT_ID ?? "hrm-frontend",
  apiKey: process.env.KSM_API_KEY ?? "hrm-bff-dev-secret-2024",
  token: process.env.KSM_TOKEN ?? null,
  organizationId: args.org ?? process.env.KSM_ORG_ID ?? null,
};

const SCRIPT_DIR = path.dirname(fileURLToPath(import.meta.url));
const SAMPLE_CSV_PATH = path.join(SCRIPT_DIR, "sample-employees.csv");

// ---------- pretty output ----------
const C = {
  reset: "\x1b[0m", dim: "\x1b[2m", bold: "\x1b[1m",
  green: "\x1b[32m", red: "\x1b[31m", yellow: "\x1b[33m",
  cyan: "\x1b[36m", magenta: "\x1b[35m",
};
const isTTY = process.stdout.isTTY;
const color = (c, s) => (isTTY ? `${C[c]}${s}${C.reset}` : s);
const ok = (s) => console.log(color("green", "  ✓ ") + s);
const ko = (s) => console.log(color("red", "  ✗ ") + s);
const info = (s) => console.log(color("dim", "    " + s));
const step = (n, title) =>
  console.log("\n" + color("bold", color("cyan", `── Step ${n} ── `)) + color("bold", title));

let session = null; // { accessToken, tenantId, organizationId }

// ---------- entrypoint ----------
try {
  banner();
  await login();
  await fetchManifest();
  await simulateAdministrationCoreSteps();
  await flipDataSource();
  await importInitialCsv();
  await verifyOutcome();
  finalReport(0);
} catch (err) {
  console.log("\n" + color("red", color("bold", "Simulation failed: ")) + (err?.message ?? err));
  if (err?.stack && args.verbose) console.log(color("dim", err.stack));
  finalReport(1);
}

// ============================================================================
function banner() {
  console.log(color("bold", color("magenta", "\n  Payroll onboarding · administration-core simulator")));
  info(`base = ${cfg.base}`);
  info(`email = ${cfg.email}`);
}

// ---------------------------------------------------------------- login
async function login() {
  step(0, "Authenticate against KSM (acquire JWT)");
  if (cfg.token) {
    session = { accessToken: cfg.token, tenantId: null, organizationId: cfg.organizationId };
    ok("using KSM_TOKEN from environment");
    return;
  }
  const discover = await ksm("POST", "/api/auth/discover-contexts", {
    body: { principal: cfg.email, password: cfg.password },
    auth: false,
  });
  const contexts = discover.contexts ?? [];
  if (contexts.length === 0) throw new Error("no accessible context for this user");
  const ctx = contexts[0];
  const orgId = cfg.organizationId ?? ctx.organizations?.[0]?.organizationId ?? null;
  const contextual = await ksm("POST", "/api/auth/select-context", {
    body: {
      selectionToken: discover.selectionToken,
      contextId: ctx.contextId,
      organizationId: orgId,
    },
    auth: false,
  });
  session = {
    accessToken: contextual.session.accessToken,
    tenantId: contextual.selectedTenantId,
    organizationId: contextual.selectedOrganizationId ?? orgId,
  };
  ok(`logged in as ${cfg.email}`);
  info(`tenantId = ${session.tenantId}`);
  info(`organizationId = ${session.organizationId}`);
}

// ----------------------------------------------- step 0: fetch manifest
async function fetchManifest() {
  step("M", "Discover payroll-core capabilities");
  const m = await ksm("GET", "/api/v1/payroll/onboarding/manifest");
  if (m.module !== "payroll") throw new Error(`unexpected module ${m.module}`);
  ok(`manifest v${m.version} · module ${m.module}`);
  info(`permissions: ${m.permissions.map((p) => p.code).join(", ")}`);
  info(`role templates: ${m.suggestedRoleTemplates.map((r) => r.code).join(", ")}`);
  info(`data source modes: ${m.dataSourceModes.map((d) => d.code).join(", ")}`);
  global.MANIFEST = m;
}

// --------------------------- steps 1-3: administration-core (simulated)
async function simulateAdministrationCoreSteps() {
  step(1, "Create tenant + organization (administration-core)");
  if (!session.organizationId) throw new Error("no organizationId — pass --org or set KSM_ORG_ID");
  ok(`org ${session.organizationId} already provisioned (skipping creation)`);
  info("administration-core would call POST /api/v1/administration/governance/organizations/{id}");

  step(2, "Provision suggested role templates (administration-core)");
  for (const template of global.MANIFEST.suggestedRoleTemplates) {
    const payload = {
      code: template.code,
      name: template.name,
      scopeType: template.scopeType,
      permissions: template.permissions,
    };
    const existing = await tryGet(`/api/v1/administration/roles?code=${template.code}`);
    if (existing) {
      ok(`role ${template.code} already exists — would patch its permissions in place`);
    } else {
      // Dry-run: we don't actually create the role to keep the simulator non-destructive.
      // The point is to show administration-core the exact payload payroll-core advertises.
      ok(`would POST /api/v1/administration/roles with the manifest payload`);
    }
    info(`payload: ${JSON.stringify(payload)}`);
  }

  step(3, "Assign PAYROLL_ADMIN to the first user (administration-core)");
  ok("administration-core would call POST /api/v1/administration/users/{userId}/roles");
  info("(skipped — outside payroll-core's perimeter; pick the user during your onboarding)");
}

// ------------------------------------------- step 4: data-source flip
async function flipDataSource() {
  step(4, "Flip the organization payroll source to LOCAL (payroll-core)");
  const before = await ksm(
    "GET",
    `/api/v1/payroll/employees/data-source?organizationId=${session.organizationId}`,
  );
  info(`before: source=${before.source}`);
  const after = await ksm(
    "PUT",
    `/api/v1/payroll/employees/data-source?organizationId=${session.organizationId}`,
    { body: { source: "LOCAL" } },
  );
  if (after.source !== "LOCAL") throw new Error(`flip failed, source=${after.source}`);
  ok(`data-source = LOCAL`);
}

// ------------------------------------------- step 5: initial CSV import
async function importInitialCsv() {
  step(5, "Import the initial employee CSV (payroll-core)");
  const csv = readFileSync(SAMPLE_CSV_PATH, "utf8");
  const lines = csv.split("\n").filter(Boolean).length - 1;
  info(`uploading ${SAMPLE_CSV_PATH} (${lines} employees)`);
  const report = await ksm(
    "POST",
    `/api/v1/payroll/employees/import?organizationId=${session.organizationId}`,
    { body: { csv } },
  );
  ok(`imported · total=${report.total} created=${report.created} updated=${report.updated} errors=${report.errors.length}`);
  for (const e of report.errors) {
    ko(`L${e.line} ${e.matricule || "?"} — ${e.message}`);
  }
  if (report.errors.length > 0) {
    throw new Error(`${report.errors.length} CSV row(s) failed to import`);
  }
}

// ---------------------------------------------- verify final state
async function verifyOutcome() {
  step("V", "Verify the organization is now driven by the local table");
  const ds = await ksm(
    "GET",
    `/api/v1/payroll/employees/data-source?organizationId=${session.organizationId}`,
  );
  if (ds.source !== "LOCAL") throw new Error(`data-source = ${ds.source} (expected LOCAL)`);
  ok(`data-source = LOCAL`);
  const list = await ksm(
    "GET",
    `/api/v1/payroll/employees?organizationId=${session.organizationId}`,
  );
  ok(`${list.length} payroll employee(s) visible to the engine`);
  for (const e of list.slice(0, 5)) {
    info(`· ${e.matricule.padEnd(10)} ${e.displayName.padEnd(28)} ${String(e.baseSalary).padStart(10)} ${e.paymentChannel}`);
  }
  if (list.length > 5) info(`… ${list.length - 5} more`);
}

// ------------------------------------------------------------------ end
function finalReport(code) {
  console.log("");
  if (code === 0) {
    console.log(color("green", color("bold", "  ✓ Onboarding simulation completed successfully")));
  } else {
    console.log(color("red", color("bold", "  ✗ Onboarding simulation FAILED")));
  }
  console.log("");
  process.exit(code);
}

// ============================================================================
// HTTP helpers
// ============================================================================
async function ksm(method, pathAndQuery, { body, auth = true } = {}) {
  const url = cfg.base + pathAndQuery;
  /** @type {Record<string,string>} */
  const headers = {
    "Content-Type": "application/json",
    Accept: "application/json",
    "X-Client-Id": cfg.clientId,
    "X-Api-Key": cfg.apiKey,
  };
  if (auth && session) {
    headers.Authorization = `Bearer ${session.accessToken}`;
    if (session.tenantId) headers["X-Tenant-Id"] = session.tenantId;
    if (session.organizationId) headers["X-Organization-Id"] = session.organizationId;
  }
  const res = await fetch(url, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let json;
  try { json = text ? JSON.parse(text) : null; } catch { json = null; }
  if (!res.ok) {
    const msg = json?.message || json?.error || text || `HTTP ${res.status}`;
    throw new Error(`${method} ${pathAndQuery} → ${res.status} · ${msg}`);
  }
  // KSM wraps responses in { status, success, message, data, ... }.
  if (json && Object.prototype.hasOwnProperty.call(json, "data")) return json.data;
  return json;
}

async function tryGet(pathAndQuery) {
  try { return await ksm("GET", pathAndQuery); } catch { return null; }
}

function parseArgs(argv) {
  /** @type {Record<string,string|boolean>} */
  const out = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === "--verbose") { out.verbose = true; continue; }
    if (a.startsWith("--")) {
      const key = a.slice(2);
      const next = argv[i + 1];
      if (next && !next.startsWith("--")) { out[key] = next; i++; }
      else { out[key] = true; }
    }
  }
  return out;
}
