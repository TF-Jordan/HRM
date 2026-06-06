import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as expensesApi from "@/server/ksm/modules/expenses";
import * as leavesApi from "@/server/ksm/modules/leaves";
import * as medicalApi from "@/server/ksm/modules/medical";
import * as missionsApi from "@/server/ksm/modules/missions";
import * as payrollApi from "@/server/ksm/modules/payroll";
import * as reviewsApi from "@/server/ksm/modules/reviews";
import * as timesheetsApi from "@/server/ksm/modules/timesheets";
import * as trainingsApi from "@/server/ksm/modules/trainings";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

type ActionUrgency = "high" | "medium" | "low";
type ActionKind = "mission" | "review" | "timesheet" | "expense" | "medical";
type ActionItem = {
  key: string;
  kind: ActionKind;
  title: string;
  description: string;
  urgency: ActionUrgency;
  href: string;
};

export async function GET() {
  return authenticatedRoute(async (session) => {
    const now = new Date();
    const currentPeriode = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;

    const safe = async <T>(p: Promise<T>, fb: T): Promise<T> => p.catch(() => fb);

    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({
        ok: true,
        data: {
          employee: null,
          leaveBalances: [],
          recentRequests: [],
          upcomingEvents: [],
          actions: [],
          actionCount: 0,
          monthlyHours: 0,
          payslipSeries: [],
          latestNet: null,
          annualLeaveBalance: null,
          activeEnrollmentCount: 0,
          pendingReviewCount: 0,
        },
      });
    }

    const [
      leaveBalances,
      myLeaves,
      myExpenses,
      myTimesheets,
      myMissions,
      myEnrollments,
      myReviews,
      payrollRuns,
      myVisits,
      myCerts,
    ] = await Promise.all([
      safe(employeesApi.listLeaveBalances(employee.id, now.getFullYear(), session), []),
      safe(leavesApi.listLeavesByEmployee(employee.id, session), []),
      safe(expensesApi.listExpenseReportsByEmployee(employee.id, session), []),
      safe(timesheetsApi.listByEmployee(employee.id, currentPeriode, session), []),
      safe(missionsApi.listMissionOrdersByEmployee(employee.id, session), []),
      safe(trainingsApi.listEnrollmentsByEmployee(employee.id, session), []),
      safe(reviewsApi.listReviewsByEmployee(employee.id, session), []),
      safe(payrollApi.listPayrollRuns(session), []),
      safe(medicalApi.listVisitsByEmployee(employee.id, session), []),
      safe(medicalApi.listCertificatesByEmployee(employee.id, session), []),
    ]);

    // ── Payslip history (last 12 validated/paid runs) ───────────────────────
    const recentRuns = payrollRuns
      .filter((r) => r.status === "VALIDATED" || r.status === "PAID")
      .sort((a, b) => a.periode.localeCompare(b.periode))
      .slice(-12);

    const payslipHistory = await Promise.all(
      recentRuns.map(async (run) => {
        const entries = await safe(payrollApi.listPayrollEntries(run.id, session), []);
        const mine = entries.find((e) => e.employeeId === employee.id);
        return mine ? { periode: run.periode, net: Number(mine.net ?? 0) } : null;
      }),
    );
    const payslipSeries = payslipHistory.filter((x): x is { periode: string; net: number } => x !== null);
    const latestNet = payslipSeries.at(-1)?.net ?? null;

    // ── Monthly hours ────────────────────────────────────────────────────────
    const monthlyHours = myTimesheets.reduce(
      (acc, ts) => acc + Number(ts.heuresNormales ?? 0) + Number(ts.heuresSupplementaires ?? 0),
      0,
    );

    // ── Recent requests: leaves + expenses + missions ────────────────────────
    type RequestItem = {
      id: string;
      type: "leave" | "expense" | "mission";
      title: string;
      meta: string;
      status: string;
      step: number; // 1=submitted, 2=approved, 3=closed
      href: string;
    };

    const recentRequests: RequestItem[] = [
      ...myLeaves
        .slice(-4)
        .reverse()
        .map((l) => ({
          id: l.id,
          type: "leave" as const,
          title: `${labelLeaveType(l.type)} · ${l.dateDebut} → ${l.dateFin}`,
          meta: `${Number(l.nbJours ?? 0).toFixed(0)} jours`,
          status: l.status,
          step: stepOf(l.status),
          href: `/leaves/${l.id}`,
        })),
      ...myExpenses
        .slice(-3)
        .reverse()
        .map((r) => ({
          id: r.id,
          type: "expense" as const,
          title: `Note de frais · ${r.motif ?? r.periode}`,
          meta: `${formatMoney(Number(r.totalMontant ?? 0))} XAF`,
          status: r.status,
          step: expenseStepOf(r.status),
          href: `/expenses/${r.id}`,
        })),
      ...myMissions
        .slice(-3)
        .reverse()
        .map((m) => ({
          id: m.id,
          type: "mission" as const,
          title: `Mission · ${m.destination}`,
          meta: `${m.dateDebut} → ${m.dateFin}`,
          status: m.status,
          step: missionStepOf(m.status),
          href: `/mission-orders/${m.id}`,
        })),
    ]
      .sort((a, b) => a.id.localeCompare(b.id))
      .slice(0, 8);

    // ── Upcoming personal events ─────────────────────────────────────────────
    type EventItem = {
      key: string;
      date: string;
      tag: string;
      tagColor: "orange" | "amber" | "violet" | "blue" | "green" | "teal";
      title: string;
      sub: string;
      href: string;
    };

    const upcomingEvents: EventItem[] = [];

    // Next approved leave
    myLeaves
      .filter((l) => l.status === "APPROVED" && new Date(l.dateDebut) > now)
      .sort((a, b) => +new Date(a.dateDebut) - +new Date(b.dateDebut))
      .slice(0, 1)
      .forEach((l) =>
        upcomingEvents.push({
          key: `leave-${l.id}`,
          date: l.dateDebut,
          tag: labelLeaveType(l.type),
          tagColor: "amber",
          title: `Congé approuvé · ${Number(l.nbJours ?? 0).toFixed(0)} jours`,
          sub: `Retour le ${l.dateFin}`,
          href: `/leaves/${l.id}`,
        }),
      );

    // Upcoming missions
    myMissions
      .filter((m) => m.status === "APPROVED" && new Date(m.dateDebut) > now)
      .sort((a, b) => +new Date(a.dateDebut) - +new Date(b.dateDebut))
      .slice(0, 2)
      .forEach((m) =>
        upcomingEvents.push({
          key: `mission-${m.id}`,
          date: m.dateDebut,
          tag: "Mission",
          tagColor: "blue",
          title: m.objet,
          sub: `${m.destination} · ${m.dateDebut} → ${m.dateFin}`,
          href: `/mission-orders/${m.id}`,
        }),
      );

    // Upcoming reviews
    myReviews
      .filter((r) => r.status === "DRAFT" || r.status === "SUBMITTED")
      .slice(0, 1)
      .forEach((r) =>
        upcomingEvents.push({
          key: `review-${r.id}`,
          date: r.periode,
          tag: "Évaluation",
          tagColor: "violet",
          title: `Évaluation ${r.periode}`,
          sub: r.status === "DRAFT" ? "À compléter" : "En attente de validation",
          href: `/reviews/${r.id}`,
        }),
      );

    // Next payslip (estimate: 28th of current month)
    const paydayEst = new Date(now.getFullYear(), now.getMonth(), 28);
    if (paydayEst > now) {
      upcomingEvents.push({
        key: "payday",
        date: paydayEst.toISOString().slice(0, 10),
        tag: "Paie",
        tagColor: "green",
        title: "Bulletin de paie disponible",
        sub: latestNet
          ? `Dernier net : ${formatMoney(latestNet)} XAF`
          : "Estimation en cours",
        href: "/payslips",
      });
    }

    upcomingEvents.sort((a, b) => a.date.localeCompare(b.date));

    // ── Action center: items requiring the EMPLOYEE's own action ──────────────
    const actions: ActionItem[] = [];
    const daysUntil = (iso: string) =>
      Math.floor((new Date(iso).getTime() - now.getTime()) / 86_400_000);

    // Missions awaiting the employee's acceptance — most time-sensitive.
    myMissions
      .filter((m) => m.status === "PENDING_ACCEPTANCE")
      .forEach((m) =>
        actions.push({
          key: `mission-${m.id}`,
          kind: "mission",
          title: `Mission · ${m.destination}`,
          description: `${m.dateDebut} → ${m.dateFin}`,
          urgency: "high",
          href: `/mission-orders/${m.id}`,
        }),
      );

    // Reviews submitted by the manager, awaiting the employee's acknowledgement.
    myReviews
      .filter((r) => r.status === "SUBMITTED")
      .forEach((r) =>
        actions.push({
          key: `review-${r.id}`,
          kind: "review",
          title: `Évaluation ${r.periode}`,
          description: "À accuser réception",
          urgency: "medium",
          href: `/reviews/${r.id}`,
        }),
      );

    // Current-period timesheet still in draft → submit it.
    myTimesheets
      .filter((ts) => ts.status === "DRAFT")
      .forEach((ts) =>
        actions.push({
          key: `timesheet-${ts.id}`,
          kind: "timesheet",
          title: `Pointage ${ts.periode}`,
          description: "Brouillon à soumettre",
          urgency: "medium",
          href: `/timesheets/${ts.id}`,
        }),
      );

    // Expense reports left in draft → finish & submit.
    myExpenses
      .filter((r) => r.status === "DRAFT")
      .forEach((r) =>
        actions.push({
          key: `expense-${r.id}`,
          kind: "expense",
          title: `Note de frais · ${r.motif ?? r.periode}`,
          description: "Brouillon à soumettre",
          urgency: "low",
          href: `/expenses/${r.id}`,
        }),
      );

    // Medical: an overdue next-visit date is the worker's signal to schedule.
    myVisits
      .filter((v) => v.prochaineEcheance && daysUntil(v.prochaineEcheance) < 0)
      .sort((a, b) => a.prochaineEcheance.localeCompare(b.prochaineEcheance))
      .slice(0, 1)
      .forEach((v) =>
        actions.push({
          key: `medical-visit-${v.id}`,
          kind: "medical",
          title: "Visite médicale à planifier",
          description: `Échéance dépassée le ${v.prochaineEcheance}`,
          urgency: "high",
          href: "/medical",
        }),
      );

    // Medical: a certificate expiring within 30 days.
    myCerts
      .filter((c) => {
        if (!c.dateExpiration) return false;
        const d = daysUntil(c.dateExpiration);
        return d >= 0 && d <= 30;
      })
      .slice(0, 1)
      .forEach((c) =>
        actions.push({
          key: `medical-cert-${c.id}`,
          kind: "medical",
          title: `Certificat « ${c.typeCertificat} » bientôt expiré`,
          description: `Expire le ${c.dateExpiration}`,
          urgency: "low",
          href: "/medical",
        }),
      );

    const urgencyRank: Record<ActionUrgency, number> = { high: 0, medium: 1, low: 2 };
    actions.sort((a, b) => urgencyRank[a.urgency] - urgencyRank[b.urgency]);

    // ── Balances ─────────────────────────────────────────────────────────────
    const annualBal = leaveBalances.find((b) => b.type === "ANNUAL");
    const annualLeaveBalance = annualBal
      ? {
          restant: Number(annualBal.soldeRestant ?? 0),
          acquis: Number(annualBal.acquis ?? 0),
          pris: Number(annualBal.pris ?? 0),
        }
      : null;

    const allBalances = leaveBalances.map((b) => ({
      type: b.type,
      restant: Number(b.soldeRestant ?? 0),
      acquis: Number(b.acquis ?? 0),
      pris: Number(b.pris ?? 0),
    }));

    return Response.json({
      ok: true,
      data: {
        employee,
        leaveBalances: allBalances,
        recentRequests,
        upcomingEvents: upcomingEvents.slice(0, 5),
        actions: actions.slice(0, 6),
        actionCount: actions.length,
        monthlyHours,
        currentPeriode,
        payslipSeries,
        latestNet,
        annualLeaveBalance,
        activeEnrollmentCount: myEnrollments.filter((e) => e.status === "ENROLLED").length,
        pendingReviewCount: myReviews.filter((r) => r.status === "DRAFT" || r.status === "SUBMITTED").length,
      },
    });
  });
}

function labelLeaveType(t: string): string {
  const m: Record<string, string> = {
    ANNUAL: "Congé annuel",
    SICK: "Maladie",
    MATERNITY: "Maternité",
    PATERNITY: "Paternité",
    UNPAID: "Sans solde",
    SPECIAL: "Congé spécial",
  };
  return m[t] ?? t;
}

function stepOf(status: string): number {
  if (status === "APPROVED") return 2;
  if (status === "REJECTED" || status === "CANCELLED") return 3;
  return 1;
}

function expenseStepOf(status: string): number {
  if (status === "APPROVED" || status === "REIMBURSED") return 2;
  if (status === "REJECTED") return 3;
  return 1;
}

function missionStepOf(status: string): number {
  if (status === "APPROVED" || status === "IN_PROGRESS" || status === "COMPLETED") return 2;
  if (status === "DECLINED" || status === "CANCELLED") return 3;
  return 1;
}

function formatMoney(n: number): string {
  return n.toLocaleString("fr-FR").replace(/,/g, " ");
}
