"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  AlarmClock,
  CalendarClock,
  CheckCircle2,
  Clock,
  Loader2,
  Moon,
  Plus,
  Send,
  TrendingUp,
  UserX,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { PeriodPicker } from "@/components/timesheets/period-picker";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatPeriod } from "@/lib/format";
import { timesheetStatusTone } from "@/lib/timesheet-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { TimesheetResponse, TimesheetStatus } from "@/server/ksm/modules/timesheets";

type MinePayload = {
  employee: EmployeeResponse | null;
  periode: string;
  timesheets: TimesheetResponse[];
  history: TimesheetResponse[];
};

type PeriodAgg = {
  periode: string;
  normales: number;
  supplementaires: number;
  nuit: number;
  weekend: number;
  absences: number;
  total: number;
  status: TimesheetStatus;
  primaryId: string;
  draftId: string | null;
};

const STATUS_RANK: Record<TimesheetStatus, number> = { DRAFT: 0, SUBMITTED: 1, VALIDATED: 2 };

/** Legal overtime multipliers (Cameroon Labour Code) — mirrors payroll-core OT_DAY/NIGHT/SUNDAY. */
const OT_MULTIPLIER = { day: "+25%", night: "+50%", weekend: "+75%" } as const;

function currentPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

const n = (v: number | string) => Number(v ?? 0);

function aggregate(periode: string, sheets: TimesheetResponse[]): PeriodAgg | null {
  if (sheets.length === 0) return null;
  const acc = sheets.reduce(
    (a, ts) => ({
      normales: a.normales + n(ts.heuresNormales),
      supplementaires: a.supplementaires + n(ts.heuresSupplementaires),
      nuit: a.nuit + n(ts.heuresNuit),
      weekend: a.weekend + n(ts.heuresWeekend),
      absences: a.absences + n(ts.absencesNonJustifiees),
    }),
    { normales: 0, supplementaires: 0, nuit: 0, weekend: 0, absences: 0 },
  );
  const status = sheets.reduce<TimesheetStatus>(
    (best, ts) => (STATUS_RANK[ts.status] > STATUS_RANK[best] ? ts.status : best),
    "DRAFT",
  );
  const draft = sheets.find((ts) => ts.status === "DRAFT") ?? null;
  return {
    periode,
    ...acc,
    total: acc.normales + acc.supplementaires + acc.nuit + acc.weekend,
    status,
    primaryId: (draft ?? sheets[0]).id,
    draftId: draft?.id ?? null,
  };
}

export function MyTimesheets() {
  const t = useTranslations("timesheets");
  const tMy = useTranslations("timesheets.my");
  const tHours = useTranslations("timesheets.hours");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const queryClient = useQueryClient();
  const [periode, setPeriode] = React.useState(currentPeriode());

  const query = useQuery({
    queryKey: ["hrm", "timesheets", "mine", periode],
    queryFn: () =>
      apiFetch<MinePayload>(`/api/hrm/timesheets/mine?periode=${encodeURIComponent(periode)}`),
  });

  const submitMutation = useMutation({
    mutationFn: (id: string) =>
      apiFetch<TimesheetResponse>(`/api/hrm/timesheets/${id}/submit`, { method: "POST" }),
    onSuccess: () => {
      toast.success(tMy("hero.submitSuccess"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "timesheets", "mine"] });
    },
    onError: (cause) =>
      toast.error(cause instanceof BffApiError ? cause.message : "Error"),
  });

  const history = React.useMemo(() => {
    const byPeriod = new Map<string, TimesheetResponse[]>();
    for (const ts of query.data?.history ?? []) {
      const list = byPeriod.get(ts.periode) ?? [];
      list.push(ts);
      byPeriod.set(ts.periode, list);
    }
    return Array.from(byPeriod.entries())
      .map(([p, list]) => aggregate(p, list)!)
      .sort((a, b) => a.periode.localeCompare(b.periode));
  }, [query.data?.history]);

  const current = React.useMemo(
    () => aggregate(periode, query.data?.timesheets ?? []),
    [periode, query.data?.timesheets],
  );

  const fmtH = (v: number) => `${v.toFixed(1)} ${tMy("units.hours")}`;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tMy("title") }]}
        title={tMy("title")}
        subtitle={tMy("subtitle")}
        actions={
          <div className="flex items-center gap-2">
            <PeriodPicker value={periode} onChange={setPeriode} maxPeriode={currentPeriode()} />
            {query.data?.employee && (
              <Link href={`/timesheets/new?periode=${periode}`}>
                <Button>
                  <Plus className="h-4 w-4" />
                  {tMy("new")}
                </Button>
              </Link>
            )}
          </div>
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "Failed"}
        </div>
      ) : !query.data.employee ? (
        <Card>
          <CardContent padding="lg">
            <div className="flex items-start gap-4">
              <span className="grid h-12 w-12 place-items-center rounded-[14px] bg-warning-50 text-warning-600">
                <UserX className="h-6 w-6" />
              </span>
              <p className="text-[14px] text-ink-2">{tMy("noEmployee")}</p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-6">
          <Hero
            periode={periode}
            locale={locale}
            current={current}
            tMy={tMy}
            t={t}
            fmtH={fmtH}
            submitting={submitMutation.isPending}
            onSubmit={(id) => submitMutation.mutate(id)}
          />

          {current && current.total + current.absences > 0 ? (
            <>
              <KpiRow current={current} tMy={tMy} fmtH={fmtH} />

              <div className="grid gap-6 lg:grid-cols-2">
                <Composition current={current} tMy={tMy} tHours={tHours} fmtH={fmtH} />
                <PayImpact current={current} tMy={tMy} fmtH={fmtH} />
              </div>
            </>
          ) : null}

          <Trend history={history} periode={periode} locale={locale} tMy={tMy} fmtH={fmtH} />

          <HistoryTable
            history={history}
            locale={locale}
            tMy={tMy}
            t={t}
            onRow={(id) => router.push(`/timesheets/${id}`)}
          />
        </div>
      )}
    </>
  );
}

/* ----------------------------------------------------------------- Hero */

function Hero({
  periode,
  locale,
  current,
  tMy,
  t,
  fmtH,
  submitting,
  onSubmit,
}: {
  periode: string;
  locale: "fr" | "en";
  current: PeriodAgg | null;
  tMy: ReturnType<typeof useTranslations<"timesheets.my">>;
  t: ReturnType<typeof useTranslations<"timesheets">>;
  fmtH: (v: number) => string;
  submitting: boolean;
  onSubmit: (id: string) => void;
}) {
  const periodLabel = formatPeriod(periode, locale);
  const hint =
    current == null
      ? tMy("hero.noEntryHint")
      : current.status === "DRAFT"
        ? tMy("hero.draftHint")
        : current.status === "SUBMITTED"
          ? tMy("hero.submittedHint")
          : tMy("hero.validatedHint");

  return (
    <div className="overflow-hidden rounded-[22px] border border-line bg-gradient-to-br from-ink to-[#23314d] text-white shadow-sm">
      <div className="grid gap-6 p-6 md:grid-cols-[1.3fr_1fr] md:p-7">
        <div className="space-y-4">
          <div className="flex items-center gap-2 text-[12.5px] font-medium uppercase tracking-wide text-white/60">
            <CalendarClock className="h-4 w-4" />
            <span className="capitalize">{periodLabel}</span>
          </div>

          {current == null ? (
            <div>
              <p className="text-[22px] font-semibold capitalize">
                {tMy("hero.noEntry", { period: periodLabel })}
              </p>
              <p className="mt-1 text-[13.5px] text-white/70">{tMy("hero.noEntryHint")}</p>
            </div>
          ) : (
            <div>
              <p className="text-[12.5px] uppercase tracking-wide text-white/55">
                {tMy("hero.label")}
              </p>
              <p className="mt-1 font-mono-tabular text-[40px] font-bold leading-none">
                {current.total.toFixed(1)}
                <span className="ml-1 text-[18px] font-medium text-white/60">
                  {tMy("units.hours")}
                </span>
              </p>
              <div className="mt-3 flex flex-wrap items-center gap-2">
                <Badge tone={timesheetStatusTone(current.status)}>
                  {t(`status.${current.status}`)}
                </Badge>
                {current.supplementaires > 0 && (
                  <span className="inline-flex items-center gap-1 rounded-full bg-white/10 px-2.5 py-1 text-[12px] text-white/85">
                    <AlarmClock className="h-3.5 w-3.5" />
                    {fmtH(current.supplementaires)} sup.
                  </span>
                )}
              </div>
            </div>
          )}

          <p className="text-[13px] text-white/70">{hint}</p>

          {current?.draftId && (
            <Button
              variant="secondary"
              onClick={() => onSubmit(current.draftId!)}
              disabled={submitting}
            >
              {submitting ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Send className="h-4 w-4" />
              )}
              {tMy("hero.submit")}
            </Button>
          )}
        </div>

        <WorkflowStepper status={current?.status ?? null} t={t} />
      </div>
    </div>
  );
}

function WorkflowStepper({
  status,
  t,
}: {
  status: TimesheetStatus | null;
  t: ReturnType<typeof useTranslations<"timesheets">>;
}) {
  const steps: TimesheetStatus[] = ["DRAFT", "SUBMITTED", "VALIDATED"];
  const rank = status ? STATUS_RANK[status] : -1;
  return (
    <div className="flex flex-col justify-center gap-3 rounded-[16px] bg-white/5 p-4">
      {steps.map((step, i) => {
        const done = rank >= STATUS_RANK[step];
        const active = rank === STATUS_RANK[step];
        return (
          <div key={step} className="flex items-center gap-3">
            <span
              className={cn(
                "grid h-7 w-7 shrink-0 place-items-center rounded-full text-[12px] font-semibold",
                done ? "bg-orange-500 text-white" : "bg-white/10 text-white/50",
              )}
            >
              {done ? <CheckCircle2 className="h-4 w-4" /> : i + 1}
            </span>
            <span
              className={cn(
                "text-[13px]",
                active ? "font-semibold text-white" : done ? "text-white/85" : "text-white/45",
              )}
            >
              {t(`status.${step}`)}
            </span>
          </div>
        );
      })}
    </div>
  );
}

/* ----------------------------------------------------------------- KPIs */

function KpiRow({
  current,
  tMy,
  fmtH,
}: {
  current: PeriodAgg;
  tMy: ReturnType<typeof useTranslations<"timesheets.my">>;
  fmtH: (v: number) => string;
}) {
  const cards = [
    {
      icon: Clock,
      tone: "text-ink",
      label: tMy("kpis.total"),
      value: fmtH(current.total),
      hint: tMy("kpis.totalHint"),
    },
    {
      icon: AlarmClock,
      tone: "text-orange-600",
      label: tMy("kpis.overtime"),
      value: fmtH(current.supplementaires),
      hint: tMy("kpis.overtimeHint"),
    },
    {
      icon: Moon,
      tone: "text-violet-600",
      label: tMy("kpis.premium"),
      value: fmtH(current.nuit + current.weekend),
      hint: tMy("kpis.premiumHint"),
    },
    {
      icon: UserX,
      tone: current.absences > 0 ? "text-danger-600" : "text-ink-3",
      label: tMy("kpis.absences"),
      value: `${current.absences.toFixed(1)} ${tMy("units.days")}`,
      hint: tMy("kpis.absencesHint"),
    },
  ];
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {cards.map((c) => (
        <Card key={c.label}>
          <CardContent padding="md">
            <div className="flex items-center justify-between">
              <span className="text-[12.5px] font-medium text-ink-3">{c.label}</span>
              <c.icon className={cn("h-4 w-4", c.tone)} />
            </div>
            <p className={cn("mt-2 font-mono-tabular text-[22px] font-bold", c.tone)}>{c.value}</p>
            <p className="mt-0.5 text-[11.5px] text-ink-3">{c.hint}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

/* ----------------------------------------------------------------- Composition */

function Composition({
  current,
  tMy,
  tHours,
  fmtH,
}: {
  current: PeriodAgg;
  tMy: ReturnType<typeof useTranslations<"timesheets.my">>;
  tHours: ReturnType<typeof useTranslations<"timesheets.hours">>;
  fmtH: (v: number) => string;
}) {
  const segments = [
    { key: "normales", label: tHours("normales"), value: current.normales, color: "bg-sky-500" },
    {
      key: "supp",
      label: tHours("supplementaires"),
      value: current.supplementaires,
      color: "bg-orange-500",
    },
    { key: "nuit", label: tHours("nuit"), value: current.nuit, color: "bg-violet-500" },
    { key: "weekend", label: tHours("weekend"), value: current.weekend, color: "bg-teal-500" },
  ].filter((s) => s.value > 0);
  const total = current.total;

  return (
    <Card>
      <CardContent padding="lg">
        <h3 className="text-[14px] font-semibold text-ink">{tMy("composition.title")}</h3>
        {total <= 0 ? (
          <p className="mt-6 text-center text-[13px] text-ink-3">{tMy("composition.empty")}</p>
        ) : (
          <>
            <div className="mt-4 flex h-3 w-full overflow-hidden rounded-full bg-bg-soft">
              {segments.map((s) => (
                <div
                  key={s.key}
                  className={s.color}
                  style={{ width: `${(s.value / total) * 100}%` }}
                />
              ))}
            </div>
            <ul className="mt-4 space-y-2.5">
              {segments.map((s) => (
                <li key={s.key} className="flex items-center justify-between text-[13px]">
                  <span className="flex items-center gap-2 text-ink-2">
                    <span className={cn("h-2.5 w-2.5 rounded-full", s.color)} />
                    {s.label}
                  </span>
                  <span className="font-mono-tabular text-ink">
                    {fmtH(s.value)}
                    <span className="ml-2 text-ink-3">
                      {Math.round((s.value / total) * 100)}%
                    </span>
                  </span>
                </li>
              ))}
            </ul>
          </>
        )}
      </CardContent>
    </Card>
  );
}

/* ----------------------------------------------------------------- Pay impact */

function PayImpact({
  current,
  tMy,
  fmtH,
}: {
  current: PeriodAgg;
  tMy: ReturnType<typeof useTranslations<"timesheets.my">>;
  fmtH: (v: number) => string;
}) {
  const rows = [
    {
      label: tMy("payImpact.overtimeDay"),
      value: current.supplementaires,
      mult: OT_MULTIPLIER.day,
      unit: "h" as const,
    },
    {
      label: tMy("payImpact.overtimeNight"),
      value: current.nuit,
      mult: OT_MULTIPLIER.night,
      unit: "h" as const,
    },
    {
      label: tMy("payImpact.overtimeWeekend"),
      value: current.weekend,
      mult: OT_MULTIPLIER.weekend,
      unit: "h" as const,
    },
    {
      label: tMy("payImpact.absences"),
      value: current.absences,
      mult: null,
      unit: "d" as const,
    },
  ].filter((r) => r.value > 0);

  const validated = current.status === "VALIDATED";

  return (
    <Card>
      <CardContent padding="lg">
        <h3 className="text-[14px] font-semibold text-ink">{tMy("payImpact.title")}</h3>
        <p className="mt-1 text-[12.5px] text-ink-3">{tMy("payImpact.subtitle")}</p>

        {rows.length === 0 ? (
          <p className="mt-6 text-center text-[13px] text-ink-3">{tMy("payImpact.none")}</p>
        ) : (
          <ul className="mt-4 divide-y divide-line">
            {rows.map((r) => (
              <li key={r.label} className="flex items-center justify-between py-2.5 text-[13px]">
                <span className="flex items-center gap-2 text-ink-2">
                  {r.label}
                  {r.mult && (
                    <span className="rounded-full bg-orange-50 px-2 py-0.5 text-[11px] font-semibold text-orange-600">
                      {r.mult}
                    </span>
                  )}
                </span>
                <span className="font-mono-tabular font-semibold text-ink">
                  {r.unit === "h" ? fmtH(r.value) : `${r.value.toFixed(1)} ${tMy("units.days")}`}
                </span>
              </li>
            ))}
          </ul>
        )}

        <div
          className={cn(
            "mt-4 flex items-center gap-2 rounded-[12px] px-3 py-2.5 text-[12.5px]",
            validated ? "bg-success-50 text-success-700" : "bg-warning-50 text-warning-700",
          )}
        >
          {validated ? (
            <CheckCircle2 className="h-4 w-4 shrink-0" />
          ) : (
            <Clock className="h-4 w-4 shrink-0" />
          )}
          {validated ? tMy("payImpact.validated") : tMy("payImpact.pending")}
        </div>
      </CardContent>
    </Card>
  );
}

/* ----------------------------------------------------------------- Trend */

function Trend({
  history,
  periode,
  locale,
  tMy,
  fmtH,
}: {
  history: PeriodAgg[];
  periode: string;
  locale: "fr" | "en";
  tMy: ReturnType<typeof useTranslations<"timesheets.my">>;
  fmtH: (v: number) => string;
}) {
  const max = Math.max(1, ...history.map((h) => h.total));
  return (
    <Card>
      <CardContent padding="lg">
        <div className="flex items-center gap-2">
          <TrendingUp className="h-4 w-4 text-orange-500" />
          <h3 className="text-[14px] font-semibold text-ink">{tMy("trend.title")}</h3>
        </div>
        {history.length === 0 ? (
          <p className="mt-6 text-center text-[13px] text-ink-3">{tMy("trend.empty")}</p>
        ) : (
          <div className="mt-5 flex items-end justify-between gap-2">
            {history.map((h) => {
              const isCurrent = h.periode === periode;
              return (
                <div key={h.periode} className="flex flex-1 flex-col items-center gap-2">
                  <span className="text-[11px] font-mono-tabular text-ink-3">
                    {h.total > 0 ? h.total.toFixed(0) : ""}
                  </span>
                  <div className="flex h-28 w-full items-end justify-center">
                    <div
                      className={cn(
                        "w-7 rounded-t-md transition-all",
                        isCurrent ? "bg-orange-500" : "bg-ink/15",
                      )}
                      style={{ height: `${Math.max(4, (h.total / max) * 100)}%` }}
                      title={fmtH(h.total)}
                    />
                  </div>
                  <span
                    className={cn(
                      "text-[10.5px] capitalize",
                      isCurrent ? "font-semibold text-ink" : "text-ink-3",
                    )}
                  >
                    {formatPeriod(h.periode, locale).split(" ")[0]}
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

/* ----------------------------------------------------------------- History table */

function HistoryTable({
  history,
  locale,
  tMy,
  t,
  onRow,
}: {
  history: PeriodAgg[];
  locale: "fr" | "en";
  tMy: ReturnType<typeof useTranslations<"timesheets.my">>;
  t: ReturnType<typeof useTranslations<"timesheets">>;
  onRow: (id: string) => void;
}) {
  const rows = [...history].sort((a, b) => b.periode.localeCompare(a.periode));
  return (
    <div className="overflow-hidden rounded-[20px] border border-line bg-white">
      <div className="border-b border-line px-5 py-3.5">
        <h3 className="text-[14px] font-semibold text-ink">{tMy("history.title")}</h3>
      </div>
      {rows.length === 0 ? (
        <p className="px-5 py-10 text-center text-[13px] text-ink-3">{tMy("history.empty")}</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-[13px]">
            <thead>
              <tr className="border-b border-line text-left text-[12px] uppercase tracking-wide text-ink-3">
                <th className="px-5 py-2.5 font-medium">{tMy("history.period")}</th>
                <th className="px-5 py-2.5 text-right font-medium">{tMy("history.total")}</th>
                <th className="px-5 py-2.5 text-right font-medium">{tMy("history.overtime")}</th>
                <th className="px-5 py-2.5 text-right font-medium">{tMy("history.absences")}</th>
                <th className="px-5 py-2.5 font-medium">{tMy("history.status")}</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((h) => (
                <tr
                  key={h.periode}
                  onClick={() => onRow(h.primaryId)}
                  className="cursor-pointer border-b border-line/60 last:border-0 hover:bg-bg-soft"
                >
                  <td className="px-5 py-3 capitalize text-ink">{formatPeriod(h.periode, locale)}</td>
                  <td className="px-5 py-3 text-right font-mono-tabular font-semibold text-ink">
                    {h.total.toFixed(1)}
                  </td>
                  <td className="px-5 py-3 text-right font-mono-tabular text-ink-2">
                    {h.supplementaires > 0 ? h.supplementaires.toFixed(1) : "—"}
                  </td>
                  <td className="px-5 py-3 text-right font-mono-tabular text-ink-2">
                    {h.absences > 0 ? h.absences.toFixed(1) : "—"}
                  </td>
                  <td className="px-5 py-3">
                    <Badge tone={timesheetStatusTone(h.status)}>{t(`status.${h.status}`)}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
