"use client";

import { useMutation } from "@tanstack/react-query";
import {
  Check,
  ChevronLeft,
  ChevronRight,
  Gauge as GaugeIcon,
  Loader2,
  Sparkles,
  Star,
  Target,
  X,
} from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { cn } from "@/lib/utils";
import type { ObjectiveResponse, ReviewResponse } from "@/server/ksm/modules/reviews";

type Props = {
  review: ReviewResponse;
  objectives: ObjectiveResponse[];
  employeeName: string;
  onClose: () => void;
  onCompleted: () => void;
};

const STEPS = ["step1", "step2", "step3"] as const;

/**
 * Immersive, guided evaluation experience ("Studio"). Replaces the small submit
 * dialog with a 3-step flow — score objectives, validate the (auto-suggested)
 * global note, then write the appraisal — and commits everything in one go via
 * the existing endpoints (evaluate each objective, then submit the review).
 */
export function EvaluationStudio({ review, objectives, employeeName, onClose, onCompleted }: Props) {
  const t = useTranslations("reviews.studio");
  const tErrors = useTranslations("errors");

  const [step, setStep] = React.useState(0);
  const [scores, setScores] = React.useState<Record<string, number>>(() =>
    Object.fromEntries(
      objectives.map((o) => [o.id, o.noteAtteinte != null ? Number(o.noteAtteinte) : 3]),
    ),
  );
  const [comments, setComments] = React.useState<Record<string, string>>(() =>
    Object.fromEntries(objectives.map((o) => [o.id, o.commentaire ?? ""])),
  );
  const [overridden, setOverridden] = React.useState(false);
  const [manualNote, setManualNote] = React.useState(3);
  const [commentaires, setCommentaires] = React.useState(review.commentaires ?? "");
  const [planAction, setPlanAction] = React.useState(review.planAction ?? "");

  const weighted = React.useMemo(() => {
    if (objectives.length === 0) return null;
    let wsum = 0;
    let num = 0;
    for (const o of objectives) {
      const w = o.poids != null && Number(o.poids) > 0 ? Number(o.poids) : 1;
      wsum += w;
      num += w * (scores[o.id] ?? 0);
    }
    return wsum > 0 ? Math.round((num / wsum) * 10) / 10 : null;
  }, [objectives, scores]);

  const effectiveNote = overridden ? manualNote : weighted ?? manualNote;
  const canFinish = effectiveNote >= 0 && effectiveNote <= 5 && commentaires.trim().length >= 5;

  const m = useMutation({
    mutationFn: async () => {
      for (const o of objectives) {
        const newScore = scores[o.id] ?? 0;
        const newComment = (comments[o.id] ?? "").trim();
        const prevScore = o.noteAtteinte != null ? Number(o.noteAtteinte) : null;
        const prevComment = (o.commentaire ?? "").trim();
        if (prevScore !== newScore || prevComment !== newComment) {
          await apiFetch(`/api/hrm/reviews/objectives/${o.id}/evaluate`, {
            method: "POST",
            body: { noteAtteinte: newScore, commentaire: newComment || null },
          });
        }
      }
      await apiFetch(`/api/hrm/reviews/${review.id}/submit`, {
        method: "POST",
        body: {
          noteGlobale: Math.round(effectiveNote * 10) / 10,
          commentaires: commentaires.trim(),
          planAction: planAction.trim() || null,
        },
      });
    },
    onSuccess: () => {
      toast.success(t("submitSuccess"));
      onCompleted();
      onClose();
    },
    onError: (cause) =>
      toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown")),
  });

  const isLast = step === STEPS.length - 1;

  return (
    <div className="fixed inset-0 z-50 flex flex-col bg-[radial-gradient(120%_120%_at_50%_-10%,#FFF7ED_0%,#FBFAF7_45%,#F4F1EA_100%)] fade-up">
      {/* Header */}
      <header className="flex shrink-0 items-center justify-between gap-4 border-b border-line/70 bg-white/70 px-6 py-4 backdrop-blur-md">
        <div className="flex items-center gap-3">
          <span className="grid h-10 w-10 place-items-center rounded-[12px] bg-grad-orange text-white shadow-orange-brand">
            <Sparkles className="h-5 w-5" />
          </span>
          <div>
            <h2 className="font-display text-[18px] font-bold tracking-tight text-ink">
              {t("title")}
            </h2>
            <p className="text-[12px] text-ink-3">
              {employeeName} · <span className="font-mono-tabular">{review.periode}</span>
            </p>
          </div>
        </div>
        <button
          type="button"
          onClick={onClose}
          aria-label={t("close")}
          className="grid h-9 w-9 place-items-center rounded-[10px] text-ink-3 transition-colors hover:bg-bg-soft hover:text-ink"
        >
          <X className="h-5 w-5" />
        </button>
      </header>

      {/* Stepper */}
      <div className="shrink-0 border-b border-line/60 bg-white/40 px-6 py-3">
        <div className="mx-auto flex max-w-3xl items-center">
          {STEPS.map((s, i) => (
            <React.Fragment key={s}>
              <div className="flex items-center gap-2.5">
                <span
                  className={cn(
                    "grid h-8 w-8 place-items-center rounded-full text-[13px] font-bold transition-all",
                    i < step && "bg-success-500 text-white",
                    i === step && "bg-grad-orange text-white shadow-orange-brand ring-4 ring-orange-500/15",
                    i > step && "border border-line bg-white text-ink-4",
                  )}
                >
                  {i < step ? <Check className="h-4 w-4" /> : i + 1}
                </span>
                <span
                  className={cn(
                    "text-[12.5px] font-semibold transition-colors",
                    i === step ? "text-ink" : "text-ink-3",
                  )}
                >
                  {t(`steps.${s}`)}
                </span>
              </div>
              {i < STEPS.length - 1 && (
                <div
                  className={cn(
                    "mx-3 h-[2px] flex-1 rounded-full transition-colors",
                    i < step ? "bg-success-500" : "bg-line",
                  )}
                />
              )}
            </React.Fragment>
          ))}
        </div>
      </div>

      {/* Body */}
      <div className="flex-1 overflow-y-auto px-6 py-7">
        <div className="mx-auto max-w-5xl">
          {step === 0 && (
            <StepObjectives
              objectives={objectives}
              scores={scores}
              comments={comments}
              weighted={weighted}
              onScore={(id, v) => setScores((p) => ({ ...p, [id]: v }))}
              onComment={(id, v) => setComments((p) => ({ ...p, [id]: v }))}
            />
          )}
          {step === 1 && (
            <StepSynthesis
              objectives={objectives}
              scores={scores}
              suggested={weighted}
              note={effectiveNote}
              overridden={overridden}
              onNote={(v) => {
                setOverridden(true);
                setManualNote(v);
              }}
              onResetToSuggested={
                weighted != null
                  ? () => {
                      setOverridden(false);
                      setManualNote(weighted);
                    }
                  : undefined
              }
            />
          )}
          {step === 2 && (
            <StepFeedback
              employeeName={employeeName}
              periode={review.periode}
              note={effectiveNote}
              objectives={objectives}
              scores={scores}
              commentaires={commentaires}
              planAction={planAction}
              onCommentaires={setCommentaires}
              onPlanAction={setPlanAction}
            />
          )}
        </div>
      </div>

      {/* Footer */}
      <footer className="flex shrink-0 items-center justify-between gap-3 border-t border-line/70 bg-white/80 px-6 py-4 backdrop-blur-md">
        <Button
          type="button"
          variant="secondary"
          disabled={step === 0}
          onClick={() => setStep((s) => Math.max(0, s - 1))}
        >
          <ChevronLeft className="h-4 w-4" />
          {t("prev")}
        </Button>
        <div className="flex items-center gap-2 text-[12px] text-ink-3">
          <GaugeIcon className="h-4 w-4 text-orange-500" />
          {t("liveScore")} ·{" "}
          <span className="font-mono-tabular text-[14px] font-bold text-ink">
            {effectiveNote.toFixed(1)}
          </span>
          <span>/ 5</span>
        </div>
        {isLast ? (
          <Button type="button" disabled={!canFinish || m.isPending} onClick={() => m.mutate()}>
            {m.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Check className="h-4 w-4" />
            )}
            {t("finish")}
          </Button>
        ) : (
          <Button type="button" onClick={() => setStep((s) => Math.min(STEPS.length - 1, s + 1))}>
            {t("next")}
            <ChevronRight className="h-4 w-4" />
          </Button>
        )}
      </footer>
    </div>
  );
}

/* ── Step 1 · Objectives ─────────────────────────────────────────────────── */

function StepObjectives({
  objectives,
  scores,
  comments,
  weighted,
  onScore,
  onComment,
}: {
  objectives: ObjectiveResponse[];
  scores: Record<string, number>;
  comments: Record<string, string>;
  weighted: number | null;
  onScore: (id: string, v: number) => void;
  onComment: (id: string, v: string) => void;
}) {
  const t = useTranslations("reviews.studio");
  const totalWeight = objectives.reduce(
    (s, o) => s + (o.poids != null ? Number(o.poids) : 0),
    0,
  );

  if (objectives.length === 0) {
    return (
      <div className="grid place-items-center rounded-[20px] border border-dashed border-line bg-white/60 px-6 py-16 text-center">
        <Target className="mb-3 h-8 w-8 text-ink-4" />
        <p className="max-w-sm text-[13.5px] text-ink-3">{t("noObjectives")}</p>
      </div>
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_300px]">
      <div className="flex flex-col gap-3">
        {objectives.map((o) => {
          const v = scores[o.id] ?? 0;
          return (
            <div
              key={o.id}
              className="rounded-[16px] border border-line bg-white p-4 shadow-xs-brand"
            >
              <div className="flex items-start justify-between gap-3">
                <p className="text-[13.5px] font-semibold text-ink">{o.description}</p>
                <span className="shrink-0 rounded-full bg-orange-50 px-2.5 py-0.5 font-mono-tabular text-[11.5px] font-bold text-orange-600">
                  {o.poids != null ? `${Number(o.poids).toFixed(0)}%` : "—"}
                </span>
              </div>
              <div className="mt-3 flex items-center gap-4">
                <input
                  type="range"
                  min={0}
                  max={5}
                  step={0.5}
                  value={v}
                  onChange={(e) => onScore(o.id, Number(e.target.value))}
                  className="h-1.5 flex-1 cursor-pointer appearance-none rounded-full bg-bg-soft accent-orange-500"
                />
                <div className="flex w-[88px] items-center justify-end gap-1.5">
                  <Stars value={v} />
                  <span className="w-8 text-right font-mono-tabular text-[14px] font-bold text-ink">
                    {v.toFixed(1)}
                  </span>
                </div>
              </div>
              <input
                type="text"
                value={comments[o.id] ?? ""}
                onChange={(e) => onComment(o.id, e.target.value)}
                placeholder={t("objectiveComment")}
                className="mt-3 w-full rounded-[10px] border border-line bg-bg-soft/40 px-3 py-2 text-[12.5px] text-ink outline-none placeholder:text-ink-4 focus:border-orange-400 focus:bg-white"
              />
            </div>
          );
        })}
      </div>

      <aside className="lg:sticky lg:top-2">
        <div className="rounded-[18px] border border-line bg-white p-5 text-center shadow-xs-brand">
          <p className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">
            {t("weightedScore")}
          </p>
          <Gauge value={weighted ?? 0} />
          <p className="mt-1 text-[11.5px] text-ink-3">
            {totalWeight > 0
              ? t("weightTotal", { total: totalWeight.toFixed(0) })
              : t("weightEqual")}
          </p>
        </div>
      </aside>
    </div>
  );
}

/* ── Step 2 · Synthesis ──────────────────────────────────────────────────── */

function StepSynthesis({
  objectives,
  scores,
  suggested,
  note,
  overridden,
  onNote,
  onResetToSuggested,
}: {
  objectives: ObjectiveResponse[];
  scores: Record<string, number>;
  suggested: number | null;
  note: number;
  overridden: boolean;
  onNote: (v: number) => void;
  onResetToSuggested?: () => void;
}) {
  const t = useTranslations("reviews.studio");
  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <div className="rounded-[18px] border border-line bg-white p-6 text-center shadow-xs-brand">
        <p className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">
          {t("globalScore")}
        </p>
        <Gauge value={note} large />
        {suggested != null && (
          <p className="mt-1 text-[12px] text-ink-3">
            {t("suggested", { value: suggested.toFixed(1) })}
            {overridden && onResetToSuggested && (
              <button
                type="button"
                onClick={onResetToSuggested}
                className="ml-2 font-semibold text-orange-600 hover:underline"
              >
                {t("reset")}
              </button>
            )}
          </p>
        )}
        <div className="mt-4 flex items-center gap-3 px-2">
          <input
            type="range"
            min={0}
            max={5}
            step={0.1}
            value={note}
            onChange={(e) => onNote(Number(e.target.value))}
            className="h-1.5 flex-1 cursor-pointer appearance-none rounded-full bg-bg-soft accent-orange-500"
          />
          <input
            type="number"
            min={0}
            max={5}
            step={0.1}
            value={note}
            onChange={(e) => onNote(Number(e.target.value))}
            className="w-16 rounded-[10px] border border-line bg-white px-2 py-1.5 text-right font-mono-tabular text-[14px] font-bold text-ink outline-none focus:border-orange-400"
          />
        </div>
        <p className="mt-3 text-[11.5px] leading-relaxed text-ink-4">{t("overrideHint")}</p>
      </div>

      <div className="rounded-[18px] border border-line bg-white p-6 shadow-xs-brand">
        <p className="mb-3 flex items-center gap-2 text-[12.5px] font-bold text-ink">
          <Target className="h-4 w-4 text-orange-500" />
          {t("radarTitle")}
        </p>
        {objectives.length >= 3 ? (
          <Radar objectives={objectives} scores={scores} />
        ) : objectives.length > 0 ? (
          <div className="flex flex-col gap-2.5">
            {objectives.map((o) => (
              <div key={o.id}>
                <div className="flex items-center justify-between text-[12px]">
                  <span className="truncate pr-2 text-ink-2">{o.description}</span>
                  <span className="font-mono-tabular font-bold text-ink">
                    {(scores[o.id] ?? 0).toFixed(1)}
                  </span>
                </div>
                <div className="mt-1 h-2 overflow-hidden rounded-full bg-bg-soft">
                  <div
                    className="h-full rounded-full bg-grad-orange transition-[width] duration-500"
                    style={{ width: `${((scores[o.id] ?? 0) / 5) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="py-8 text-center text-[12.5px] text-ink-3">{t("noObjectivesShort")}</p>
        )}
      </div>
    </div>
  );
}

/* ── Step 3 · Feedback ───────────────────────────────────────────────────── */

function StepFeedback({
  employeeName,
  periode,
  note,
  objectives,
  scores,
  commentaires,
  planAction,
  onCommentaires,
  onPlanAction,
}: {
  employeeName: string;
  periode: string;
  note: number;
  objectives: ObjectiveResponse[];
  scores: Record<string, number>;
  commentaires: string;
  planAction: string;
  onCommentaires: (v: string) => void;
  onPlanAction: (v: string) => void;
}) {
  const t = useTranslations("reviews.studio");
  const ranked = [...objectives].sort((a, b) => (scores[b.id] ?? 0) - (scores[a.id] ?? 0));
  const top = ranked[0];
  const low = ranked.length > 1 ? ranked[ranked.length - 1] : undefined;

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_300px]">
      <div className="flex flex-col gap-5">
        <div>
          <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
            {t("commentsLabel")} <span className="text-danger-500">*</span>
          </label>
          <textarea
            rows={5}
            value={commentaires}
            onChange={(e) => onCommentaires(e.target.value)}
            placeholder={t("commentsPlaceholder")}
            className="w-full rounded-[12px] border border-line bg-white px-3.5 py-3 text-[13.5px] text-ink outline-none placeholder:text-ink-4 focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          />
        </div>
        <div>
          <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
            {t("planLabel")}
          </label>
          <textarea
            rows={4}
            value={planAction}
            onChange={(e) => onPlanAction(e.target.value)}
            placeholder={t("planPlaceholder")}
            className="w-full rounded-[12px] border border-line bg-white px-3.5 py-3 text-[13.5px] text-ink outline-none placeholder:text-ink-4 focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          />
        </div>
      </div>

      <aside className="flex flex-col gap-3 lg:sticky lg:top-2">
        <div className="rounded-[18px] border border-line bg-white p-5 shadow-xs-brand">
          <p className="mb-3 text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">
            {t("summaryTitle")}
          </p>
          <div className="flex items-center gap-3">
            <Avatar name={employeeName} size="sm" />
            <div className="min-w-0">
              <p className="truncate text-[13px] font-semibold text-ink">{employeeName}</p>
              <p className="font-mono-tabular text-[11.5px] text-ink-3">{periode}</p>
            </div>
            <span className="ml-auto font-display font-mono-tabular text-[22px] font-extrabold text-ink">
              {note.toFixed(1)}
            </span>
          </div>
          {top && (
            <div className="mt-4 space-y-2 border-t border-line-soft pt-3 text-[12px]">
              <div className="flex items-center gap-2">
                <Star className="h-3.5 w-3.5 fill-success-500 text-success-500" />
                <span className="truncate text-ink-2">{top.description}</span>
              </div>
              {low && low.id !== top.id && (
                <div className="flex items-center gap-2">
                  <Target className="h-3.5 w-3.5 text-danger-500" />
                  <span className="truncate text-ink-2">{low.description}</span>
                </div>
              )}
            </div>
          )}
        </div>
      </aside>
    </div>
  );
}

/* ── Small visuals ───────────────────────────────────────────────────────── */

function Stars({ value }: { value: number }) {
  return (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((i) => (
        <Star
          key={i}
          className={cn(
            "h-3 w-3",
            i <= Math.round(value)
              ? "fill-orange-500 text-orange-500"
              : "fill-bg-soft text-bg-soft",
          )}
        />
      ))}
    </div>
  );
}

function Gauge({ value, large }: { value: number; large?: boolean }) {
  const pct = Math.max(0, Math.min(1, value / 5));
  const r = 52;
  const circ = 2 * Math.PI * r;
  const offset = circ * (1 - pct);
  const color = value >= 4 ? "#059669" : value >= 2.5 ? "#EA580C" : "#DC2626";
  return (
    <svg viewBox="0 0 140 140" className={cn("mx-auto", large ? "h-44 w-44" : "h-36 w-36")}>
      <circle cx="70" cy="70" r={r} fill="none" stroke="#ECE6DC" strokeWidth="12" />
      <circle
        cx="70"
        cy="70"
        r={r}
        fill="none"
        stroke={color}
        strokeWidth="12"
        strokeLinecap="round"
        strokeDasharray={circ}
        strokeDashoffset={offset}
        transform="rotate(-90 70 70)"
        style={{ transition: "stroke-dashoffset 0.7s var(--ease-brand, ease)" }}
      />
      <text
        x="70"
        y="68"
        textAnchor="middle"
        className="fill-ink font-display"
        style={{ fontSize: 32, fontWeight: 800 }}
      >
        {value.toFixed(1)}
      </text>
      <text x="70" y="90" textAnchor="middle" className="fill-ink-3" style={{ fontSize: 12 }}>
        / 5
      </text>
    </svg>
  );
}

function Radar({
  objectives,
  scores,
}: {
  objectives: ObjectiveResponse[];
  scores: Record<string, number>;
}) {
  const n = objectives.length;
  const cx = 130;
  const cy = 120;
  const R = 88;
  const angleAt = (i: number) => -Math.PI / 2 + (2 * Math.PI * i) / n;
  const pointAt = (i: number, ratio: number): [number, number] => [
    cx + Math.cos(angleAt(i)) * R * ratio,
    cy + Math.sin(angleAt(i)) * R * ratio,
  ];
  const polygon = objectives
    .map((o, i) => pointAt(i, (scores[o.id] ?? 0) / 5).join(","))
    .join(" ");
  const rings = [0.25, 0.5, 0.75, 1];

  return (
    <div className="flex flex-col items-center">
      <svg viewBox="0 0 260 240" className="h-56 w-full max-w-[280px]">
        {rings.map((ratio) => (
          <polygon
            key={ratio}
            points={objectives.map((_, i) => pointAt(i, ratio).join(",")).join(" ")}
            fill="none"
            stroke="#ECE6DC"
            strokeWidth="1"
          />
        ))}
        {objectives.map((_, i) => {
          const [x, y] = pointAt(i, 1);
          return <line key={i} x1={cx} y1={cy} x2={x} y2={y} stroke="#ECE6DC" strokeWidth="1" />;
        })}
        <polygon
          points={polygon}
          fill="rgba(242,107,15,0.18)"
          stroke="#EA580C"
          strokeWidth="2"
          style={{ transition: "all 0.5s var(--ease-brand, ease)" }}
        />
        {objectives.map((o, i) => {
          const [x, y] = pointAt(i, (scores[o.id] ?? 0) / 5);
          return <circle key={o.id} cx={x} cy={y} r="3" fill="#EA580C" />;
        })}
      </svg>
      <div className="mt-2 grid w-full grid-cols-1 gap-1">
        {objectives.map((o, i) => (
          <div key={o.id} className="flex items-center justify-between text-[11.5px]">
            <span className="truncate pr-2 text-ink-3">
              {i + 1}. {o.description}
            </span>
            <span className="font-mono-tabular font-bold text-ink">
              {(scores[o.id] ?? 0).toFixed(1)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
