"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Plus, Sparkles, Star } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { buildSkillAggregates, categoryTone, type SkillAggregate } from "@/lib/skill-aggregates";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  EmployeeSkillResponse,
  SkillResponse,
} from "@/server/ksm/modules/skills";

type Tab = "cartography" | "list" | "gap";

export function SkillsOverview() {
  const t = useTranslations("skills");
  const router = useRouter();
  const canCreate = useCan("hrm:skill:create");
  const [tab, setTab] = React.useState<Tab>("cartography");
  const [category, setCategory] = React.useState<string | "ALL">("ALL");

  const skillsQuery = useQuery({
    queryKey: ["hrm", "skills"],
    queryFn: () => apiFetch<SkillResponse[]>("/api/hrm/skills"),
    refetchInterval: 60_000,
  });
  const mappingsQuery = useQuery({
    queryKey: ["hrm", "skills", "employee-skills"],
    queryFn: () =>
      apiFetch<EmployeeSkillResponse[]>("/api/hrm/skills/employee-skills"),
    refetchInterval: 60_000,
  });
  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const skills = React.useMemo(() => skillsQuery.data ?? [], [skillsQuery.data]);
  const mappings = React.useMemo(
    () => mappingsQuery.data ?? [],
    [mappingsQuery.data],
  );
  const employees = employeesQuery.data ?? [];
  const nameOf = React.useCallback(
    (employeeId: string) => {
      const e = employees.find((x) => x.id === employeeId);
      return e?.actorDisplayName ?? e?.matricule ?? `${employeeId.slice(0, 8)}…`;
    },
    [employees],
  );

  const aggregates = React.useMemo(
    () => buildSkillAggregates(skills, mappings),
    [skills, mappings],
  );

  const categories = React.useMemo(() => {
    const m = new Map<string, number>();
    for (const s of skills) {
      const k = s.categorie ?? "—";
      m.set(k, (m.get(k) ?? 0) + 1);
    }
    return [...m.entries()];
  }, [skills]);

  const counts = React.useMemo(() => {
    const totalSkills = skills.length;
    const totalEvaluated = mappings.length;
    const expertsBySkill = new Map<string, number>();
    for (const m of mappings) {
      if (m.niveauActuel >= 5) {
        expertsBySkill.set(m.skillId, (expertsBySkill.get(m.skillId) ?? 0) + 1);
      }
    }
    const rareSkills = skills.filter((s) => (expertsBySkill.get(s.id) ?? 0) < 3).length;
    const avgScore =
      totalEvaluated === 0
        ? 0
        : mappings.reduce((acc, m) => acc + m.niveauActuel, 0) / totalEvaluated;
    return {
      totalSkills,
      totalEvaluated,
      rareSkills,
      avgScore: Math.round(avgScore * 10) / 10,
      ratio:
        totalEvaluated === 0 || employees.length === 0
          ? "0"
          : (totalEvaluated / employees.length).toFixed(1),
    };
  }, [skills, mappings, employees]);

  const filteredAggregates =
    category === "ALL" ? aggregates : aggregates.filter((a) => (a.skill.categorie ?? "—") === category);

  const cartography = [...filteredAggregates]
    .filter((a) => a.evaluatedCount > 0)
    .sort((a, b) => b.evaluatedCount - a.evaluatedCount)
    .slice(0, 8);

  const experts = React.useMemo(() => {
    const top = mappings.filter((m) => m.niveauActuel >= 5).slice(0, 6);
    return top.map((m) => {
      const skill = skills.find((s) => s.id === m.skillId);
      return {
        employeeId: m.employeeId,
        name: nameOf(m.employeeId),
        skillName: skill?.name ?? m.skillId.slice(0, 8),
        category: skill?.categorie ?? null,
        level: m.niveauActuel,
      };
    });
  }, [mappings, skills, nameOf]);

  const gap = React.useMemo(
    () => filteredAggregates.filter((a) => a.evaluatedCount > 0 && a.gap < 0),
    [filteredAggregates],
  );

  const isLoading = skillsQuery.isLoading || mappingsQuery.isLoading;
  const errored = skillsQuery.error || mappingsQuery.error;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Link href="/skills/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("new.title")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <div className="mb-5 grid grid-cols-4 gap-4">
        <Tile
          label={t("kpi.totalSkills")}
          value={counts.totalSkills}
          sub={t("kpiSub.categories", { count: categories.length })}
          tone="bg-orange-500"
        />
        <Tile
          label={t("kpi.totalEvaluated")}
          value={counts.totalEvaluated}
          sub={t("kpiSub.perEmployee", { ratio: counts.ratio })}
          tone="bg-info-500"
        />
        <Tile
          label={t("kpi.rareSkills")}
          value={counts.rareSkills}
          sub={t("kpiSub.lessThan3")}
          tone="bg-danger-500"
        />
        <Tile
          label={t("kpi.averageScore")}
          value={counts.totalEvaluated === 0 ? "—" : `${counts.avgScore} / 5`}
          sub={t("kpiSub.scale")}
          tone="bg-success-500"
        />
      </div>

      {/* Category chips */}
      <div className="mb-4 flex flex-wrap items-center gap-2">
        <button
          type="button"
          onClick={() => setCategory("ALL")}
          className={cn(
            "rounded-full px-3 py-1.5 text-[12px] font-semibold transition-colors",
            category === "ALL"
              ? "bg-grad-orange text-white shadow-orange-brand"
              : "border border-line bg-white text-ink-2 hover:bg-bg-soft",
          )}
        >
          {t("filters.all")} ({counts.totalSkills})
        </button>
        {categories.map(([name, count]) => (
          <button
            key={name}
            type="button"
            onClick={() => setCategory(name)}
            className={cn(
              "rounded-full px-3 py-1.5 text-[12px] font-semibold transition-colors",
              category === name
                ? "bg-grad-orange text-white shadow-orange-brand"
                : "border border-line bg-white text-ink-2 hover:bg-bg-soft",
            )}
          >
            {name === "—" ? t("filters.uncategorized") : name} ({count})
          </button>
        ))}
      </div>

      {/* Tab toggle */}
      <div className="mb-4 flex w-fit items-center gap-1 rounded-full border border-line bg-white p-1 shadow-xs-brand">
        {(["cartography", "list", "gap"] as Tab[]).map((k) => (
          <button
            key={k}
            type="button"
            onClick={() => setTab(k)}
            className={cn(
              "rounded-full px-3.5 py-1.5 text-[12.5px] font-semibold transition-colors",
              tab === k
                ? "bg-grad-orange text-white shadow-orange-brand"
                : "text-ink-2 hover:bg-bg-soft",
            )}
          >
            {t(`skillTabs.${k}`)}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
        </div>
      ) : errored ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {errored instanceof BffApiError ? errored.message : "—"}
        </div>
      ) : tab === "cartography" ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-[2fr_1fr]">
          <Card>
            <CardContent padding="lg">
              <div className="mb-3">
                <div className="text-[14px] font-bold tracking-tight text-ink">
                  {t("cartography.title")}
                </div>
                <div className="text-[12px] text-ink-3">{t("cartography.subtitle")}</div>
              </div>
              {cartography.length === 0 ? (
                <p className="rounded-[12px] border border-dashed border-line bg-bg-soft px-4 py-6 text-center text-[13px] text-ink-3">
                  {t("cartography.empty")}
                </p>
              ) : (
                <div className="flex flex-col gap-4">
                  {cartography.map((a) => (
                    <button
                      key={a.skill.id}
                      type="button"
                      onClick={() => router.push(`/skills/${a.skill.id}`)}
                      className="text-left"
                    >
                      <div className="mb-1.5 flex flex-wrap items-center gap-2">
                        <span className="text-[13.5px] font-semibold text-ink">
                          {a.skill.name}
                        </span>
                        {a.skill.categorie && (
                          <Badge tone={categoryTone(a.skill.categorie)}>{a.skill.categorie}</Badge>
                        )}
                        <span className="ml-auto text-[12px] text-ink-3">
                          <b className="text-ink-2">{a.evaluatedCount}</b>{" "}
                          {t("cartography.employeesShort", { count: a.evaluatedCount })} ·{" "}
                          {t("cartography.level")}{" "}
                          <b className="text-orange-600">
                            {a.avgActual} / 5
                          </b>
                        </span>
                      </div>
                      <LevelBar level={a.avgActual} />
                    </button>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardContent padding="lg">
              <div className="mb-4 flex items-center gap-2 text-[14px] font-bold tracking-tight text-ink">
                <Star className="h-4 w-4 fill-orange-500 text-orange-500" />
                {t("experts.title")}
              </div>
              {experts.length === 0 ? (
                <p className="rounded-[12px] border border-dashed border-line bg-bg-soft px-4 py-6 text-center text-[12.5px] text-ink-3">
                  {t("experts.empty")}
                </p>
              ) : (
                <div className="flex flex-col divide-y divide-line-soft">
                  {experts.map((e, idx) => (
                    <div key={`${e.employeeId}-${idx}`} className="flex items-center gap-3 py-2.5">
                      <Avatar name={e.name} size="sm" />
                      <div className="min-w-0 flex-1">
                        <div className="truncate text-[12.5px] font-semibold text-ink">{e.name}</div>
                        <div className="truncate text-[11px] text-ink-3">{e.skillName}</div>
                      </div>
                      <div className="flex items-center gap-0.5">
                        {[1, 2, 3, 4, 5].map((i) => (
                          <Star
                            key={i}
                            className={cn(
                              "h-3 w-3",
                              i <= e.level
                                ? "fill-orange-500 text-orange-500"
                                : "fill-bg-soft text-bg-soft",
                            )}
                          />
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      ) : tab === "list" ? (
        <Card>
          {filteredAggregates.length === 0 ? (
            <CardContent padding="lg">
              <p className="text-center text-[13px] text-ink-3">{t("list.empty")}</p>
            </CardContent>
          ) : (
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-bg-dim">
                  <Th>{t("list.columns.name")}</Th>
                  <Th>{t("list.columns.category")}</Th>
                  <Th className="text-right">{t("list.columns.evaluated")}</Th>
                  <Th>{t("list.columns.average")}</Th>
                </tr>
              </thead>
              <tbody>
                {filteredAggregates.map((a) => (
                  <tr
                    key={a.skill.id}
                    className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                    onClick={() => router.push(`/skills/${a.skill.id}`)}
                  >
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-2">
                        <Sparkles className="h-3.5 w-3.5 text-orange-500" />
                        <span className="text-[13.5px] font-semibold text-ink">{a.skill.name}</span>
                      </div>
                    </td>
                    <td className="px-3 py-3">
                      {a.skill.categorie ? (
                        <Badge tone={categoryTone(a.skill.categorie)}>{a.skill.categorie}</Badge>
                      ) : (
                        <span className="text-ink-4">—</span>
                      )}
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-bold text-ink">
                      {a.evaluatedCount}
                    </td>
                    <td className="px-3 py-3">
                      {a.evaluatedCount === 0 ? (
                        <span className="text-ink-4">—</span>
                      ) : (
                        <div className="flex w-40 items-center gap-2">
                          <LevelBar level={a.avgActual} compact />
                          <span className="font-mono-tabular text-[12.5px] font-semibold text-ink-2">
                            {a.avgActual}
                          </span>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      ) : (
        <Card>
          {gap.length === 0 ? (
            <CardContent padding="lg">
              <p className="text-center text-[13px] text-ink-3">{t("gap.empty")}</p>
            </CardContent>
          ) : (
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-bg-dim">
                  <Th>{t("gap.columns.skill")}</Th>
                  <Th>{t("gap.columns.category")}</Th>
                  <Th className="text-right">{t("gap.columns.target")}</Th>
                  <Th className="text-right">{t("gap.columns.actual")}</Th>
                  <Th>{t("gap.columns.gap")}</Th>
                  <Th className="text-right">{t("gap.columns.evaluated")}</Th>
                </tr>
              </thead>
              <tbody>
                {gap.map((a) => (
                  <tr
                    key={a.skill.id}
                    className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                    onClick={() => router.push(`/skills/${a.skill.id}`)}
                  >
                    <td className="px-5 py-3 text-[13.5px] font-semibold text-ink">{a.skill.name}</td>
                    <td className="px-3 py-3">
                      {a.skill.categorie ? (
                        <Badge tone={categoryTone(a.skill.categorie)}>{a.skill.categorie}</Badge>
                      ) : (
                        <span className="text-ink-4">—</span>
                      )}
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">
                      {a.avgTarget}
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">
                      {a.avgActual}
                    </td>
                    <td className="px-3 py-3">
                      <Badge tone={a.gap < 0 ? "danger" : a.gap === 0 ? "gray" : "success"}>
                        {a.gap > 0 ? `+${a.gap}` : a.gap.toFixed(1)}
                      </Badge>
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">
                      {a.evaluatedCount}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      )}
    </>
  );
}

function Tile({
  label,
  value,
  sub,
  tone,
}: {
  label: string;
  value: React.ReactNode;
  sub: React.ReactNode;
  tone: string;
}) {
  return (
    <div className="relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand">
      <div className="flex items-center justify-between">
        <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">{label}</span>
        <span className={cn("inline-block h-2 w-2 rounded-full", tone)} />
      </div>
      <div className="font-display font-mono-tabular text-[24px] font-extrabold tracking-tight text-ink">
        {value}
      </div>
      <div className="text-[11px] text-ink-3">{sub}</div>
    </div>
  );
}

function Th({ children, className }: { children?: React.ReactNode; className?: string }) {
  return (
    <th
      className={cn(
        "px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3 first:pl-5 last:pr-5",
        className,
      )}
    >
      {children}
    </th>
  );
}

function LevelBar({ level, compact }: { level: number; compact?: boolean }) {
  const floor = Math.floor(level);
  const partial = level - floor;
  return (
    <div className={cn("flex gap-1", compact ? "h-2" : "h-4")}>
      {[1, 2, 3, 4, 5].map((i) => (
        <div
          key={i}
          className={cn(
            "flex-1 rounded-[3px]",
            i <= floor
              ? "bg-orange-500"
              : i === floor + 1 && partial > 0
                ? "bg-orange-200"
                : "bg-bg-soft",
          )}
        />
      ))}
    </div>
  );
}

export type { SkillAggregate };
