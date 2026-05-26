"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import {
  Users,
  Wallet,
  CalendarDays,
  AlertTriangle,
  Pause,
  Plus,
  Receipt,
  Coins,
  ClipboardList,
  UserPlus,
  Briefcase,
  Stethoscope,
  Calculator,
  GraduationCap,
  CheckCircle2,
  FileText,
  Plane,
} from "lucide-react";
import { PageHeader } from "@/components/shell/PageHeader";
import { KpiCard } from "@/components/ui-tokens/KpiCard";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Link } from "@/i18n/navigation";
import { useHrmDashboard } from "@/hooks/modules/useDashboard";
import { useEmployeeLeaves, usePendingLeaves } from "@/hooks/modules/useLeaves";
import { useEmployeeLoans, usePendingLoans } from "@/hooks/modules/useLoans";
import { useAllContracts } from "@/hooks/modules/useContracts";
import { useFormat } from "@/hooks/useFormat";
import { useEmployeeExpenses } from "@/hooks/modules/useExpenses";
import { useEmployeeMissions } from "@/hooks/modules/useMissions";
import type { RoleCode } from "@/lib/roles";

export type DashboardClientProps = {
  firstName: string;
  roleCode: RoleCode;
  employeeId: string | null;
};

export function DashboardClient({ firstName, roleCode, employeeId }: DashboardClientProps) {
  const t = useTranslations("dashboard");
  const tRoles = useTranslations("roles");

  const subtitle = t(`subtitleByRole.${roleCode}` as never);

  return (
    <div className="space-y-7 animate-fade-up">
      <PageHeader
        title={t("welcome", { name: firstName })}
        subtitle={`${subtitle} · ${tRoles(roleCode)}`}
      />

      {renderForRole(roleCode, employeeId)}
    </div>
  );
}

function renderForRole(roleCode: RoleCode, employeeId: string | null) {
  switch (roleCode) {
    case "PLATFORM_ADMIN":
    case "HRM_ADMIN":
    case "DRH":
      return <HRAdminDashboard />;
    case "RESP_PAIE":
    case "COMPTABLE":
      return <PayrollDashboard isPayrollOfficer={roleCode === "RESP_PAIE"} />;
    case "MANAGER":
      return <ManagerDashboard />;
    case "RECRUTEUR":
      return <RecruiterDashboard />;
    case "MEDECIN":
      return <DoctorDashboard />;
    case "EMPLOYE":
    default:
      return <EmployeeDashboard employeeId={employeeId} />;
  }
}

/* -------------------------------------------------------------------------- */
/* HR Admin / DRH / Platform Admin                                            */
/* -------------------------------------------------------------------------- */

function HRAdminDashboard() {
  const t = useTranslations("dashboard");
  const tNav = useTranslations("navigation");
  const tEmp = useTranslations("employees");
  const fmt = useFormat();
  const { data, isLoading } = useHrmDashboard();
  const hc = data?.headcount;
  const contracts = useAllContracts();
  const pendingLeaves = usePendingLeaves();
  const pendingLoans = usePendingLoans();

  const masseSalariale = (contracts.data ?? [])
    .filter((c) => c.status === "ACTIVE")
    .reduce((s, c) => s + Number(c.salaireBase), 0);

  return (
    <>
      <div className="flex justify-end">
        <Button asChild>
          <Link href="/employees/new">
            <Plus className="size-4" />
            {tEmp("list.newButton")}
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {isLoading ? (
          Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-32 rounded-[20px]" />)
        ) : (
          <>
            <KpiCard
              tone="orange"
              icon={Users}
              label={t("kpi.activeEmployees")}
              value={hc?.active ?? 0}
              footer={`${hc?.total ?? 0} total`}
            />
            <KpiCard
              tone="dark"
              icon={Wallet}
              label={t("kpi.monthlyPayroll")}
              value={contracts.isLoading ? "…" : fmt.moneyShort(masseSalariale)}
              footer={tNav("items.contracts")}
            />
            <KpiCard
              tone="amber"
              icon={CalendarDays}
              label={t("kpi.pendingLeaves")}
              value={pendingLeaves.isLoading ? "…" : (pendingLeaves.data?.length ?? 0)}
            />
            <KpiCard
              tone="violet"
              icon={Coins}
              label={t("kpi.pendingLoans")}
              value={pendingLoans.isLoading ? "…" : (pendingLoans.data?.length ?? 0)}
            />
          </>
        )}
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>{tNav("items.employees")}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              <Mini label="Actifs" value={hc?.active} tone="green" />
              <Mini label="En congé" value={hc?.onLeave} tone="blue" />
              <Mini
                label="Suspendus"
                value={hc?.suspended}
                tone="amber"
                icon={<Pause className="size-3.5" />}
              />
              <Mini label="Sortis" value={hc?.terminated} tone="red" />
            </div>
            <div className="mt-4 flex justify-end">
              <Button asChild variant="secondary">
                <Link href="/employees">{tNav("items.employees")}</Link>
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>{t("sections.quickActions")}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
              <QuickAction href="/employees" icon={Users} label={tNav("items.employees")} />
              <QuickAction href="/payroll" icon={Wallet} label={tNav("items.payroll")} />
              <QuickAction href="/declarations" icon={ClipboardList} label={tNav("items.declarations")} />
              <QuickAction href="/recruitment" icon={UserPlus} label={tNav("items.recruitment")} />
              <QuickAction href="/trainings" icon={GraduationCap} label={tNav("items.trainings")} />
              <QuickAction href="/analytics" icon={Calculator} label={tNav("items.analytics")} />
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}

/* -------------------------------------------------------------------------- */
/* Payroll                                                                    */
/* -------------------------------------------------------------------------- */

function PayrollDashboard({ isPayrollOfficer }: { isPayrollOfficer: boolean }) {
  const t = useTranslations("dashboard");
  const tNav = useTranslations("navigation");
  const loans = usePendingLoans();

  return (
    <>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard tone="orange" icon={Wallet} label={t("kpi.payrollCycles")} value="—" />
        <KpiCard
          tone="dark"
          icon={Coins}
          label={t("kpi.pendingLoans")}
          value={loans.isLoading ? "…" : (loans.data?.length ?? 0)}
        />
        <KpiCard tone="amber" icon={Receipt} label={t("kpi.pendingExpenses")} value="—" />
        {isPayrollOfficer && (
          <KpiCard tone="violet" icon={ClipboardList} label={t("kpi.declarationsDue")} value="—" />
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t("sections.quickActions")}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <QuickAction href="/payroll" icon={Wallet} label={t("actions.runPayroll")} />
            <QuickAction href="/loans/approve" icon={Coins} label={t("actions.approveLoans")} />
            <QuickAction href="/expenses/approve" icon={Receipt} label={t("actions.approveExpenses")} />
            {isPayrollOfficer && (
              <QuickAction
                href="/declarations"
                icon={ClipboardList}
                label={tNav("items.declarations")}
              />
            )}
          </div>
        </CardContent>
      </Card>
    </>
  );
}

/* -------------------------------------------------------------------------- */
/* Manager                                                                    */
/* -------------------------------------------------------------------------- */

function ManagerDashboard() {
  const t = useTranslations("dashboard");
  const tNav = useTranslations("navigation");
  const leaves = usePendingLeaves();

  return (
    <>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          tone="orange"
          icon={CalendarDays}
          label={t("kpi.pendingLeaves")}
          value={leaves.isLoading ? "…" : (leaves.data?.length ?? 0)}
        />
        <KpiCard tone="dark" icon={Receipt} label={t("kpi.pendingExpenses")} value="—" />
        <KpiCard tone="amber" icon={Plane} label={t("kpi.missionsAssigned")} value="—" />
        <KpiCard tone="violet" icon={Users} label={t("kpi.teamSize")} value="—" />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t("sections.quickActions")}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <QuickAction
              href="/leaves/pending"
              icon={CalendarDays}
              label={t("actions.approveLeaves")}
            />
            <QuickAction
              href="/expenses/approve"
              icon={Receipt}
              label={t("actions.approveExpenses")}
            />
            <QuickAction href="/mission-orders" icon={Plane} label={tNav("items.missionOrders")} />
            <QuickAction href="/reviews" icon={CheckCircle2} label={tNav("items.reviews")} />
          </div>
        </CardContent>
      </Card>
    </>
  );
}

/* -------------------------------------------------------------------------- */
/* Recruiter                                                                  */
/* -------------------------------------------------------------------------- */

function RecruiterDashboard() {
  const t = useTranslations("dashboard");
  const tNav = useTranslations("navigation");

  return (
    <>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard tone="orange" icon={UserPlus} label={t("kpi.applicationsToReview")} value="—" />
        <KpiCard tone="dark" icon={Briefcase} label={t("kpi.activeJobOffers")} value="—" />
        <KpiCard tone="amber" icon={Users} label={t("kpi.newApplications")} value="—" />
        <KpiCard tone="violet" icon={CheckCircle2} label={t("kpi.openPositions")} value="—" />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t("sections.quickActions")}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <QuickAction href="/recruitment" icon={UserPlus} label={tNav("items.recruitment")} />
            <QuickAction href="/recruitment" icon={Briefcase} label={t("actions.newOffer")} />
            <QuickAction href="/employees" icon={Users} label={tNav("items.employees")} />
          </div>
        </CardContent>
      </Card>
    </>
  );
}

/* -------------------------------------------------------------------------- */
/* Doctor                                                                     */
/* -------------------------------------------------------------------------- */

function DoctorDashboard() {
  const t = useTranslations("dashboard");
  const tNav = useTranslations("navigation");

  return (
    <>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard tone="orange" icon={Stethoscope} label={t("kpi.upcomingVisits")} value="—" />
        <KpiCard
          tone="amber"
          icon={AlertTriangle}
          label={t("kpi.certificatesExpiring")}
          value="—"
        />
        <KpiCard tone="dark" icon={Users} label={tNav("items.employees")} value="—" />
        <KpiCard tone="violet" icon={FileText} label="" value="—" />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t("sections.quickActions")}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <QuickAction href="/medical" icon={Stethoscope} label={t("actions.newVisit")} />
            <QuickAction href="/medical" icon={FileText} label={t("actions.newCertificate")} />
            <QuickAction href="/employees" icon={Users} label={tNav("items.employees")} />
          </div>
        </CardContent>
      </Card>
    </>
  );
}

/* -------------------------------------------------------------------------- */
/* Employee (self-service)                                                    */
/* -------------------------------------------------------------------------- */

function EmployeeDashboard({ employeeId }: { employeeId: string | null }) {
  const t = useTranslations("dashboard");
  const tNav = useTranslations("navigation");
  const leaves = useEmployeeLeavesSafe(employeeId);
  const loans = useEmployeeLoansSafe(employeeId);
  const expenses = useEmployeeExpensesSafe(employeeId);
  const missions = useEmployeeMissionsSafe(employeeId);

  const myPending =
    (leaves.data?.filter((l) => l.status === "PENDING").length ?? 0) +
    (loans.data?.filter((l) => l.status === "PENDING").length ?? 0) +
    (expenses.data?.filter((e) => e.status === "SUBMITTED").length ?? 0);

  return (
    <>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard tone="orange" icon={CalendarDays} label={t("kpi.myLeaveBalance")} value="—" />
        <KpiCard
          tone="dark"
          icon={CheckCircle2}
          label={t("kpi.myPendingRequests")}
          value={myPending}
        />
        <KpiCard
          tone="amber"
          icon={Plane}
          label={t("kpi.missionsAssigned")}
          value={missions.isLoading ? "…" : (missions.data?.length ?? 0)}
        />
        <KpiCard tone="violet" icon={Wallet} label={tNav("items.payroll")} value="—" />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t("sections.myShortcuts")}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <QuickAction href="/leaves/my" icon={CalendarDays} label={t("actions.newLeave")} />
            <QuickAction href="/loans/my" icon={Coins} label={t("actions.newLoan")} />
            <QuickAction href="/expenses/my" icon={Receipt} label={t("actions.newExpense")} />
            <QuickAction href="/me/profile" icon={Users} label={t("actions.myProfile")} />
            <QuickAction href="/medical" icon={Stethoscope} label={tNav("items.medical")} />
            <QuickAction href="/trainings" icon={GraduationCap} label={tNav("items.trainings")} />
          </div>
        </CardContent>
      </Card>
    </>
  );
}

/* -------------------------------------------------------------------------- */
/* Shared primitives                                                          */
/* -------------------------------------------------------------------------- */

function Mini({
  label,
  value,
  tone,
  icon,
}: {
  label: string;
  value: number | undefined;
  tone: "green" | "blue" | "amber" | "red";
  icon?: React.ReactNode;
}) {
  const toneClass = {
    green: "bg-status-green-50 text-status-green-600",
    blue: "bg-status-blue-50 text-status-blue-600",
    amber: "bg-status-amber-50 text-status-amber-600",
    red: "bg-status-red-50 text-status-red-600",
  }[tone];
  return (
    <div className="rounded-[14px] border border-line bg-white p-3 shadow-elev-sm">
      <div className="flex items-center gap-1.5">
        <span className={`grid size-6 place-items-center rounded-md ${toneClass}`}>
          {icon ?? null}
        </span>
        <span className="text-[11px] font-semibold uppercase tracking-wider text-ink-3">
          {label}
        </span>
      </div>
      <div className="mt-1 font-display text-xl font-extrabold text-ink tabular">
        {value ?? 0}
      </div>
    </div>
  );
}

function QuickAction({
  href,
  icon: Icon,
  label,
}: {
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
}) {
  return (
    <Link
      href={href as never}
      className="group flex items-center gap-2.5 rounded-[14px] border border-line bg-white p-3 shadow-elev-sm transition-all hover:-translate-y-px hover:border-brand-300 hover:shadow-elev-md"
    >
      <span className="grid size-9 place-items-center rounded-lg bg-brand-50 text-brand-600">
        <Icon className="size-4" />
      </span>
      <span className="text-[13px] font-semibold text-ink group-hover:text-brand-600">
        {label}
      </span>
    </Link>
  );
}

/* Wrappers that short-circuit when employeeId is null (admin without employee). */
function useEmployeeLeavesSafe(id: string | null) {
  return useEmployeeLeaves(id ?? undefined);
}
function useEmployeeLoansSafe(id: string | null) {
  return useEmployeeLoans(id ?? undefined);
}
function useEmployeeExpensesSafe(id: string | null) {
  return useEmployeeExpenses(id ?? undefined);
}
function useEmployeeMissionsSafe(id: string | null) {
  return useEmployeeMissions(id ?? undefined);
}
