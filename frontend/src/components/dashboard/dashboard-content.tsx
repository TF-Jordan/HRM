"use client";

import { useQuery } from "@tanstack/react-query";
import {
  Briefcase,
  CalendarRange,
  ChartLine,
  CheckCircle2,
  Clock,
  FileText,
  GraduationCap,
  Loader2,
  Map,
  ShieldCheck,
  Stethoscope,
  UserCheck,
  Users,
  Wallet,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";

type DashboardPayload = {
  me: { id: string; matricule: string; actorDisplayName?: string | null } | null;
  organization: string | null;
  effectif: { active: number; total: number };
  contracts: { cddExpiringSoon: number };
  leaves: {
    pending: number;
    mineUpcoming: { id: string; dateDebut: string; dateFin: string; type: string } | null;
    mineAnnualBalance: { acquis: number; pris: number; restant: number } | null;
    minePendingCount: number;
  };
  missions: { pendingMine: number; declinedCount: number };
  expenses: { submittedCount: number; submittedAmount: number };
  trainings: {
    inProgress: number;
    completed: number;
    planned: number;
    mineActiveCount: number;
    mineCompletedCount: number;
  };
  budget: { allocated: number; engaged: number; realised: number; available: number };
  reviews: { periode: string; finalized: number; submitted: number; total: number };
  declarations: { toGenerate: number; toSubmit: number; submitted: number; acknowledged: number };
  recruitment: { openOffers: number; activeApplications: number; hires: number };
  medical: {
    visitsThisMonth: number;
    certificatesThisMonth: number;
    overdueChecks: number;
    dueSoonChecks: number;
  };
};

export function DashboardContent() {
  const t = useTranslations("dashboard");
  const tCommon = useTranslations("common");
  const locale = useLocale() as "fr" | "en";
  const { session } = useSession();

  const canAdmin = useCan("hrm:leave:approve");
  const canExpenses = useCan("hrm:expense:manage");
  const canMissions = useCan("hrm:mission:manage");
  const canRecruitment = useCan("hrm:recruitment:read");
  const canMedical = useCan("hrm:medical:create");
  const canDeclarations = useCan(["hrm:declaration:read", "hrm:declaration:manage"]);
  const canDrh = useCan(["hrm:budget:read", "hrm:review:manage"]);

  const query = useQuery({
    queryKey: ["hrm", "dashboard"],
    queryFn: () => apiFetch<DashboardPayload>("/api/hrm/dashboard"),
    refetchInterval: 60_000,
  });

  if (!session) return null;
  const user = session.user;
  const workspace = session.workspace;
  const data = query.data;
  const greeting = user.fullName.split(" ")[0] ?? user.fullName;

  return (
    <>
      <PageHeader
        ucBadge="UC-27"
        breadcrumb={[{ label: tCommon("appName") }, { label: t("title") }]}
        title={t("greeting", { name: greeting })}
        subtitle={
          workspace
            ? `${workspace.organizationName ?? workspace.organizationId} · ${user.roles[0] ?? user.email}`
            : user.roles[0] ?? user.email
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : !data ? null : (
        <div className="flex flex-col gap-6">
          {data.me && <EmployeePanel data={data} locale={locale} t={t} />}
          {canAdmin && <HRPanel data={data} t={t} />}
          {(canRecruitment || canDrh) &&
            data.recruitment.openOffers + data.recruitment.activeApplications > 0 && (
              <RecruitmentPanel data={data} t={t} />
            )}
          {canDrh && <DrhPanel data={data} locale={locale} t={t} />}
          {(canExpenses || canMissions) && (
            <AccountingPanel
              data={data}
              canExpenses={canExpenses}
              canMissions={canMissions}
              locale={locale}
              t={t}
            />
          )}
          {canDeclarations &&
            (data.declarations.toGenerate + data.declarations.toSubmit + data.declarations.acknowledged > 0) && (
              <PayrollPanel data={data} t={t} />
            )}
          {canMedical && <MedicalPanel data={data} t={t} />}
        </div>
      )}
    </>
  );
}

function EmployeePanel({
  data,
  locale,
  t,
}: {
  data: DashboardPayload;
  locale: "fr" | "en";
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  const me = data.me!;
  const annual = data.leaves.mineAnnualBalance;
  const upcoming = data.leaves.mineUpcoming;
  return (
    <Section title={t("sections.employee")} subtitle={t("sections.employeeSub", { matricule: me.matricule })}>
      <Tile
        href="/leaves/my"
        icon={CalendarRange}
        tone="orange"
        label={t("kpi.leaveBalance")}
        value={annual ? annual.restant.toFixed(1) : "—"}
        sub={annual ? t("kpi.leaveAcquired", { count: annual.acquis }) : t("kpi.noBalance")}
      />
      <Tile
        href="/leaves/my"
        icon={Clock}
        tone="info"
        label={t("kpi.nextLeave")}
        value={upcoming ? formatDate(upcoming.dateDebut, { locale }) : "—"}
        sub={upcoming ? formatDate(upcoming.dateFin, { locale }) : t("kpi.noUpcomingLeave")}
      />
      <Tile
        href="/mission-orders/mine"
        icon={Map}
        tone="warning"
        label={t("kpi.missionsPending")}
        value={data.missions.pendingMine}
        sub={data.missions.pendingMine > 0 ? t("kpi.actionRequired") : t("kpi.upToDate")}
      />
      <Tile
        href="/trainings/mine"
        icon={GraduationCap}
        tone="violet"
        label={t("kpi.trainingsActive")}
        value={data.trainings.mineActiveCount}
        sub={t("kpi.completedSuffix", { count: data.trainings.mineCompletedCount })}
      />
    </Section>
  );
}

function HRPanel({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  return (
    <Section title={t("sections.hr")} subtitle={t("sections.hrSub")}>
      <Tile
        href="/employees"
        icon={Users}
        tone="orange"
        label={t("kpi.activeWorkforce")}
        value={data.effectif.active}
        sub={t("kpi.totalSuffix", { count: data.effectif.total })}
      />
      <Tile
        href="/employees"
        icon={FileText}
        tone="warning"
        label={t("kpi.cddExpiring")}
        value={data.contracts.cddExpiringSoon}
        sub={t("kpi.next30d")}
      />
      <Tile
        href="/leaves"
        icon={CalendarRange}
        tone="info"
        label={t("kpi.leavesPending")}
        value={data.leaves.pending}
        sub={t("kpi.toReview")}
      />
      <Tile
        href="/reviews"
        icon={CheckCircle2}
        tone="success"
        label={t("kpi.reviewsFinalized")}
        value={`${data.reviews.finalized} / ${data.reviews.total || "—"}`}
        sub={data.reviews.periode}
      />
    </Section>
  );
}

function RecruitmentPanel({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  return (
    <Section title={t("sections.recruitment")} subtitle={t("sections.recruitmentSub")}>
      <Tile
        href="/recruitment"
        icon={Briefcase}
        tone="orange"
        label={t("kpi.openOffers")}
        value={data.recruitment.openOffers}
        sub={t("kpi.published")}
      />
      <Tile
        href="/recruitment"
        icon={UserCheck}
        tone="info"
        label={t("kpi.activeApplications")}
        value={data.recruitment.activeApplications}
        sub={t("kpi.activeApplicationsSub")}
      />
      <Tile
        href="/recruitment"
        icon={CheckCircle2}
        tone="success"
        label={t("kpi.hiresThisCycle")}
        value={data.recruitment.hires}
        sub={t("kpi.hiresThisCycleSub")}
      />
    </Section>
  );
}

function DrhPanel({
  data,
  locale,
  t,
}: {
  data: DashboardPayload;
  locale: "fr" | "en";
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  const usedPct =
    data.budget.allocated > 0
      ? Math.round(((data.budget.engaged + data.budget.realised) / data.budget.allocated) * 100)
      : 0;
  return (
    <Section title={t("sections.drh")} subtitle={t("sections.drhSub")}>
      <Tile
        href="/training-budgets"
        icon={Wallet}
        tone="orange"
        label={t("kpi.budgetAllocated")}
        value={formatNumber(data.budget.allocated, locale)}
        sub={t("kpi.usedPct", { pct: usedPct })}
      />
      <Tile
        href="/training-budgets"
        icon={ChartLine}
        tone="info"
        label={t("kpi.budgetAvailable")}
        value={formatNumber(data.budget.available, locale)}
        sub="XAF"
      />
      <Tile
        href="/trainings"
        icon={GraduationCap}
        tone="success"
        label={t("kpi.trainingsInProgress")}
        value={data.trainings.inProgress}
        sub={t("kpi.plannedSuffix", { count: data.trainings.planned })}
      />
      <Tile
        href="/reviews"
        icon={CheckCircle2}
        tone="violet"
        label={t("kpi.reviewsSubmitted")}
        value={data.reviews.submitted}
        sub={t("kpi.awaitingDecision")}
      />
    </Section>
  );
}

function AccountingPanel({
  data,
  canExpenses,
  canMissions,
  locale,
  t,
}: {
  data: DashboardPayload;
  canExpenses: boolean;
  canMissions: boolean;
  locale: "fr" | "en";
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  return (
    <Section title={t("sections.accounting")} subtitle={t("sections.accountingSub")}>
      {canExpenses && (
        <Tile
          href="/expenses?status=SUBMITTED"
          icon={FileText}
          tone="warning"
          label={t("kpi.expensesPending")}
          value={data.expenses.submittedCount}
          sub={`${formatNumber(data.expenses.submittedAmount, locale)} XAF`}
        />
      )}
      {canMissions && (
        <Tile
          href="/mission-orders?status=DECLINED"
          icon={Map}
          tone="danger"
          label={t("kpi.missionsDeclined")}
          value={data.missions.declinedCount}
          sub={t("kpi.toAmend")}
        />
      )}
    </Section>
  );
}

function PayrollPanel({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  return (
    <Section title={t("sections.payroll")} subtitle={t("sections.payrollSub")}>
      <Tile
        href="/declarations"
        icon={ShieldCheck}
        tone="warning"
        label={t("kpi.declarationsToGenerate")}
        value={data.declarations.toGenerate}
        sub={t("kpi.draft")}
      />
      <Tile
        href="/declarations"
        icon={ShieldCheck}
        tone="info"
        label={t("kpi.declarationsToSubmit")}
        value={data.declarations.toSubmit}
        sub={t("kpi.generated")}
      />
      <Tile
        href="/declarations"
        icon={ShieldCheck}
        tone="success"
        label={t("kpi.declarationsAcknowledged")}
        value={data.declarations.acknowledged}
        sub={t("kpi.validated")}
      />
    </Section>
  );
}

function MedicalPanel({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  return (
    <Section title={t("sections.medical")} subtitle={t("sections.medicalSub")}>
      <Tile
        href="/medical"
        icon={Stethoscope}
        tone="orange"
        label={t("kpi.visitsThisMonth")}
        value={data.medical.visitsThisMonth}
        sub={t("kpi.thisMonth")}
      />
      <Tile
        href="/medical"
        icon={FileText}
        tone="info"
        label={t("kpi.certificatesThisMonth")}
        value={data.medical.certificatesThisMonth}
        sub={t("kpi.thisMonth")}
      />
      <Tile
        href="/medical"
        icon={Stethoscope}
        tone="danger"
        label={t("kpi.medicalOverdue")}
        value={data.medical.overdueChecks}
        sub={t("kpi.toScheduleFast")}
      />
      <Tile
        href="/medical"
        icon={Stethoscope}
        tone="warning"
        label={t("kpi.medicalDueSoon")}
        value={data.medical.dueSoonChecks}
        sub={t("kpi.next30d")}
      />
    </Section>
  );
}

function Section({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
}) {
  return (
    <section>
      <div className="mb-3 flex items-end justify-between">
        <div>
          <h2 className="text-[14px] font-bold tracking-tight text-ink">{title}</h2>
          {subtitle && <p className="text-[12px] text-ink-3">{subtitle}</p>}
        </div>
      </div>
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">{children}</div>
    </section>
  );
}

type TileTone = "orange" | "info" | "warning" | "success" | "violet" | "danger";

function Tile({
  href,
  icon: Icon,
  tone,
  label,
  value,
  sub,
}: {
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  tone: TileTone;
  label: string;
  value: React.ReactNode;
  sub?: React.ReactNode;
}) {
  const dot = {
    orange: "bg-orange-500",
    info: "bg-info-500",
    warning: "bg-warning-500",
    success: "bg-success-500",
    violet: "bg-violet-500",
    danger: "bg-danger-500",
  }[tone];
  const iconBg = {
    orange: "bg-orange-50 text-orange-600",
    info: "bg-info-50 text-info-600",
    warning: "bg-warning-50 text-warning-600",
    success: "bg-success-50 text-success-600",
    violet: "bg-violet-50 text-violet-600",
    danger: "bg-danger-50 text-danger-600",
  }[tone];
  return (
    <Link href={href} className="block">
      <Card clickable className="h-full">
        <CardContent padding="lg">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">{label}</span>
            <span className={cn("inline-block h-2 w-2 rounded-full", dot)} />
          </div>
          <div className="flex items-end justify-between">
            <div>
              <div className="font-display font-mono-tabular text-[28px] font-extrabold leading-none tracking-tight text-ink">
                {value}
              </div>
              {sub && <div className="mt-1.5 text-[11.5px] text-ink-3">{sub}</div>}
            </div>
            <span className={cn("grid h-10 w-10 place-items-center rounded-[12px]", iconBg)}>
              <Icon className="h-5 w-5" />
            </span>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}
