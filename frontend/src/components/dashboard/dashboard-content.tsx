"use client";

import { AlertTriangle, CalendarRange, Sparkles, Users, Wallet } from "lucide-react";
import { useTranslations } from "next-intl";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { KpiCard } from "@/components/ui/kpi-card";

export function DashboardContent() {
  const tCommon = useTranslations("common");
  const { session } = useSession();
  if (!session) return null;
  const user = session.user;
  const workspace = session.workspace;

  return (
    <>
      <PageHeader
        ucBadge="UC-27"
        breadcrumb={[{ label: tCommon("appName") }, { label: "Dashboard" }]}
        title={<>Bonjour, {user.fullName.split(" ")[0] ?? user.fullName}</>}
        subtitle={
          workspace
            ? `${workspace.organizationName ?? workspace.organizationId} · ${user.roles[0] ?? "—"}`
            : user.roles[0] ?? "—"
        }
      />

      <div className="mb-8 grid grid-cols-4 gap-4">
        <KpiCard tone="orange" icon={Users} label="Effectif actif" value="—" foot="Phase 3 à venir" />
        <KpiCard tone="dark" icon={CalendarRange} label="Congés en attente" value="—" foot="Phase 4 à venir" />
        <KpiCard tone="amber" icon={Wallet} label="Masse salariale" value="—" foot="Phase 14 à venir" />
        <KpiCard tone="violet" icon={AlertTriangle} label="Alertes RH" value="—" foot="Phase 17 à venir" />
      </div>

      <Card>
        <CardContent padding="lg">
          <div className="flex items-start gap-4">
            <span className="grid h-12 w-12 place-items-center rounded-[14px] bg-orange-50 text-orange-600">
              <Sparkles className="h-6 w-6" />
            </span>
            <div>
              <h2 className="font-display text-[20px] font-bold tracking-tight text-ink">
                Phase 1 livrée — authentification fonctionnelle
              </h2>
              <p className="mt-1 max-w-[680px] text-[13.5px] text-ink-3">
                Vous êtes connecté en tant que <strong>{user.fullName}</strong> avec{" "}
                {user.permissions.length} permission(s) actives sur le workspace courant. Les modules
                fonctionnels (employés, paie, congés…) seront implémentés dans les phases suivantes.
              </p>
              <div className="mt-3 flex flex-wrap gap-1.5">
                {user.permissions.slice(0, 8).map((p) => (
                  <span
                    key={p}
                    className="rounded-md border border-line bg-bg-soft px-2 py-0.5 font-mono-tabular text-[10.5px] text-ink-3"
                  >
                    {p}
                  </span>
                ))}
                {user.permissions.length > 8 && (
                  <span className="rounded-md border border-line bg-bg-soft px-2 py-0.5 font-mono-tabular text-[10.5px] text-ink-3">
                    +{user.permissions.length - 8}
                  </span>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </>
  );
}
