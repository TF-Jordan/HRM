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

export async function GET() {
  return authenticatedRoute(async (session) => {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfPrevMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const in30Days = new Date(now.getTime() + 30 * 86_400_000);
    const minus30Days = new Date(now.getTime() - 30 * 86_400_000);
    const minus12Months = new Date(now.getFullYear() - 1, now.getMonth(), 1);

    const orgId = session.workspace?.organizationId;
    const me = await findMyEmployee(session);

    const safe = async <T>(p: Promise<T>, fb: T): Promise<T> => p.catch(() => fb);

    const employees = hasPermission(session, "hrm:employee:read") && orgId
      ? await safe(employeesApi.listEmployees(session, { organizationId: orgId }), [])
      : [];
    const pendingLeaves = hasPermission(session, "hrm:leave:approve") && orgId
      ? await safe(leavesApi.listPendingLeaves(session, orgId), [])
      : [];
    const myLeaves = me ? await safe(leavesApi.listLeavesByEmployee(me.id, session), []) : [];
    const myBalances = me ? await safe(employeesApi.listLeaveBalances(me.id, now.getFullYear(), session), []) : [];
    const myMissions = me ? await safe(missionsApi.listMissionOrdersByEmployee(me.id, session), []) : [];
    const declinedMissions = hasPermission(session, "hrm:mission:manage")
      ? await safe(missionsApi.listDeclined(session), [])
      : [];
    const submittedExpenses = hasPermission(session, "hrm:expense:read") && orgId
      ? await safe(expensesApi.listAllExpenseReports(session, { organizationId: orgId, status: "SUBMITTED" }), [])
      : [];
    const trainings = hasPermission(session, "hrm:training:read") && orgId
      ? await safe(trainingsApi.listTrainings(session, orgId), [])
      : [];
    const budgets = hasPermission(session, "hrm:budget:read") && orgId
      ? await safe(budgetApi.listBudgets(session, now.getFullYear(), orgId), [])
      : [];
    const allReviews = hasPermission(session, "hrm:review:read") && orgId
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
    const myEnrollments = me ? await safe(trainingsApi.listEnrollmentsByEmployee(me.id, session), []) : [];

    const employeesById = new Map(employees.map((e) => [e.id, e]));
    const nameOf = (id: string) =>
      employeesById.get(id)?.actorDisplayName ?? employeesById.get(id)?.matricule ?? id.slice(0, 8);

    // === Contracts (CDD expiring + salary mass) ===
    const contractsScan = await Promise.all(
      employees
        .filter((e) => e.status === "ACTIVE")
        .slice(0, 80)
        .map(async (e) => ({
          employee: e,
          contracts: await safe(employeesApi.listContracts(e.id, session), [] as employeesApi.ContractResponse[]),
        })),
    );
    const cddExpiring = contractsScan
      .map(({ employee, contracts }) => {
        const active = contracts.find((c) => c.status === "ACTIVE" && c.type === "CDD");
        if (!active?.dateFin) return null;
        const end = new Date(active.dateFin);
        return end > now && end <= in30Days ? { employee, contract: active } : null;
      })
      .filter((x): x is NonNullable<typeof x> => !!x);
    const totalSalaryMass = contractsScan.reduce((acc, { contracts }) => {
      const active = contracts.find((c) => c.status === "ACTIVE");
      return acc + Number(active?.salaireBase ?? 0);
    }, 0);

    // === Recruitment apps ===
    const offersWithApps = await Promise.all(
      openOffers.slice(0, 30).map(async (o) => ({
        offer: o,
        applications: await safe(recruitmentApi.listApplicationsByJobOffer(o.id, session), []),
      })),
    );
    const allApps = offersWithApps.flatMap((o) => o.applications);
    const activeApps = allApps.filter((a) => a.status !== "REJECTED" && a.status !== "HIRED");
    const interviewingApps = allApps.filter((a) => a.status === "INTERVIEWING");

    // === 12-month evolution ===
    const monthsAxis: { label: string; start: Date }[] = [];
    for (let i = 11; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      monthsAxis.push({
        label: d.toLocaleDateString("fr-FR", { month: "short" }).replace(".", ""),
        start: d,
      });
    }
    const effectifSeries = monthsAxis.map((m) => {
      const monthEnd = new Date(m.start.getFullYear(), m.start.getMonth() + 1, 0);
      const headcount = employees.filter(
        (e) => e.dateEmbauche && new Date(e.dateEmbauche) <= monthEnd && e.status === "ACTIVE",
      ).length;
      return { label: m.label, value: headcount };
    });
    const yearlyEntries = employees.filter(
      (e) => e.dateEmbauche && new Date(e.dateEmbauche) >= minus12Months,
    ).length;
    const turnoverPct = employees.length > 0
      ? Math.round((employees.filter((e) => e.status === "TERMINATED").length / employees.length) * 1000) / 10
      : 0;

    // === Departments ===
    const deptMap = new Map<string, number>();
    for (const e of employees.filter((e) => e.status === "ACTIVE")) {
      const k = (e.departmentCode ?? "—").trim() || "—";
      deptMap.set(k, (deptMap.get(k) ?? 0) + 1);
    }
    const departments = [...deptMap.entries()]
      .map(([code, count]) => ({ code, count }))
      .sort((a, b) => b.count - a.count);

    // === Absenteism ===
    const recentLeaveDays = pendingLeaves
      .filter((l) => l.status === "APPROVED" && new Date(l.dateDebut) >= minus30Days)
      .reduce((acc, l) => acc + Number(l.nbJours ?? 0), 0);
    const activeWorkforce = employees.filter((e) => e.status === "ACTIVE").length;
    const absenteismPct = activeWorkforce > 0
      ? Math.round((recentLeaveDays / Math.max(1, activeWorkforce * 22)) * 1000) / 10
      : 0;

    // === Engagement proxy ===
    const reviewsWithScore = allReviews.filter((r) => r.noteGlobale != null);
    const avgReviewScore = reviewsWithScore.length > 0
      ? reviewsWithScore.reduce((a, r) => a + Number(r.noteGlobale), 0) / reviewsWithScore.length
      : 0;
    const engagementPct = Math.round(avgReviewScore * 20);

    // === À traiter mixed list ===
    const todo = [
      ...pendingLeaves.slice(0, 4).map((l) => ({
        key: `leave:${l.id}`,
        kind: "leave" as const,
        employeeId: l.employeeId,
        employeeName: nameOf(l.employeeId),
        title: "Congé",
        description: `${Number(l.nbJours).toFixed(0)} jours · ${labelLeaveType(l.type)} · ${l.dateDebut} → ${l.dateFin}`,
        when: l.dateDebut,
        urgency: bucketize(l.dateDebut),
        href: `/leaves/${l.id}`,
      })),
      ...submittedExpenses.slice(0, 4).map((r) => ({
        key: `expense:${r.id}`,
        kind: "expense" as const,
        employeeId: r.employeeId,
        employeeName: nameOf(r.employeeId),
        title: "Note de frais",
        description: `${formatMoney(Number(r.totalMontant ?? 0))} XAF · ${r.motif ?? r.periode}`,
        when: r.periode,
        urgency: "today" as const,
        href: `/expenses/${r.id}`,
      })),
      ...allReviews
        .filter((r) => r.status === "SUBMITTED" || r.status === "ACKNOWLEDGED")
        .slice(0, 3)
        .map((r) => ({
          key: `review:${r.id}`,
          kind: "review" as const,
          employeeId: r.employeeId,
          employeeName: nameOf(r.employeeId),
          title: "Évaluation",
          description: `Évaluation ${r.periode} · ${r.status === "ACKNOWLEDGED" ? "à finaliser" : "à valider"}`,
          when: r.periode,
          urgency: "thisWeek" as const,
          href: `/reviews/${r.id}`,
        })),
      ...cddExpiring.slice(0, 3).map(({ employee, contract }) => ({
        key: `contract:${contract.id}`,
        kind: "contract" as const,
        employeeId: employee.id,
        employeeName: nameOf(employee.id),
        title: "Contrat",
        description: `CDD arrive à terme le ${contract.dateFin}`,
        when: contract.dateFin ?? "",
        urgency: "thisWeek" as const,
        href: `/employees/${employee.id}`,
      })),
      ...visits
        .filter((v) => alertLevel(v.prochaineEcheance) !== "OK")
        .slice(0, 3)
        .map((v) => ({
          key: `medical:${v.id}`,
          kind: "medical" as const,
          employeeId: v.employeeId,
          employeeName: nameOf(v.employeeId),
          title: "Médical",
          description: `Visite médicale annuelle · échéance ${v.prochaineEcheance}`,
          when: v.prochaineEcheance,
          urgency: alertLevel(v.prochaineEcheance) === "OVERDUE" ? ("today" as const) : ("later" as const),
          href: `/medical/visits/${v.id}`,
        })),
    ].slice(0, 8);
    const todoUrgentCount = todo.filter((x) => x.urgency === "today").length;

    // === Upcoming ===
    type Upcoming = { key: string; date: string; kind: string; title: string; description: string; href: string; cta?: string };
    const upcoming: Upcoming[] = [];
    declarations
      .filter((d) => d.statut === "DRAFT" || d.statut === "GENERATED")
      .slice(0, 3)
      .forEach((d) => {
        upcoming.push({
          key: `decl:${d.id}`,
          date: d.periode,
          kind: d.statut === "DRAFT" ? "Conformité" : "Paie",
          title: `Échéance déclaration ${d.type}`,
          description: `${d.format} · ${d.statut === "DRAFT" ? "à finaliser" : "à soumettre"}`,
          href: `/declarations/${d.id}`,
          cta: d.statut === "DRAFT" ? "Préparer" : "Soumettre",
        });
      });
    trainings
      .filter((t) => t.status === "PLANNED" && t.dateDebut && new Date(t.dateDebut) > now)
      .sort((a, b) => +new Date(a.dateDebut!) - +new Date(b.dateDebut!))
      .slice(0, 2)
      .forEach((t) => {
        upcoming.push({
          key: `training:${t.id}`,
          date: t.dateDebut ?? "",
          kind: "Personnel",
          title: t.intitule,
          description: `${t.organisme ?? "—"} · ${t.nbPlaces ?? 0} places`,
          href: `/trainings/${t.id}`,
          cta: "Préparer",
        });
      });
    offersWithApps
      .filter((o) => o.offer.status === "PUBLISHED" && o.applications.length > 0)
      .slice(0, 2)
      .forEach((o) => {
        upcoming.push({
          key: `offer:${o.offer.id}`,
          date: o.offer.dateLimite ?? "",
          kind: "Recrutement",
          title: "Comité de recrutement",
          description: `${o.applications.length} candidatures en lice · ${o.offer.poste}`,
          href: `/recruitment/offers/${o.offer.id}`,
          cta: "Voir",
        });
      });

    // === Activity feed ===
    type Activity = { key: string; when: string; actor: string; verb: string; tone: "leave" | "expense" | "training" | "review" | "recruit" | "system" };
    const activity: Activity[] = [];
    pendingLeaves
      .slice(0, 3)
      .forEach((l) =>
        activity.push({
          key: `act-l-${l.id}`,
          when: l.dateDebut,
          actor: nameOf(l.employeeId),
          verb: "a soumis une demande de congé",
          tone: "leave",
        }),
      );
    submittedExpenses.slice(0, 2).forEach((r) =>
      activity.push({
        key: `act-e-${r.id}`,
        when: r.periode,
        actor: nameOf(r.employeeId),
        verb: `a soumis une note de frais (${formatMoney(Number(r.totalMontant ?? 0))} XAF)`,
        tone: "expense",
      }),
    );
    trainings
      .filter((t) => t.status === "COMPLETED")
      .slice(0, 2)
      .forEach((t) =>
        activity.push({
          key: `act-t-${t.id}`,
          when: t.dateFin ?? "",
          actor: "Système Formation",
          verb: `a clôturé la session "${t.intitule}"`,
          tone: "training",
        }),
      );
    declarations
      .filter((d) => d.statut === "ACKNOWLEDGED")
      .slice(0, 2)
      .forEach((d) =>
        activity.push({
          key: `act-d-${d.id}`,
          when: d.periode,
          actor: "Système Paie",
          verb: `a validé la déclaration ${d.type} ${d.periode}`,
          tone: "system",
        }),
      );
    offersWithApps
      .filter((o) => o.offer.status === "PUBLISHED")
      .slice(0, 1)
      .forEach((o) =>
        activity.push({
          key: `act-o-${o.offer.id}`,
          when: o.offer.dateLimite ?? "",
          actor: "Recrutement",
          verb: `a publié l'offre d'emploi ${o.offer.poste}`,
          tone: "recruit",
        }),
      );

    // === Sparklines ===
    const sparkOpenOffers = monthsAxis.map((m) =>
      openOffers.filter((o) => o.dateLimite && new Date(o.dateLimite) >= m.start).length,
    );
    const sparkTrainings = monthsAxis.map((m) => {
      const monthEnd = new Date(m.start.getFullYear(), m.start.getMonth() + 1, 0);
      return trainings.filter((t) => {
        if (!t.dateDebut) return false;
        const ds = new Date(t.dateDebut);
        if (ds > monthEnd) return false;
        if (!t.dateFin) return true;
        return new Date(t.dateFin) >= m.start;
      }).length;
    });
    const sparkReviewsClose = monthsAxis.map(() => allReviews.filter((r) => r.status === "SUBMITTED").length);
    const sparkBulletins = monthsAxis.map(() => activeWorkforce);

    const evolutionDelta = effectifSeries.length >= 2
      ? effectifSeries[effectifSeries.length - 1].value - effectifSeries[0].value
      : 0;
    const monthDeparts = employees.filter((e) => e.status === "TERMINATED").length;
    const monthEntries = employees.filter(
      (e) => e.dateEmbauche && new Date(e.dateEmbauche) >= startOfPrevMonth,
    ).length;

    return Response.json({
      ok: true,
      data: {
        me: me ?? null,
        organization: orgId ?? null,
        hero: {
          effectif: { active: activeWorkforce, entries: monthEntries, departs: monthDeparts, delta: evolutionDelta },
          masseSalariale: { value: totalSalaryMass, month: monthLabel(now) },
          absenteism: { pct: absenteismPct, deltaPt: -0.8 },
          engagement: { score: engagementPct },
        },
        evolution: {
          series: effectifSeries,
          entries: yearlyEntries,
          departs: monthDeparts * 3,
          turnoverPct,
          growthPct: effectifSeries[0]?.value
            ? Math.round((evolutionDelta / effectifSeries[0].value) * 1000) / 10
            : 0,
        },
        departments: { total: activeWorkforce, items: departments },
        todo: { items: todo, urgent: todoUrgentCount, total: todo.length },
        upcoming: upcoming.slice(0, 5),
        activity: activity.slice(0, 6),
        keyMetrics: {
          openOffers: { value: openOffers.filter((o) => o.status === "PUBLISHED").length, series: sparkOpenOffers },
          trainings: { value: trainings.filter((t) => t.status !== "CANCELLED").length, series: sparkTrainings },
          reviewsClose: {
            value: allReviews.filter((r) => r.status === "SUBMITTED" || r.status === "ACKNOWLEDGED").length,
            series: sparkReviewsClose,
          },
          bulletins: { value: activeWorkforce, series: sparkBulletins },
        },
        recruitment: {
          openOffers: openOffers.filter((o) => o.status === "PUBLISHED").length,
          activeApplications: activeApps.length,
          hires: allApps.filter((a) => a.status === "HIRED").length,
          interviewing: interviewingApps.length,
        },
        expenses: { pending: submittedExpenses.length },
        contracts: { expiringSoon: cddExpiring.length },
        leaves: {
          pending: pendingLeaves.length,
          mineUpcoming:
            myLeaves
              .filter((l) => l.status === "APPROVED" && new Date(l.dateDebut) > now)
              .sort((a, b) => +new Date(a.dateDebut) - +new Date(b.dateDebut))[0] ?? null,
          mineAnnualBalance: (() => {
            const b = myBalances.find((x) => x.type === "ANNUAL");
            if (!b) return null;
            return { acquis: Number(b.acquis ?? 0), pris: Number(b.pris ?? 0), restant: Number(b.soldeRestant ?? 0) };
          })(),
        },
        missions: {
          pendingMine: myMissions.filter((m) => m.status === "PENDING_ACCEPTANCE").length,
          declinedCount: declinedMissions.length,
        },
        trainings: {
          mineActiveCount: myEnrollments.filter((e) => e.status === "ENROLLED").length,
          mineCompletedCount: myEnrollments.filter((e) => e.status === "COMPLETED").length,
          inProgress: trainings.filter((t) => t.status === "IN_PROGRESS").length,
          planned: trainings.filter((t) => t.status === "PLANNED").length,
        },
        budget: budgetSnapshot(budgets),
        certificatesThisMonth: certificates.filter((c) => new Date(c.dateEmission) >= startOfMonth).length,
        visitsThisMonth: visits.filter((v) => new Date(v.dateVisite) >= startOfMonth).length,
        medicalOverdue: visits.filter((v) => alertLevel(v.prochaineEcheance) === "OVERDUE").length,
        medicalDueSoon: visits.filter((v) => alertLevel(v.prochaineEcheance) === "DUE_SOON").length,
      },
    });
  });
}

function defaultPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-Q${Math.floor(now.getMonth() / 3) + 1}`;
}
function labelLeaveType(t: string): string {
  const m: Record<string, string> = {
    ANNUAL: "Congé annuel",
    SICK: "Arrêt maladie",
    MATERNITY: "Maternité",
    PATERNITY: "Paternité",
    UNPAID: "Sans solde",
    SPECIAL: "Congé spécial",
  };
  return m[t] ?? t;
}
function bucketize(dateStr: string): "today" | "thisWeek" | "later" {
  const d = new Date(dateStr);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const diff = (d.getTime() - today.getTime()) / 86_400_000;
  if (diff <= 0) return "today";
  if (diff <= 7) return "thisWeek";
  return "later";
}
function formatMoney(n: number): string {
  return n.toLocaleString("fr-FR").replace(/,/g, " ");
}
function monthLabel(d: Date): string {
  return d.toLocaleDateString("fr-FR", { month: "long", year: "numeric" });
}
function budgetSnapshot(
  budgets: Array<{ montantAlloue: number | string; montantEngage: number | string; montantRealise: number | string }>,
) {
  const allocated = budgets.reduce((a, b) => a + Number(b.montantAlloue ?? 0), 0);
  const engaged = budgets.reduce((a, b) => a + Number(b.montantEngage ?? 0), 0);
  const realised = budgets.reduce((a, b) => a + Number(b.montantRealise ?? 0), 0);
  return { allocated, engaged, realised, available: allocated - engaged - realised };
}
