"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  CheckCircle2,
  ClipboardList,
  Loader2,
  MessageSquare,
  Star,
  Target,
  TrendingUp,
  UserCircle2,
} from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { reviewStatusTone } from "@/lib/training-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  ObjectiveResponse,
  ReviewResponse,
  ReviewStatus,
} from "@/server/ksm/modules/reviews";

type MinePayload = {
  employee: EmployeeResponse | null;
  reviews: ReviewResponse[];
  objectives: Record<string, ObjectiveResponse[]>;
};

const SCALE = 5;

function num(v: number | string | null | undefined): number | null {
  if (v == null) return null;
  const n = typeof v === "string" ? Number(v) : v;
  return Number.isFinite(n) ? n : null;
}

/** Weighted objective achievement (/5): weights by poids when present, else simple average. */
function objectiveAchievement(objectives: ObjectiveResponse[]): number | null {
  const scored = objectives
    .map((o) => ({ note: num(o.noteAtteinte), poids: num(o.poids) }))
    .filter((o): o is { note: number; poids: number | null } => o.note != null);
  if (scored.length === 0) return null;
  const totalWeight = scored.reduce((s, o) => s + (o.poids ?? 0), 0);
  if (totalWeight > 0) {
    return scored.reduce((s, o) => s + o.note * (o.poids ?? 0), 0) / totalWeight;
  }
  return scored.reduce((s, o) => s + o.note, 0) / scored.length;
}

function Stars({ value, size = "sm" }: { value: number; size?: "sm" | "lg" }) {
  const cls = size === "lg" ? "h-4 w-4" : "h-3 w-3";
  return (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((i) => (
        <Star
          key={i}
          className={cn(
            cls,
            i <= Math.round(value)
              ? "fill-orange-500 text-orange-500"
              : "fill-bg-soft text-bg-soft",
          )}
        />
      ))}
    </div>
  );
}

export function MyReviews() {
  const t = useTranslations("reviews");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: ["hrm", "reviews", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/reviews/mine"),
    refetchInterval: 60_000,
  });

  const ackMutation = useMutation({
    mutationFn: (id: string) =>
      apiFetch<ReviewResponse>(`/api/hrm/reviews/${id}/acknowledge-mine`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("mine.ackSuccess"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "reviews"] });
    },
    onError: (cause: unknown) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  const reviews = React.useMemo(() => {
    const list = query.data?.reviews ?? [];
    // Most recent first by period label (e.g. 2026-Q3 > 2026-Q1).
    return [...list].sort((a, b) => b.periode.localeCompare(a.periode));
  }, [query.data?.reviews]);
  const objectivesByReview = query.data?.objectives ?? {};

  const scored = reviews
    .map((r) => num(r.noteGlobale))
    .filter((n): n is number => n != null);
  const avgScore = scored.length ? scored.reduce((s, n) => s + n, 0) / scored.length : null;
  const latest = reviews[0] ?? null;
  const latestScore = latest ? num(latest.noteGlobale) : null;
  const toAcknowledge = reviews.filter((r) => r.status === "SUBMITTED");

  // Trend oldest → newest for the bar chart.
  const trend = [...reviews]
    .filter((r) => num(r.noteGlobale) != null)
    .sort((a, b) => a.periode.localeCompare(b.periode));

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
      ) : reviews.length === 0 ? (
        <Card>
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("mine.empty")}</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-6">
          {latest && (
            <ReviewHero
              review={latest}
              score={latestScore}
              onAcknowledge={() => ackMutation.mutate(latest.id)}
              acknowledging={ackMutation.isPending && ackMutation.variables === latest.id}
            />
          )}

          <KpiRow
            count={reviews.length}
            latestScore={latestScore}
            avgScore={avgScore}
            pendingAck={toAcknowledge.length}
          />

          {toAcknowledge.length > 1 && (
            <section>
              <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
                <span className="inline-block h-2 w-2 animate-pulse rounded-full bg-warning-500" />
                {t("mine.toAcknowledgeTitle")}
                <Badge tone="warning">{toAcknowledge.length}</Badge>
              </h2>
              <div className="grid gap-3 md:grid-cols-2">
                {toAcknowledge.map((r) => (
                  <AcknowledgeCard
                    key={r.id}
                    review={r}
                    score={num(r.noteGlobale)}
                    onAcknowledge={() => ackMutation.mutate(r.id)}
                    acknowledging={ackMutation.isPending && ackMutation.variables === r.id}
                  />
                ))}
              </div>
            </section>
          )}

          {trend.length >= 2 && <ScoreTrend reviews={trend} />}

          <section>
            <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
              <ClipboardList className="h-3.5 w-3.5 text-orange-500" />
              {t("mine.historyTitle")}
            </h2>
            <div className="space-y-3">
              {reviews.map((r) => (
                <ReviewCard
                  key={r.id}
                  review={r}
                  objectives={objectivesByReview[r.id] ?? []}
                />
              ))}
            </div>
          </section>
        </div>
      )}
    </>
  );
}

function workflowSteps(status: ReviewStatus, t: (k: string) => string): WorkflowStep[] {
  const order: ReviewStatus[] = ["DRAFT", "SUBMITTED", "ACKNOWLEDGED", "FINALIZED"];
  const idx = order.indexOf(status);
  const mk = (k: string, l: string, p: number): WorkflowStep => ({
    key: k,
    label: l,
    state: p < idx ? "done" : p === idx ? "active" : "pending",
  });
  return [
    mk("draft", t("detail.workflow.draft"), 0),
    mk("submitted", t("detail.workflow.submitted"), 1),
    mk("acknowledged", t("detail.workflow.acknowledged"), 2),
    mk("finalized", t("detail.workflow.finalized"), 3),
  ];
}

/* ----------------------------------------------------------------- Hero */
function ReviewHero({
  review,
  score,
  onAcknowledge,
  acknowledging,
}: {
  review: ReviewResponse;
  score: number | null;
  onAcknowledge: () => void;
  acknowledging: boolean;
}) {
  const t = useTranslations("reviews");
  return (
    <div className="overflow-hidden rounded-[22px] border border-line bg-gradient-to-br from-ink to-[#23314d] text-white shadow-sm">
      <div className="grid gap-6 p-6 md:grid-cols-[1.3fr_1fr] md:p-7">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-[12px] font-medium uppercase tracking-wide text-white/60">
            <Star className="h-4 w-4" />
            {t("mine.hero.latest")}
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <Badge tone="orange">{review.periode}</Badge>
            <Badge tone={reviewStatusTone(review.status)}>{t(`status.${review.status}`)}</Badge>
          </div>
          <div className="flex items-end gap-3">
            <span className="font-display font-mono-tabular text-[44px] font-extrabold leading-none">
              {score != null ? score.toFixed(1) : "—"}
            </span>
            {score != null && <span className="pb-1 text-[14px] text-white/50">/ {SCALE}</span>}
            {score != null && (
              <div className="pb-1.5">
                <Stars value={score} size="lg" />
              </div>
            )}
          </div>
          {review.evaluateurDisplayName && (
            <p className="flex items-center gap-1.5 text-[13px] text-white/70">
              <UserCircle2 className="h-4 w-4 text-white/50" />
              {t("detail.evaluator")} · {review.evaluateurDisplayName}
            </p>
          )}
          {review.status === "SUBMITTED" && (
            <div className="flex flex-wrap items-center gap-3 pt-1">
              <Button type="button" onClick={onAcknowledge} disabled={acknowledging}>
                {acknowledging ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <CheckCircle2 className="h-4 w-4" />
                )}
                {t("mine.acknowledge")}
              </Button>
              <span className="text-[12px] text-white/60">{t("mine.acknowledgeHint")}</span>
            </div>
          )}
        </div>

        <div className="rounded-[16px] bg-white/5 p-5">
          <WorkflowStepper steps={workflowSteps(review.status, t)} />
          <Link
            href={`/reviews/${review.id}`}
            className="mt-4 inline-flex text-[12.5px] font-semibold text-orange-300 hover:text-orange-200"
          >
            {t("mine.viewDetail")} →
          </Link>
        </div>
      </div>
    </div>
  );
}

/* ----------------------------------------------------------------- KPIs */
function KpiRow({
  count,
  latestScore,
  avgScore,
  pendingAck,
}: {
  count: number;
  latestScore: number | null;
  avgScore: number | null;
  pendingAck: number;
}) {
  const t = useTranslations("reviews");
  const fmt = (v: number | null) => (v != null ? `${v.toFixed(1)}/${SCALE}` : "—");
  const items = [
    { icon: ClipboardList, label: t("mine.kpi.count"), value: String(count), tone: "text-ink" },
    { icon: Star, label: t("mine.kpi.latest"), value: fmt(latestScore), tone: "text-orange-600" },
    { icon: TrendingUp, label: t("mine.kpi.average"), value: fmt(avgScore), tone: "text-ink" },
    {
      icon: CheckCircle2,
      label: t("mine.kpi.pendingAck"),
      value: String(pendingAck),
      tone: pendingAck > 0 ? "text-warning-600" : "text-success-600",
    },
  ];
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {items.map((it) => (
        <Card key={it.label}>
          <CardContent padding="md">
            <div className="flex items-center justify-between">
              <span className="text-[12.5px] font-medium text-ink-3">{it.label}</span>
              <it.icon className={cn("h-4 w-4", it.tone)} />
            </div>
            <p className={cn("mt-2 font-mono-tabular text-[22px] font-bold", it.tone)}>{it.value}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

/* ----------------------------------------------------------------- Trend */
function ScoreTrend({ reviews }: { reviews: ReviewResponse[] }) {
  const t = useTranslations("reviews");
  return (
    <Card>
      <CardContent padding="lg">
        <h3 className="mb-4 flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
          <TrendingUp className="h-4 w-4 text-orange-500" />
          {t("mine.trendTitle")}
        </h3>
        <div className="flex items-end gap-3" style={{ height: 140 }}>
          {reviews.map((r) => {
            const score = num(r.noteGlobale) ?? 0;
            const pct = Math.max(6, (score / SCALE) * 100);
            return (
              <div key={r.id} className="flex flex-1 flex-col items-center justify-end gap-2">
                <span className="font-mono-tabular text-[12px] font-bold text-ink">
                  {score.toFixed(1)}
                </span>
                <div className="flex w-full items-end justify-center" style={{ height: 96 }}>
                  <div
                    className="w-full max-w-[44px] rounded-t-[8px] bg-grad-orange"
                    style={{ height: `${pct}%` }}
                  />
                </div>
                <span className="font-mono-tabular text-[10.5px] text-ink-4">{r.periode}</span>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}

/* ----------------------------------------------------------------- Review card */
function AcknowledgeCard({
  review,
  score,
  onAcknowledge,
  acknowledging,
}: {
  review: ReviewResponse;
  score: number | null;
  onAcknowledge: () => void;
  acknowledging: boolean;
}) {
  const t = useTranslations("reviews");
  return (
    <Card>
      <CardContent padding="md">
        <div className="mb-2 flex items-center justify-between">
          <Badge tone="orange">{review.periode}</Badge>
          <Badge tone="warning">{t("status.SUBMITTED")}</Badge>
        </div>
        <div className="mb-3 flex items-center gap-2">
          <span className="font-display font-mono-tabular text-[20px] font-extrabold text-ink">
            {score != null ? score.toFixed(1) : "—"}
          </span>
          {score != null && <Stars value={score} />}
        </div>
        <Button type="button" onClick={onAcknowledge} disabled={acknowledging} className="w-full">
          {acknowledging ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <CheckCircle2 className="h-4 w-4" />
          )}
          {t("mine.acknowledge")}
        </Button>
      </CardContent>
    </Card>
  );
}

function ReviewCard({
  review,
  objectives,
}: {
  review: ReviewResponse;
  objectives: ObjectiveResponse[];
}) {
  const t = useTranslations("reviews");
  const score = num(review.noteGlobale);
  const achievement = objectiveAchievement(objectives);
  const evaluated = objectives.filter((o) => num(o.noteAtteinte) != null).length;

  return (
    <Card>
      <CardContent padding="lg">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <Badge tone="orange">{review.periode}</Badge>
            <Badge tone={reviewStatusTone(review.status)}>{t(`status.${review.status}`)}</Badge>
            {review.evaluateurDisplayName && (
              <span className="flex items-center gap-1.5 text-[12px] text-ink-3">
                <UserCircle2 className="h-3.5 w-3.5 text-ink-4" />
                {review.evaluateurDisplayName}
              </span>
            )}
          </div>
          <div className="flex items-center gap-2">
            <span className="font-display font-mono-tabular text-[20px] font-extrabold text-ink">
              {score != null ? score.toFixed(1) : "—"}
            </span>
            <span className="text-[11px] text-ink-4">/ {SCALE}</span>
            {score != null && <Stars value={score} />}
          </div>
        </div>

        {objectives.length > 0 && (
          <div className="mt-4 rounded-[14px] border border-line-soft bg-bg-soft/50 p-4">
            <div className="mb-2.5 flex items-center justify-between">
              <span className="flex items-center gap-1.5 text-[11.5px] font-bold uppercase tracking-wider text-ink-3">
                <Target className="h-3.5 w-3.5 text-orange-500" />
                {t("detail.objectives.title")}
              </span>
              {achievement != null && (
                <span className="font-mono-tabular text-[12px] font-bold text-ink">
                  {t("mine.objectivesAvg")} {achievement.toFixed(1)}/{SCALE}
                </span>
              )}
            </div>
            <ul className="space-y-2">
              {objectives.map((o) => {
                const note = num(o.noteAtteinte);
                const poids = num(o.poids);
                return (
                  <li key={o.id} className="flex items-center gap-3">
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-[12.5px] font-medium text-ink">
                        {o.description}
                      </div>
                      {poids != null && (
                        <div className="text-[10.5px] text-ink-4">
                          {t("detail.objectives.columns.poids")} · {poids.toFixed(0)}%
                        </div>
                      )}
                    </div>
                    <div className="h-1.5 w-24 overflow-hidden rounded-full bg-line-soft">
                      <div
                        className="h-full rounded-full bg-grad-orange"
                        style={{ width: `${note != null ? (note / SCALE) * 100 : 0}%` }}
                      />
                    </div>
                    <span className="w-10 shrink-0 text-right font-mono-tabular text-[12px] font-bold text-ink">
                      {note != null ? note.toFixed(1) : "—"}
                    </span>
                  </li>
                );
              })}
            </ul>
            <p className="mt-2 text-[10.5px] text-ink-4">
              {t("mine.objectivesEvaluated", { evaluated, total: objectives.length })}
            </p>
          </div>
        )}

        {review.commentaires && (
          <div className="mt-4">
            <div className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wider text-ink-4">
              <MessageSquare className="h-3.5 w-3.5" />
              {t("detail.comments")}
            </div>
            <p className="mt-1 whitespace-pre-line text-[13px] text-ink-2">{review.commentaires}</p>
          </div>
        )}
        {review.planAction && (
          <div className="mt-3 rounded-[12px] border-l-2 border-orange-400 bg-orange-50/50 px-3.5 py-2.5">
            <div className="text-[11px] font-semibold uppercase tracking-wider text-orange-700">
              {t("detail.planAction")}
            </div>
            <p className="mt-1 whitespace-pre-line text-[13px] text-ink-2">{review.planAction}</p>
          </div>
        )}

        <div className="mt-4 flex justify-end">
          <Link
            href={`/reviews/${review.id}`}
            className="text-[12px] font-semibold text-orange-600 hover:text-orange-700"
          >
            {t("mine.viewDetail")} →
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}
