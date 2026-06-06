"use client";

import { useQuery } from "@tanstack/react-query";
import {
  ArrowUpRight,
  CheckCircle2,
  Gauge,
  Loader2,
  Sparkles,
  Target,
  TrendingUp,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { categoryTone } from "@/lib/skill-aggregates";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { EmployeeSkillView } from "@/server/ksm/modules/skills";

type MinePayload = { employee: EmployeeResponse | null; skills: EmployeeSkillView[] };

const SCALE = 5;

export function MySkills() {
  const t = useTranslations("skills");
  const locale = useLocale() as "fr" | "en";

  const query = useQuery({
    queryKey: ["hrm", "skills", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/skills/mine"),
    refetchInterval: 60_000,
  });

  const skills = React.useMemo(() => query.data?.skills ?? [], [query.data?.skills]);

  const stats = React.useMemo(() => {
    if (skills.length === 0) {
      return { count: 0, avg: 0, mastered: 0, gaps: 0, readiness: 0 };
    }
    const avg = skills.reduce((s, k) => s + k.niveauActuel, 0) / skills.length;
    const mastered = skills.filter((k) => k.niveauActuel >= k.niveauAttendu).length;
    const gaps = skills.filter((k) => k.niveauActuel < k.niveauAttendu).length;
    return {
      count: skills.length,
      avg: Math.round(avg * 10) / 10,
      mastered,
      gaps,
      readiness: Math.round((mastered / skills.length) * 100),
    };
  }, [skills]);

  // Group by category for the competency matrix.
  const byCategory = React.useMemo(() => {
    const m = new Map<string, EmployeeSkillView[]>();
    for (const k of skills) {
      const key = k.skillCategory ?? "—";
      const list = m.get(key) ?? [];
      list.push(k);
      m.set(key, list);
    }
    return [...m.entries()].map(([category, items]) => ({
      category,
      items: [...items].sort((a, b) => a.skillName.localeCompare(b.skillName)),
    }));
  }, [skills]);

  const gapList = React.useMemo(
    () =>
      skills
        .filter((k) => k.niveauActuel < k.niveauAttendu)
        .sort(
          (a, b) =>
            b.niveauAttendu - b.niveauActuel - (a.niveauAttendu - a.niveauActuel),
        ),
    [skills],
  );

  const lastEval = React.useMemo(() => {
    const dates = skills
      .map((k) => k.dateEvaluation)
      .filter((d): d is string => !!d)
      .sort((a, b) => b.localeCompare(a));
    return dates[0] ?? null;
  }, [skills]);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : !query.data.employee ? (
        <Card>
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("mine.noEmployee")}</p>
          </CardContent>
        </Card>
      ) : skills.length === 0 ? (
        <Card>
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("mine.empty")}</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-6">
          <Hero
            avg={stats.avg}
            readiness={stats.readiness}
            mastered={stats.mastered}
            total={stats.count}
            lastEval={lastEval}
            locale={locale}
          />

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Kpi icon={Sparkles} label={t("mine.kpi.count")} value={String(stats.count)} tone="text-ink" />
            <Kpi icon={Gauge} label={t("mine.kpi.avg")} value={`${stats.avg}/${SCALE}`} tone="text-orange-600" />
            <Kpi
              icon={CheckCircle2}
              label={t("mine.kpi.mastered")}
              value={String(stats.mastered)}
              tone="text-success-600"
            />
            <Kpi
              icon={Target}
              label={t("mine.kpi.gaps")}
              value={String(stats.gaps)}
              tone={stats.gaps > 0 ? "text-warning-600" : "text-ink-3"}
            />
          </div>

          {gapList.length > 0 && <GapPanel skills={gapList} />}

          <section>
            <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
              <Sparkles className="h-3.5 w-3.5 text-orange-500" />
              {t("mine.matrixTitle")}
            </h2>
            <div className="space-y-4">
              {byCategory.map((group) => (
                <Card key={group.category}>
                  <CardContent padding="lg">
                    <div className="mb-3 flex items-center gap-2">
                      <Badge tone={categoryTone(group.category === "—" ? null : group.category)}>
                        {group.category === "—" ? t("filters.uncategorized") : group.category}
                      </Badge>
                      <span className="text-[11.5px] text-ink-4">
                        {t("mine.skillCount", { count: group.items.length })}
                      </span>
                    </div>
                    <div className="space-y-3.5">
                      {group.items.map((k) => (
                        <SkillRow key={k.id} skill={k} />
                      ))}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </section>

          <p className="flex items-start gap-1.5 text-[12px] text-ink-3">
            <Target className="mt-0.5 h-3.5 w-3.5 shrink-0 text-orange-500" />
            {t("mine.assessedHint")}
          </p>
        </div>
      )}
    </>
  );
}

/* ----------------------------------------------------------------- Hero */
function Hero({
  avg,
  readiness,
  mastered,
  total,
  lastEval,
  locale,
}: {
  avg: number;
  readiness: number;
  mastered: number;
  total: number;
  lastEval: string | null;
  locale: "fr" | "en";
}) {
  const t = useTranslations("skills");
  return (
    <div className="overflow-hidden rounded-[22px] border border-line bg-gradient-to-br from-ink to-[#23314d] text-white shadow-sm">
      <div className="grid gap-6 p-6 md:grid-cols-[1.3fr_1fr] md:p-7">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-[12px] font-medium uppercase tracking-wide text-white/60">
            <Gauge className="h-4 w-4" />
            {t("mine.hero.proficiency")}
          </div>
          <div className="flex items-end gap-3">
            <span className="font-display font-mono-tabular text-[44px] font-extrabold leading-none">
              {avg.toFixed(1)}
            </span>
            <span className="pb-1 text-[14px] text-white/50">/ {SCALE}</span>
          </div>
          <div className="max-w-sm">
            <div className="mb-1 flex items-center justify-between text-[12px] text-white/70">
              <span>{t("mine.hero.readiness")}</span>
              <span className="font-mono-tabular font-bold text-white">{readiness}%</span>
            </div>
            <div className="h-2 overflow-hidden rounded-full bg-white/15">
              <div className="h-full rounded-full bg-grad-orange" style={{ width: `${readiness}%` }} />
            </div>
            <p className="mt-1.5 text-[11.5px] text-white/55">
              {t("mine.hero.readinessHint", { mastered, total })}
            </p>
          </div>
          {lastEval && (
            <p className="text-[12px] text-white/60">
              {t("mine.hero.lastEval")} · {formatDate(lastEval, { locale })}
            </p>
          )}
        </div>

        <div className="flex items-center justify-center rounded-[16px] bg-white/5 p-5">
          <div className="text-center">
            <p className="font-mono-tabular text-[40px] font-bold leading-none">{total}</p>
            <p className="mt-1.5 text-[12.5px] text-white/60">{t("mine.hero.skillsAssessed")}</p>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ----------------------------------------------------------------- KPI */
function Kpi({
  icon: Icon,
  label,
  value,
  tone,
}: {
  icon: typeof Gauge;
  label: string;
  value: string;
  tone: string;
}) {
  return (
    <Card>
      <CardContent padding="md">
        <div className="flex items-center justify-between">
          <span className="text-[12.5px] font-medium text-ink-3">{label}</span>
          <Icon className={cn("h-4 w-4", tone)} />
        </div>
        <p className={cn("mt-2 font-mono-tabular text-[22px] font-bold", tone)}>{value}</p>
      </CardContent>
    </Card>
  );
}

/* ----------------------------------------------------------------- Gap panel */
function GapPanel({ skills }: { skills: EmployeeSkillView[] }) {
  const t = useTranslations("skills");
  return (
    <Card>
      <CardContent padding="lg">
        <h3 className="mb-1 flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
          <TrendingUp className="h-4 w-4 text-orange-500" />
          {t("mine.gapTitle")}
        </h3>
        <p className="mb-4 text-[12px] text-ink-3">{t("mine.gapSubtitle")}</p>
        <div className="space-y-3">
          {skills.map((k) => {
            const gap = k.niveauAttendu - k.niveauActuel;
            return (
              <div key={k.id} className="flex items-center gap-3">
                <div className="min-w-0 flex-1">
                  <div className="truncate text-[13px] font-semibold text-ink">{k.skillName}</div>
                  {k.skillCategory && (
                    <div className="text-[10.5px] text-ink-4">{k.skillCategory}</div>
                  )}
                </div>
                <LevelBar current={k.niveauActuel} target={k.niveauAttendu} />
                <Badge tone="warning">−{gap}</Badge>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}

/* ----------------------------------------------------------------- Skill row */
function SkillRow({ skill }: { skill: EmployeeSkillView }) {
  const t = useTranslations("skills");
  const meets = skill.niveauActuel >= skill.niveauAttendu;
  return (
    <div className="flex items-center gap-3">
      <div className="min-w-0 flex-[1.4]">
        <div className="truncate text-[13.5px] font-semibold text-ink">{skill.skillName}</div>
      </div>
      <LevelBar current={skill.niveauActuel} target={skill.niveauAttendu} />
      <div className="flex w-[120px] shrink-0 items-center justify-end gap-2">
        <span className="font-mono-tabular text-[12.5px] font-bold text-ink">
          {skill.niveauActuel}
          <span className="text-[11px] font-normal text-ink-4">/{skill.niveauAttendu}</span>
        </span>
        {meets ? (
          <Badge tone="success">{t("mine.met")}</Badge>
        ) : (
          <Badge tone="warning">
            <ArrowUpRight className="h-3 w-3" />
            {skill.niveauAttendu - skill.niveauActuel}
          </Badge>
        )}
      </div>
    </div>
  );
}

/** Five-segment level bar: filled = current, hollow outline marks the expected target. */
function LevelBar({ current, target }: { current: number; target: number }) {
  return (
    <div className="hidden flex-1 gap-1 sm:flex" style={{ maxWidth: 200 }}>
      {[1, 2, 3, 4, 5].map((i) => {
        const filled = i <= current;
        const isTarget = i === target;
        return (
          <div
            key={i}
            className={cn(
              "h-3.5 flex-1 rounded-[3px]",
              filled ? "bg-orange-500" : "bg-bg-soft",
              isTarget && !filled && "ring-2 ring-inset ring-orange-300",
              isTarget && filled && "ring-2 ring-inset ring-orange-700",
            )}
          />
        );
      })}
    </div>
  );
}
