import "server-only";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as payrollApi from "@/server/ksm/modules/payroll";
import { hasPermission } from "@/server/permissions";

/** Default jurisdiction for the configuration health widgets (CM is seeded). */
const DEFAULT_COUNTRY = "CM";

const num = (v: number | string | null | undefined): number => {
  const n = Number(v ?? 0);
  return Number.isFinite(n) ? n : 0;
};

/** Aggregated, server-computed snapshot for the payroll manager dashboard. */
export async function GET() {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const orgId = session.workspace?.organizationId;
    if (!orgId) {
      return fail(400, "NO_ORGANIZATION", "No organization in the current workspace.");
    }

    const safe = async <T>(p: Promise<T>, fb: T): Promise<T> => p.catch(() => fb);
    const canRun = hasPermission(session, "hrm:payroll:run");

    const [runs, employees, garnishments, settlements, retro, payElements, taxScales, lookupTables] =
      await Promise.all([
        safe(payrollApi.listPayrollRuns(session, orgId), [] as payrollApi.PayrollRunResponse[]),
        safe(
          employeesApi.listEmployees(session, { organizationId: orgId }),
          [] as employeesApi.EmployeeResponse[],
        ),
        safe(
          payrollApi.listGarnishments(session, { organizationId: orgId }),
          [] as payrollApi.GarnishmentResponse[],
        ),
        safe(
          payrollApi.listFinalSettlements(session, { organizationId: orgId }),
          [] as payrollApi.FinalSettlementResponse[],
        ),
        safe(
          payrollApi.listRetroactiveAdjustments(session, { organizationId: orgId }),
          [] as payrollApi.RetroactiveResponse[],
        ),
        canRun
          ? safe(payrollApi.listPayElements(session, DEFAULT_COUNTRY), [] as payrollApi.PayElementResponse[])
          : Promise.resolve([] as payrollApi.PayElementResponse[]),
        canRun
          ? safe(payrollApi.listTaxBracketTables(session, DEFAULT_COUNTRY), [] as payrollApi.TaxBracketTableResponse[])
          : Promise.resolve([] as payrollApi.TaxBracketTableResponse[]),
        canRun
          ? safe(payrollApi.listLookupTables(session, DEFAULT_COUNTRY), [] as payrollApi.LookupTableResponse[])
          : Promise.resolve([] as payrollApi.LookupTableResponse[]),
      ]);

    // Runs newest → oldest by period (YYYY-MM lexicographic ordering is chronological).
    const sortedDesc = runs
      .slice()
      .sort((a, b) => (a.periode < b.periode ? 1 : a.periode > b.periode ? -1 : 0));
    const current = sortedDesc[0] ?? null;
    const previous = current
      ? (sortedDesc.find((r) => r.periode < current.periode) ?? null)
      : null;
    const currency = current?.currency ?? "XAF";

    const employerCost = (r: payrollApi.PayrollRunResponse) =>
      num(r.totalGross) + num(r.totalEmployerCharges);

    const pctDelta = (cur: number, prev: number): number | null => {
      if (!prev) return null;
      return Math.round(((cur - prev) / prev) * 1000) / 10;
    };

    const kpis = current
      ? {
          masseSalariale: num(current.totalGross),
          netAPayer: num(current.totalNet),
          chargesPatronales: num(current.totalEmployerCharges),
          impot: num(current.totalIncomeTax),
          deductions: num(current.totalEmployeeDeductions),
          coutTotal: employerCost(current),
          effectif: current.nbEmployes,
        }
      : null;

    const deltas =
      current && previous
        ? {
            gross: pctDelta(num(current.totalGross), num(previous.totalGross)),
            net: pctDelta(num(current.totalNet), num(previous.totalNet)),
            cost: pctDelta(employerCost(current), employerCost(previous)),
            headcount: current.nbEmployes - previous.nbEmployes,
          }
        : { gross: null, net: null, cost: null, headcount: null };

    // Cost structure of the current run: net + employee deductions + income tax = gross;
    // adding employer charges yields the full employer cost.
    const costStructure = current
      ? {
          net: num(current.totalNet),
          deductions: num(current.totalEmployeeDeductions),
          incomeTax: num(current.totalIncomeTax),
          employerCharges: num(current.totalEmployerCharges),
        }
      : null;

    // Last 12 runs, oldest → newest, for the evolution chart.
    const evolution = sortedDesc
      .slice(0, 12)
      .reverse()
      .map((r) => ({
        periode: r.periode,
        gross: num(r.totalGross),
        net: num(r.totalNet),
        employerCharges: num(r.totalEmployerCharges),
        cost: employerCost(r),
        headcount: r.nbEmployes,
      }));

    // Lifecycle pipeline — how many runs sit at each actionable state.
    const pipeline = {
      draft: runs.filter((r) => r.status === "DRAFT" || r.status === "VARIABLES_LOCKED").length,
      toValidate: runs.filter((r) => r.status === "CALCULATED" || r.status === "REVIEW").length,
      toApprove: runs.filter((r) => r.status === "VALIDATED").length,
      toPay: runs.filter((r) => r.status === "APPROVED").length,
      inPayment: runs.filter((r) => r.status === "PAYMENT_INITIATED").length,
      toClose: runs.filter((r) => r.status === "PAID").length,
      closed: runs.filter((r) => r.status === "CLOSED").length,
    };

    const activeGarnishments = garnishments.filter((g) => g.status === "ACTIVE");
    const suspendedGarnishments = garnishments.filter((g) => g.status === "SUSPENDED");
    const garnish = {
      active: activeGarnishments.length,
      suspended: suspendedGarnishments.length,
      monthly: activeGarnishments.reduce((a, g) => a + num(g.monthlyAmount), 0),
      remaining: activeGarnishments.reduce((a, g) => a + num(g.remainingBalance), 0),
    };

    const retroPending = retro.filter((r) => r.status === "PENDING");
    const retroSummary = {
      pending: retroPending.length,
      applied: retro.filter((r) => r.status === "APPLIED").length,
      netDelta: retroPending.reduce((a, r) => a + num(r.deltaNet), 0),
    };

    const settlementsCalculated = settlements.filter((s) => s.status === "CALCULATED");
    const settlementsSummary = {
      calculated: settlementsCalculated.length,
      paid: settlements.filter((s) => s.status === "PAID").length,
      toPay: settlementsCalculated.reduce((a, s) => a + num(s.netSettlement), 0),
    };

    const config = {
      payElementsActive: payElements.filter((e) => e.active).length,
      payElementsTotal: payElements.length,
      taxScalesActive: taxScales.filter((t) => t.active).length,
      lookupActive: lookupTables.filter((t) => t.active).length,
    };

    const activeEmployees = employees.filter((e) => e.status === "ACTIVE").length;

    // Year-to-date cumulative across runs of the current calendar year.
    const year = String(new Date().getFullYear());
    const ytdRuns = runs.filter((r) => r.periode.startsWith(year));
    const ytd = {
      runsCount: ytdRuns.length,
      gross: ytdRuns.reduce((a, r) => a + num(r.totalGross), 0),
      net: ytdRuns.reduce((a, r) => a + num(r.totalNet), 0),
      employerCharges: ytdRuns.reduce((a, r) => a + num(r.totalEmployerCharges), 0),
      incomeTax: ytdRuns.reduce((a, r) => a + num(r.totalIncomeTax), 0),
    };

    const recentRuns = sortedDesc.slice(0, 6).map((r) => ({
      id: r.id,
      periode: r.periode,
      status: r.status,
      nbEmployes: r.nbEmployes,
      totalGross: num(r.totalGross),
      totalNet: num(r.totalNet),
      totalEmployerCharges: num(r.totalEmployerCharges),
      cost: employerCost(r),
      date: r.paidAt ?? r.approvedAt ?? r.validatedAt ?? r.calculatedAt ?? null,
    }));

    const now = new Date();
    const currentMonthPeriode = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
    const hasCurrentMonthRun = runs.some((r) => r.periode === currentMonthPeriode);

    return Response.json({
      ok: true,
      data: {
        organization: orgId,
        currency,
        canRun,
        currentMonthPeriode,
        hasCurrentMonthRun,
        runsTotal: runs.length,
        workforce: {
          active: activeEmployees,
          paidCurrent: current?.nbEmployes ?? 0,
        },
        current: current
          ? {
              id: current.id,
              periode: current.periode,
              status: current.status,
              calculatedAt: current.calculatedAt,
              validatedAt: current.validatedAt,
              approvedAt: current.approvedAt,
              paidAt: current.paidAt,
              closedAt: current.closedAt,
            }
          : null,
        kpis,
        deltas,
        costStructure,
        evolution,
        pipeline,
        garnish,
        retro: retroSummary,
        settlements: settlementsSummary,
        config,
        ytd,
        recentRuns,
      },
    });
  });
}
