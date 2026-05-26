"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import {
  Plus,
  Users,
  AlertTriangle,
  Download,
  Search,
  SlidersHorizontal,
  ChevronLeft,
  ChevronRight,
  MoreHorizontal,
} from "lucide-react";
import { Link, useRouter } from "@/i18n/navigation";
import { PageHeader } from "@/components/shell/PageHeader";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge, type BadgeProps } from "@/components/ui/badge";
import { Avatar } from "@/components/ui/avatar";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { exportCsv } from "@/lib/csv";
import { cn } from "@/lib/utils";
import type { Employee } from "@/lib/types/hrm/employee";

const PAGE_SIZE = 12;

type AvatarTone = NonNullable<React.ComponentProps<typeof Avatar>["tone"]>;
const AVATAR_TONES: AvatarTone[] = ["orange", "blue", "green", "violet", "amber", "teal"];

function toneForName(name: string): AvatarTone {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = (hash * 31 + name.charCodeAt(i)) | 0;
  return AVATAR_TONES[Math.abs(hash) % AVATAR_TONES.length]!;
}

const CONTRACT_TONE: Record<string, BadgeProps["tone"]> = {
  CDI: "green",
  CDD: "blue",
  STAGE: "violet",
  INTERIM: "amber",
};

function monthsSince(iso: string): number {
  const start = new Date(iso);
  if (Number.isNaN(start.getTime())) return 0;
  const now = new Date();
  return (now.getFullYear() - start.getFullYear()) * 12 + (now.getMonth() - start.getMonth());
}

function seniorityLabel(iso: string): string {
  const m = monthsSince(iso);
  const years = Math.floor(m / 12);
  const months = m % 12;
  if (years <= 0 && months <= 0) return "< 1m";
  return `${years > 0 ? `${years}a ` : ""}${months}m`.trim();
}

function daysUntil(iso: string | null | undefined): number | null {
  if (!iso) return null;
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return null;
  return Math.ceil((d.getTime() - Date.now()) / 86_400_000);
}

export function EmployeesPageClient() {
  const t = useTranslations("employees");
  const router = useRouter();
  const { data, isLoading, isError, error, refetch } = useEmployees();

  const [query, setQuery] = React.useState("");
  const [dept, setDept] = React.useState<string | null>(null);
  const [showAllDepts, setShowAllDepts] = React.useState(false);
  const [page, setPage] = React.useState(0);

  const employees = React.useMemo(() => data ?? [], [data]);

  const stats = React.useMemo(() => {
    const total = employees.length;
    const active = employees.filter((e) => e.status === "ACTIVE").length;
    const cdi = employees.filter((e) => e.contractType === "CDI").length;
    const cdd = employees.filter(
      (e) => e.contractType === "CDD" || e.contractType === "STAGE" || e.contractType === "INTERIM",
    ).length;
    const endingSoon = employees.filter((e) => {
      const d = daysUntil(e.contractDateFin);
      return d != null && d >= 0 && d <= 90;
    }).length;
    const trial = employees.filter(
      (e) =>
        e.status === "ACTIVE" &&
        e.contractPeriodeEssai != null &&
        e.contractPeriodeEssai > 0 &&
        monthsSince(e.dateEmbauche) < e.contractPeriodeEssai,
    ).length;
    const cdiPct = total > 0 ? Math.round((cdi / total) * 100) : 0;
    return { total, active, cdi, cdd, endingSoon, trial, cdiPct };
  }, [employees]);

  const departments = React.useMemo(() => {
    const counts = new Map<string, number>();
    for (const e of employees) {
      const key = e.departmentCode ?? "__none__";
      counts.set(key, (counts.get(key) ?? 0) + 1);
    }
    return [...counts.entries()]
      .map(([code, count]) => ({ code, count }))
      .sort((a, b) => b.count - a.count);
  }, [employees]);

  const siteCount = React.useMemo(() => {
    const set = new Set<string>();
    for (const e of employees) set.add(e.agencyId ?? "__head__");
    return set.size;
  }, [employees]);

  const filtered = React.useMemo(() => {
    const q = query.trim().toLowerCase();
    return employees.filter((e) => {
      if (dept != null) {
        const key = e.departmentCode ?? "__none__";
        if (key !== dept) return false;
      }
      if (!q) return true;
      return (
        e.actorDisplayName.toLowerCase().includes(q) ||
        e.matricule.toLowerCase().includes(q) ||
        (e.departmentCode ?? "").toLowerCase().includes(q) ||
        (e.echelon ?? "").toLowerCase().includes(q)
      );
    });
  }, [employees, query, dept]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage = Math.min(page, pageCount - 1);
  const pageRows = filtered.slice(safePage * PAGE_SIZE, safePage * PAGE_SIZE + PAGE_SIZE);
  const rangeFrom = filtered.length === 0 ? 0 : safePage * PAGE_SIZE + 1;
  const rangeTo = Math.min(filtered.length, safePage * PAGE_SIZE + PAGE_SIZE);

  const visibleDepts = showAllDepts ? departments : departments.slice(0, 4);
  const hiddenDeptCount = departments.length - visibleDepts.length;

  function handleExport() {
    exportCsv(`employees-${new Date().toISOString().slice(0, 10)}`, filtered, [
      { header: t("table.matricule"), value: (e: Employee) => e.matricule },
      { header: t("table.name"), value: (e: Employee) => e.actorDisplayName },
      { header: t("columns.department"), value: (e: Employee) => e.departmentCode ?? "" },
      { header: t("columns.contract"), value: (e: Employee) => e.contractType ?? "" },
      { header: t("columns.status"), value: (e: Employee) => e.status },
      { header: t("table.hireDate"), value: (e: Employee) => e.dateEmbauche },
    ]);
  }

  return (
    <div className="animate-fade-up">
      <PageHeader
        crumbs={[{ label: t("list.eyebrow") }]}
        title={t("list.title")}
        subtitle={t("list.summary", {
          count: stats.total,
          departments: departments.filter((d) => d.code !== "__none__").length,
          sites: siteCount,
        })}
        actions={
          <>
            <Button variant="secondary" disabled={filtered.length === 0} onClick={handleExport}>
              <Download className="size-4" />
              {t("list.exportButton")}
            </Button>
            <Button asChild>
              <Link href="/employees/new">
                <Plus className="size-4" />
                {t("list.newButton")}
              </Link>
            </Button>
          </>
        }
      />

      {/* KPI band */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {isLoading ? (
          Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-[104px] rounded-[18px]" />)
        ) : (
          <>
            <StatCard
              tone="green"
              label={t("list.kpi.total")}
              value={stats.total}
              footer={t("list.kpi.totalFooter", { active: stats.active })}
            />
            <StatCard
              tone="orange"
              label={t("list.kpi.cdi")}
              value={stats.cdi}
              footer={t("list.kpi.cdiFooter", { pct: stats.cdiPct })}
            />
            <StatCard
              tone="amber"
              label={t("list.kpi.cdd")}
              value={stats.cdd}
              footer={t("list.kpi.cddFooter", { count: stats.endingSoon })}
            />
            <StatCard
              tone="blue"
              label={t("list.kpi.trial")}
              value={stats.trial}
              footer={t("list.kpi.trialFooter")}
            />
          </>
        )}
      </div>

      {isError && (
        <Card className="mt-5">
          <CardContent className="flex items-start gap-3">
            <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
            <div className="flex-1">
              <div className="font-semibold text-ink">Failed to load employees</div>
              <div className="text-sm text-ink-3">{(error as Error | null)?.message ?? "Unknown error"}</div>
            </div>
            <Button variant="secondary" onClick={() => refetch()}>
              Retry
            </Button>
          </CardContent>
        </Card>
      )}

      {!isError && (
        <Card className="mt-5 overflow-hidden p-0">
          {/* Filter bar */}
          <div className="flex flex-wrap items-center gap-2 border-b border-line-soft p-3">
            <div className="relative min-w-[220px] flex-1">
              <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-ink-4" />
              <input
                value={query}
                onChange={(e) => {
                  setQuery(e.target.value);
                  setPage(0);
                }}
                placeholder={t("list.filters.searchPlaceholder")}
                className="h-10 w-full rounded-xl border border-line bg-cream-soft/40 pl-9 pr-3 text-[13.5px] text-ink outline-none transition-colors placeholder:text-ink-4 focus:border-brand-300 focus:bg-white"
              />
            </div>
            <DeptChip
              label={`${t("list.filters.all")} (${employees.length})`}
              active={dept === null}
              onClick={() => {
                setDept(null);
                setPage(0);
              }}
            />
            {visibleDepts.map((d) => (
              <DeptChip
                key={d.code}
                label={`${d.code === "__none__" ? t("list.filters.none") : d.code} (${d.count})`}
                active={dept === d.code}
                onClick={() => {
                  setDept(dept === d.code ? null : d.code);
                  setPage(0);
                }}
              />
            ))}
            {hiddenDeptCount > 0 && (
              <button
                type="button"
                onClick={() => setShowAllDepts(true)}
                className="rounded-full px-3 py-1.5 text-[12.5px] font-semibold text-ink-3 hover:text-brand-600"
              >
                {t("list.filters.more", { count: hiddenDeptCount })}
              </button>
            )}
            <button
              type="button"
              className="ml-auto inline-flex items-center gap-1.5 rounded-xl border border-line bg-white px-3 py-2 text-[12.5px] font-semibold text-ink-2 hover:border-line-strong"
            >
              <SlidersHorizontal className="size-3.5" />
              {t("list.filters.moreFilters")}
            </button>
          </div>

          {isLoading ? (
            <div className="space-y-2 p-4">
              {Array.from({ length: 6 }).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : employees.length === 0 ? (
            <EmptyState t={t} />
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full border-collapse text-[13px]">
                  <thead>
                    <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                      <th className="px-4 py-2.5 text-left font-semibold">{t("list.columns.employee")}</th>
                      <th className="px-3 py-2.5 text-left font-semibold">{t("list.columns.poste")}</th>
                      <th className="px-3 py-2.5 text-left font-semibold">{t("list.columns.department")}</th>
                      <th className="px-3 py-2.5 text-left font-semibold">{t("list.columns.site")}</th>
                      <th className="px-3 py-2.5 text-left font-semibold">{t("list.columns.contract")}</th>
                      <th className="px-3 py-2.5 text-left font-semibold">{t("list.columns.status")}</th>
                      <th className="px-3 py-2.5 text-left font-semibold">{t("list.columns.seniority")}</th>
                      <th className="w-10 px-3 py-2.5" />
                    </tr>
                  </thead>
                  <tbody>
                    {pageRows.map((e) => (
                      <tr
                        key={e.id}
                        onClick={() => router.push(`/employees/${e.id}` as never)}
                        className="cursor-pointer border-b border-line-soft/70 transition-colors last:border-0 hover:bg-brand-50/40"
                      >
                        <td className="px-4 py-2.5">
                          <div className="flex items-center gap-2.5">
                            <Avatar
                              name={e.actorDisplayName}
                              tone={toneForName(e.actorDisplayName)}
                              size="default"
                            />
                            <div className="min-w-0 leading-tight">
                              <div className="truncate font-semibold text-ink">{e.actorDisplayName}</div>
                              <div className="truncate font-mono text-[11px] text-ink-4">{e.matricule}</div>
                            </div>
                          </div>
                        </td>
                        <td className="px-3 py-2.5 text-ink-2">
                          {e.poste ?? e.echelon ?? `Cat. ${e.categorie}`}
                        </td>
                        <td className="px-3 py-2.5">
                          {e.departmentCode ? (
                            <Badge tone="gray" withDot={false}>
                              {e.departmentCode}
                            </Badge>
                          ) : (
                            <span className="text-ink-4">—</span>
                          )}
                        </td>
                        <td className="px-3 py-2.5 text-ink-3">
                          {e.agencyId ? t("list.agency") : t("list.headOffice")}
                        </td>
                        <td className="px-3 py-2.5">
                          {e.contractType ? (
                            <Badge tone={CONTRACT_TONE[e.contractType] ?? "gray"}>{e.contractType}</Badge>
                          ) : (
                            <span className="text-[11px] text-ink-4">{t("list.noContract")}</span>
                          )}
                        </td>
                        <td className="px-3 py-2.5">
                          <StatusBadge kind="employee" status={e.status} />
                        </td>
                        <td className="px-3 py-2.5 tabular text-ink-2">{seniorityLabel(e.dateEmbauche)}</td>
                        <td className="px-3 py-2.5 text-right">
                          <span className="grid size-7 place-items-center rounded-lg text-ink-4 hover:bg-cream-soft hover:text-ink-2">
                            <MoreHorizontal className="size-4" />
                          </span>
                        </td>
                      </tr>
                    ))}
                    {pageRows.length === 0 && (
                      <tr>
                        <td colSpan={8} className="px-4 py-10 text-center text-sm text-ink-3">
                          {t("list.noResults")}
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <div className="flex items-center justify-between border-t border-line-soft px-4 py-3 text-[12.5px] text-ink-3">
                <span>{t("list.pagination.range", { from: rangeFrom, to: rangeTo, total: filtered.length })}</span>
                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    disabled={safePage === 0}
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    className="grid size-8 place-items-center rounded-lg border border-line bg-white text-ink-2 disabled:opacity-40 enabled:hover:border-line-strong"
                  >
                    <ChevronLeft className="size-4" />
                  </button>
                  <span className="px-1 tabular">
                    {safePage + 1} / {pageCount}
                  </span>
                  <button
                    type="button"
                    disabled={safePage >= pageCount - 1}
                    onClick={() => setPage((p) => Math.min(pageCount - 1, p + 1))}
                    className="grid size-8 place-items-center rounded-lg border border-line bg-white text-ink-2 disabled:opacity-40 enabled:hover:border-line-strong"
                  >
                    <ChevronRight className="size-4" />
                  </button>
                </div>
              </div>
            </>
          )}
        </Card>
      )}
    </div>
  );
}

function DeptChip({
  label,
  active,
  onClick,
}: {
  label: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "rounded-full px-3 py-1.5 text-[12.5px] font-semibold transition-colors",
        active
          ? "bg-grad-orange text-white shadow-brand"
          : "border border-line bg-white text-ink-2 hover:border-brand-300 hover:text-brand-700",
      )}
    >
      {label}
    </button>
  );
}

function EmptyState({ t }: { t: ReturnType<typeof useTranslations> }) {
  return (
    <div className="flex flex-col items-center gap-3 py-14 text-center">
      <div className="grid size-12 place-items-center rounded-2xl bg-brand-50 text-brand-600">
        <Users className="size-6" />
      </div>
      <div className="font-display text-lg font-bold text-ink">{t("list.empty")}</div>
      <div className="max-w-md text-sm text-ink-3">{t("list.emptySubtitle")}</div>
      <Button asChild className="mt-2">
        <Link href="/employees/new">
          <Plus className="size-4" />
          {t("list.newButton")}
        </Link>
      </Button>
    </div>
  );
}
