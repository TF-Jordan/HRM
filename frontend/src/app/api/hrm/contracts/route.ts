import "server-only";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import type { ContractResponse } from "@/server/ksm/modules/employees";

/* ── Helpers ─────────────────────────────────────────────────────────── */

function addDays(dateStr: string, days: number): Date {
  const d = new Date(dateStr);
  d.setDate(d.getDate() + days);
  return d;
}

function isInTrialPeriod(c: ContractResponse): boolean {
  if (!c.periodeEssai || c.status !== "ACTIVE") return false;
  return addDays(c.dateDebut, c.periodeEssai) > new Date();
}

function isExpiringIn90Days(c: ContractResponse): boolean {
  if (!c.dateFin || c.status !== "ACTIVE") return false;
  if (c.type !== "CDD" && c.type !== "STAGE" && c.type !== "INTERIM") return false;
  const now = new Date();
  const end = new Date(c.dateFin);
  const cutoff = new Date();
  cutoff.setDate(cutoff.getDate() + 90);
  return end >= now && end <= cutoff;
}

function trialEndsThisMonth(c: ContractResponse): boolean {
  if (!c.periodeEssai || c.status !== "ACTIVE") return false;
  const trialEnd = addDays(c.dateDebut, c.periodeEssai);
  const now = new Date();
  return (
    trialEnd >= now &&
    trialEnd.getMonth() === now.getMonth() &&
    trialEnd.getFullYear() === now.getFullYear()
  );
}

/* ── GET /api/hrm/contracts ──────────────────────────────────────────── */

export async function GET() {
  return requirePermissionRoute("hrm:contract:read", async (session) => {
    // 1. Fetch all employees of the organisation
    const employees = await employeesApi.listEmployees(session);

    // 2. Fetch each employee's contracts in parallel
    const contractsByEmployee = await Promise.all(
      employees.map(async (emp) => {
        try {
          const contracts = await employeesApi.listContracts(emp.id, session);
          return contracts.map((c) => ({
            ...c,
            employeeMatricule: emp.matricule,
            employeeDisplayName: emp.actorDisplayName ?? null,
          }));
        } catch {
          return [];
        }
      }),
    );

    const contracts = contractsByEmployee.flat();

    // 3. Compute aggregate stats
    const total = contracts.length;
    const active = contracts.filter((c) => c.status === "ACTIVE").length;
    const trialing = contracts.filter(isInTrialPeriod).length;
    const expiringIn90Days = contracts.filter(isExpiringIn90Days).length;
    const cdi = contracts.filter((c) => c.type === "CDI").length;
    const cdd = contracts.filter((c) => c.type === "CDD").length;
    const stage = contracts.filter((c) => c.type === "STAGE").length;
    const interim = contracts.filter((c) => c.type === "INTERIM").length;

    const trialAlerts = contracts.filter(trialEndsThisMonth).map((c) => ({
      employeeId: c.employeeId,
      employeeDisplayName: c.employeeDisplayName,
      trialEnd: addDays(c.dateDebut, c.periodeEssai!).toISOString().slice(0, 10),
    }));

    return Response.json({
      ok: true,
      data: {
        contracts,
        stats: {
          total,
          active,
          trialing,
          expiringIn90Days,
          cdi,
          cdd,
          stage,
          interim,
          totalEmployees: employees.length,
        },
        trialAlerts,
      },
    });
  });
}
