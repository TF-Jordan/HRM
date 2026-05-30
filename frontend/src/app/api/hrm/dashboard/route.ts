import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as declarationsApi from "@/server/ksm/modules/declarations";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as expensesApi from "@/server/ksm/modules/expenses";
import * as leavesApi from "@/server/ksm/modules/leaves";
import * as medicalApi from "@/server/ksm/modules/medical";
import * as missionsApi from "@/server/ksm/modules/missions";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";
import * as reviewsApi from "@/server/ksm/modules/reviews";
import * as budgetApi from "@/server/ksm/modules/training-budgets";
import * as trainingsApi from "@/server/ksm/modules/trainings";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";
import { alertLevel } from "@/lib/medical-status";
import { hasPermission } from "@/server/permissions";

/**
 * Unified dashboard aggregator. Fetches every domain stream the dashboard
 * needs in parallel, then folds them into role-agnostic counters the client
 * can render the panels appropriate for each user. Buckets the current user
 * cannot read return null so the dashboard never spams 403s — KSM still
 * enforces the per-call permission.
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const today = new Date();
    const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const in30Days = new Date(today.getTime() + 30 * 86_400_000);

    const orgId = session.workspace?.organizationId;
    const me = await findMyEmployee(session);

    // Fire every read in parallel; swallow errors per-bucket.
    const safe = async <T>(p: Promise<T>, fallback: T): Promise<T> => p.catch(() => fallback);

    const employees = hasPermission(session, "hrm:employee:read") && orgId
      ? await safe(employeesApi.listEmployees(session, { organizationId: orgId }), [])
      : [];

    const pendingLeaves = hasPermission(session, "hrm:leave:approve") && orgId
      ? await safe(leavesApi.listPendingLeaves(session, orgId), [])
      : [];

    const myLeaves = me
      ? await safe(leavesApi.listLeavesByEmployee(me.id, session), [])
      : [];
    const myBalances = me
      ? await safe(employeesApi.listLeaveBalances(me.id, today.getFullYear(), session), [])
      : [];

    const myMissions = me
      ? await safe(missionsApi.listMissionOrdersByEmployee(me.id, session), [])
      : [];
    const declinedMissions = hasPermission(session, "hrm:mission:manage")
      ? await safe(missionsApi.listDeclined(session), [])
      : [];

    const submittedExpenses = hasPermission(session, "hrm:expense:read") && orgId
      ? await safe(
          expensesApi.listAllExpenseReports(session, { organizationId: orgId, status: "SUBMITTED" }),
          [],
        )
      : [];

    const trainings = hasPermission(session, "hrm:training:read") && orgId
      ? await safe(trainingsApi.listTrainings(session, orgId), [])
      : [];

    const budgets = hasPermission(session, "hrm:budget:read") && orgId
      ? await safe(budgetApi.listBudgets(session, today.getFullYear(), orgId), [])
      : [];

    const reviews = hasPermission(session, "hrm:review:read") && orgId
      ? await safe(reviewsApi.listReviewsByOrgAndPeriode(session, defaultPeriode(), orgId), [])
      : [];

    const declarations = hasPermission(session, "hrm:declaration:read") && orgId
      ? await safe(declarationsApi.listDeclarations(session, orgId), [])
      : [];

    const openOffers = hasPermission(session, "hrm:recruitment:read") && orgId
      ? await safe(recruitmentApi.listJobOffers(session, orgId), [])
      : [];

    const visits = hasPermission(session, "hrm:medical:read") && orgId
      ? await safe(medicalApi.listVisits(session, orgId), [])
      : [];
    const certificates = hasPermission(session, "hrm:medical:read") && orgId
      ? await safe(medicalApi.listCertificates(session, orgId), [])
      : [];

    const myEnrollments = me
      ? await safe(trainingsApi.listEnrollmentsByEmployee(me.id, session), [])
      : [];

    // === Apps per offer for the recruiter pipeline / interview count ===
    const offersWithApps = await Promise.all(
      openOffers
        .filter((o) => o.status === "PUBLISHED")
        .slice(0, 20) // bound the fan-out
        .map(async (o) => ({
          offer: o,
          applications: await safe(
            recruitmentApi.listApplicationsByJobOffer(o.id, session),
            [],
          ),
        })),
    );

    const allActiveApps = offersWithApps
      .flatMap((o) => o.applications)
      .filter((a) => a.status !== "REJECTED" && a.status !== "HIRED");

    // === Aggregates ===
    const cddExpiring = await Promise.all(
      employees
        .filter((e) => e.status === "ACTIVE")
        .slice(0, 50)
        .map(async (e) => {
          const contracts = await safe(
            employeesApi.listContracts(e.id, session),
            [] as employeesApi.ContractResponse[],
          );
          const active = contracts.find((c) => c.status === "ACTIVE" && c.type === "CDD");
          if (!active?.dateFin) return null;
          const end = new Date(active.dateFin);
          return end > today && end <= in30Days ? { employee: e, contract: active } : null;
        }),
    );
    const cddExpiringSoon = cddExpiring.filter((x): x is NonNullable<typeof x> => !!x);

    const myUpcoming = myLeaves
      .filter((l) => l.status === "APPROVED" && new Date(l.dateDebut) > today)
      .sort((a, b) => +new Date(a.dateDebut) - +new Date(b.dateDebut))[0] ?? null;

    const annualBalance = myBalances.find((b) => b.type === "ANNUAL") ?? null;

    const myPendingMissions = myMissions.filter((m) => m.status === "PENDING_ACCEPTANCE").length;

    const overdueVisits = visits.filter((v) => alertLevel(v.prochaineEcheance) === "OVERDUE").length;
    const dueSoonVisits = visits.filter((v) => alertLevel(v.prochaineEcheance) === "DUE_SOON").length;
    const visitsThisMonth = visits.filter((v) => new Date(v.dateVisite) >= startOfMonth).length;
    const certificatesThisMonth = certificates.filter((c) => new Date(c.dateEmission) >= startOfMonth).length;
    const overdueCertificates = certificates.filter((c) => alertLevel(c.dateExpiration) === "OVERDUE").length;

    const totalAllocated = budgets.reduce((acc, b) => acc + Number(b.montantAlloue ?? 0), 0);
    const totalEngaged = budgets.reduce((acc, b) => acc + Number(b.montantEngage ?? 0), 0);
    const totalRealised = budgets.reduce((acc, b) => acc + Number(b.montantRealise ?? 0), 0);
    const budgetAvailable = totalAllocated - totalEngaged - totalRealised;

    return Response.json({
      ok: true,
      data: {
        me: me ?? null,
        organization: orgId ?? null,
        // Admin RH / DRH
        effectif: {
          active: employees.filter((e) => e.status === "ACTIVE").length,
          total: employees.length,
        },
        contracts: {
          cddExpiringSoon: cddExpiringSoon.length,
        },
        // Leaves
        leaves: {
          pending: pendingLeaves.length,
          mineUpcoming: myUpcoming,
          mineAnnualBalance:
            annualBalance == null
              ? null
              : {
                  acquis: Number(annualBalance.acquis ?? 0),
                  pris: Number(annualBalance.pris ?? 0),
                  restant: Number(annualBalance.soldeRestant ?? 0),
                },
          minePendingCount: myLeaves.filter((l) => l.status === "PENDING").length,
        },
        // Missions
        missions: {
          pendingMine: myPendingMissions,
          declinedCount: declinedMissions.length,
        },
        // Expenses
        expenses: {
          submittedCount: submittedExpenses.length,
          submittedAmount: submittedExpenses.reduce(
            (acc, r) => acc + Number(r.totalMontant ?? 0),
            0,
          ),
        },
        // Trainings
        trainings: {
          inProgress: trainings.filter((t) => t.status === "IN_PROGRESS").length,
          completed: trainings.filter((t) => t.status === "COMPLETED").length,
          planned: trainings.filter((t) => t.status === "PLANNED").length,
          mineActiveCount: myEnrollments.filter((e) => e.status === "ENROLLED").length,
          mineCompletedCount: myEnrollments.filter((e) => e.status === "COMPLETED").length,
        },
        // Training budget
        budget: {
          allocated: totalAllocated,
          engaged: totalEngaged,
          realised: totalRealised,
          available: budgetAvailable,
        },
        // Reviews (current period)
        reviews: {
          periode: defaultPeriode(),
          finalized: reviews.filter((r) => r.status === "FINALIZED").length,
          submitted: reviews.filter((r) => r.status === "SUBMITTED").length,
          total: reviews.length,
        },
        // Declarations
        declarations: {
          toGenerate: declarations.filter((d) => d.statut === "DRAFT").length,
          toSubmit: declarations.filter((d) => d.statut === "GENERATED").length,
          submitted: declarations.filter((d) => d.statut === "SUBMITTED").length,
          acknowledged: declarations.filter((d) => d.statut === "ACKNOWLEDGED").length,
        },
        // Recruitment
        recruitment: {
          openOffers: openOffers.filter((o) => o.status === "PUBLISHED").length,
          activeApplications: allActiveApps.length,
          hires: offersWithApps
            .flatMap((o) => o.applications)
            .filter((a) => a.status === "HIRED").length,
        },
        // Medical
        medical: {
          visitsThisMonth,
          certificatesThisMonth,
          overdueChecks: overdueVisits + overdueCertificates,
          dueSoonChecks: dueSoonVisits,
        },
      },
    });
  });
}

function defaultPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-Q${Math.floor(now.getMonth() / 3) + 1}`;
}
