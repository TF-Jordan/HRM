"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Check, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useOrgTimesheets, useValidateTimesheet } from "@/hooks/modules/useOrgTimesheets";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Avatar } from "@/components/ui/avatar";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

type TsStatus = "DRAFT" | "SUBMITTED" | "VALIDATED";

export function TimesheetsClient() {
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const [periode, setPeriode] = React.useState<string>(new Date().toISOString().slice(0, 7));
  const [filter, setFilter] = React.useState<TsStatus | null>(null);
  const list = useOrgTimesheets(periode || null);
  const employees = useEmployees();
  const validate = useValidateTimesheet();

  const empMap = React.useMemo(
    () => new Map((employees.data ?? []).map((e) => [e.id, e])),
    [employees.data],
  );

  const rows = React.useMemo(() => list.data ?? [], [list.data]);
  const filtered = filter ? rows.filter((r) => r.status === filter) : rows;

  const stats = React.useMemo(() => {
    const totalH = rows.reduce((s, r) => s + Number(r.totalHeures), 0);
    const supH = rows.reduce((s, r) => s + Number(r.totalHeuresSup), 0);
    const byStatus: Record<string, number> = {};
    for (const r of rows) byStatus[r.status] = (byStatus[r.status] ?? 0) + 1;
    return { totalH, supH, submitted: byStatus.SUBMITTED ?? 0, validated: byStatus.VALIDATED ?? 0, byStatus };
  }, [rows]);

  const chips: Array<{ key: TsStatus | null; label: string }> = [
    { key: null, label: `Tous (${rows.length})` },
    { key: "SUBMITTED", label: `À valider (${stats.byStatus.SUBMITTED ?? 0})` },
    { key: "VALIDATED", label: `Validées (${stats.byStatus.VALIDATED ?? 0})` },
    { key: "DRAFT", label: `Brouillons (${stats.byStatus.DRAFT ?? 0})` },
  ];

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        crumbs={[{ label: "Activité" }]}
        title="Temps & Présences"
        subtitle="Suivi des feuilles de temps, pointages et heures supplémentaires"
        actions={
          <Input
            type="month"
            value={periode}
            onChange={(e) => setPeriode(e.target.value)}
            className="max-w-[170px] tabular"
          />
        }
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {list.isLoading ? (
          Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-[104px] rounded-[18px]" />)
        ) : (
          <>
            <StatCard tone="orange" label="Feuilles" value={rows.length} footer="sur la période" />
            <StatCard tone="blue" label="Heures totales" value={`${fmt.number(stats.totalH)} h`} footer="travaillées" />
            <StatCard tone="amber" label="Heures sup." value={`${fmt.number(stats.supH)} h`} footer="cumulées" />
            <StatCard tone="green" label="À valider" value={stats.submitted} footer={`${stats.validated} validées`} />
          </>
        )}
      </div>

      <Card className="overflow-hidden p-0">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line-soft p-3">
          <h3 className="px-1 font-display text-[15px] font-bold text-ink">Pointages</h3>
          <div className="flex flex-wrap gap-2">
            {chips.map((c) => (
              <button
                key={c.label}
                type="button"
                onClick={() => setFilter(c.key)}
                className={cn(
                  "rounded-full px-3 py-1.5 text-[12.5px] font-semibold transition-colors",
                  filter === c.key
                    ? "bg-grad-orange text-white shadow-brand"
                    : "border border-line bg-white text-ink-2 hover:border-brand-300 hover:text-brand-700",
                )}
              >
                {c.label}
              </button>
            ))}
          </div>
        </div>

        {list.isLoading ? (
          <div className="space-y-2 p-4">
            {Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-12 w-full" />)}
          </div>
        ) : rows.length === 0 ? (
          <div className="py-10 text-center text-sm text-ink-3">Aucun pointage pour cette période</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-[13px]">
              <thead>
                <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                  <th className="px-4 py-2.5 text-left font-semibold">Employé</th>
                  <th className="px-3 py-2.5 text-left font-semibold">Période</th>
                  <th className="px-3 py-2.5 text-right font-semibold">Heures</th>
                  <th className="px-3 py-2.5 text-right font-semibold">H. supp.</th>
                  <th className="px-3 py-2.5 text-left font-semibold">Statut</th>
                  <th className="w-24 px-3 py-2.5" />
                </tr>
              </thead>
              <tbody>
                {filtered.map((t) => {
                  const emp = empMap.get(t.employeeId);
                  return (
                    <tr key={t.id} className="border-b border-line-soft/70 last:border-0">
                      <td className="px-4 py-2.5">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={emp?.actorDisplayName ?? "?"} size="sm" />
                          <div className="min-w-0">
                            <div className="truncate font-semibold text-ink">
                              {emp?.actorDisplayName ?? t.employeeId.slice(0, 8)}
                            </div>
                            <div className="font-mono text-[11px] text-ink-4">{emp?.matricule ?? ""}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-3 py-2.5 text-ink-2 tabular">{t.periode}</td>
                      <td className="px-3 py-2.5 text-right text-ink-2 tabular">{fmt.number(t.totalHeures)} h</td>
                      <td className="px-3 py-2.5 text-right text-ink-2 tabular">{fmt.number(t.totalHeuresSup)} h</td>
                      <td className="px-3 py-2.5">
                        <StatusBadge kind="timesheet" status={t.status} />
                      </td>
                      <td className="px-3 py-2.5 text-right">
                        {t.status === "SUBMITTED" && (
                          <Button
                            size="sm"
                            onClick={() =>
                              validate.mutate(t.id, {
                                onSuccess: () => toast.success("Pointage validé"),
                                onError: (err) => toast.error((err as Error).message),
                              })
                            }
                          >
                            {validate.isPending ? <Loader2 className="size-4 animate-spin" /> : <Check className="size-4" />}
                            Valider
                          </Button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
