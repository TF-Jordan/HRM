"use client";

import { useTranslations } from "next-intl";
import { Users, Wallet, CalendarDays, AlertTriangle, Pause, Plus } from "lucide-react";
import { PageHeader } from "@/components/shell/PageHeader";
import { KpiCard } from "@/components/ui-tokens/KpiCard";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Link } from "@/i18n/navigation";
import { useHrmDashboard } from "@/hooks/modules/useDashboard";

export function DashboardClient({ displayName }: { displayName: string }) {
  const t = useTranslations("dashboard");
  const tNav = useTranslations("navigation");
  const tEmp = useTranslations("employees");
  const { data, isLoading } = useHrmDashboard();
  const hc = data?.headcount;

  return (
    <div className="space-y-7 animate-fade-up">
      <PageHeader
        ucBadge="UC-27"
        title={t("welcome", { name: displayName })}
        subtitle={t("subtitle")}
        actions={
          <Button asChild>
            <Link href="/employees/new">
              <Plus className="size-4" />
              {tEmp("list.newButton")}
            </Link>
          </Button>
        }
      />

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
              value="—"
              footer="Phase 5"
            />
            <KpiCard
              tone="amber"
              icon={CalendarDays}
              label={t("kpi.pendingLeaves")}
              value="—"
              footer="Phase 3"
            />
            <KpiCard
              tone="violet"
              icon={AlertTriangle}
              label={t("kpi.complianceAlerts")}
              value="—"
              footer="Phase 9"
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
              <Mini label="Suspendus" value={hc?.suspended} tone="amber" icon={<Pause className="size-3.5" />} />
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
            <p className="text-[13.5px] text-ink-3">
              Cette section sera enrichie au fil des phases (paie, congés, recrutement, formations…).
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

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
