import { setRequestLocale, getTranslations } from "next-intl/server";
import { Users, Wallet, CalendarDays, AlertTriangle } from "lucide-react";
import { PageHeader } from "@/components/shell/PageHeader";
import { KpiCard } from "@/components/ui-tokens/KpiCard";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { requireSession } from "@/server/session";

export default async function DashboardPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const session = await requireSession();
  const t = await getTranslations("dashboard");

  return (
    <div className="space-y-7 animate-fade-up">
      <PageHeader
        ucBadge="UC-27"
        title={t("welcome", { name: session.user.displayName })}
        subtitle={t("subtitle")}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          tone="orange"
          icon={Users}
          label={t("kpi.activeEmployees")}
          value="—"
          footer={<Badge tone="gray" withDot={false}>Phase 1</Badge>}
        />
        <KpiCard
          tone="dark"
          icon={Wallet}
          label={t("kpi.monthlyPayroll")}
          value="—"
          footer={<Badge tone="gray" withDot={false}>Phase 1</Badge>}
        />
        <KpiCard
          tone="amber"
          icon={CalendarDays}
          label={t("kpi.pendingLeaves")}
          value="—"
          footer={<Badge tone="gray" withDot={false}>Phase 1</Badge>}
        />
        <KpiCard
          tone="violet"
          icon={AlertTriangle}
          label={t("kpi.complianceAlerts")}
          value="—"
          footer={<Badge tone="gray" withDot={false}>Phase 1</Badge>}
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t("sections.quickActions")}</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-[13.5px] text-ink-3">
            Cette page sera enrichie lors des phases suivantes avec des données réelles KSM.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
