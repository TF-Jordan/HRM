"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Award,
  BookOpen,
  CalendarClock,
  CalendarRange,
  Download,
  GraduationCap,
  Loader2,
  MapPin,
  Plus,
  Sparkles,
  Star,
  XCircle,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Textarea } from "@/components/ui/input";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import {
  enrollmentStatusTone,
  trainingRequestStatusTone,
  trainingStatusTone,
} from "@/lib/training-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  EnrollmentResponse,
  TrainingRequestResponse,
  TrainingResponse,
} from "@/server/ksm/modules/trainings";

type MinePayload = {
  employee: EmployeeResponse | null;
  enrollments: EnrollmentResponse[];
  requests: TrainingRequestResponse[];
  trainings: TrainingResponse[];
};

export function MyTrainings() {
  const t = useTranslations("trainings");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const queryClient = useQueryClient();
  const canRequest = useCan("hrm:training:request");
  const [requestOpen, setRequestOpen] = React.useState(false);
  // Captured once at mount so date math stays pure across renders.
  const [today] = React.useState(() => new Date());

  const query = useQuery({
    queryKey: ["hrm", "trainings", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/trainings/mine"),
    refetchInterval: 60_000,
  });
  const data = query.data;

  const trainingById = React.useMemo(() => {
    const m = new Map<string, TrainingResponse>();
    for (const tr of data?.trainings ?? []) m.set(tr.id, tr);
    return m;
  }, [data?.trainings]);

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "trainings"] });
  }, [queryClient]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error("—");
  }

  const cancelRequest = useMutation({
    mutationFn: (id: string) =>
      apiFetch<TrainingRequestResponse>(`/api/hrm/trainings/requests/${id}/cancel`, {
        method: "POST",
      }),
    onSuccess: () => {
      toast.success(t("mine.cancelRequestSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  const enrollments = data?.enrollments ?? [];
  const requests = data?.requests ?? [];

  // Active requests the employee still tracks (pending or just-rejected).
  const openRequests = requests.filter((r) => r.status === "PENDING" || r.status === "REJECTED");
  const pendingCount = requests.filter((r) => r.status === "PENDING").length;

  // Enrollment buckets driven by the linked session's lifecycle.
  const liveEnrollments = enrollments.filter((e) => e.status === "ENROLLED");
  const upcoming = liveEnrollments.filter((e) => {
    const s = trainingById.get(e.trainingId)?.status;
    return s === "PLANNED" || s === "IN_PROGRESS";
  });
  const inProgressCount = liveEnrollments.filter(
    (e) => trainingById.get(e.trainingId)?.status === "IN_PROGRESS",
  ).length;
  const completed = enrollments.filter((e) => e.status === "COMPLETED");

  // Sessions the employee can still request: planned, not already enrolled / pending.
  const enrolledIds = new Set(
    enrollments.filter((e) => e.status !== "CANCELLED").map((e) => e.trainingId),
  );
  const pendingIds = new Set(
    requests.filter((r) => r.status === "PENDING").map((r) => r.trainingId),
  );
  const requestableSessions = (data?.trainings ?? []).filter(
    (tr) => tr.status === "PLANNED" && !enrolledIds.has(tr.id) && !pendingIds.has(tr.id),
  );

  // Development metrics derived from the employee's own enrollments (no mock).
  const completedNotes = completed
    .map((e) => (typeof e.noteEvaluation === "string" ? Number(e.noteEvaluation) : e.noteEvaluation))
    .filter((n): n is number => n != null && Number.isFinite(n));
  const avgScore = completedNotes.length
    ? completedNotes.reduce((s, n) => s + n, 0) / completedNotes.length
    : null;
  const attestations = completed.filter((e) => e.attestationFileId).length;
  const trainingDaysThisYear = enrollments
    .filter((e) => e.status !== "CANCELLED")
    .map((e) => trainingById.get(e.trainingId))
    .filter(
      (tr): tr is TrainingResponse =>
        !!tr?.dateDebut && new Date(tr.dateDebut).getFullYear() === today.getFullYear(),
    )
    .reduce((s, tr) => s + trainingDays(tr.dateDebut, tr.dateFin), 0);

  // Hero = the session currently in progress, else the next planned one.
  const heroEnrollment =
    upcoming.find((e) => trainingById.get(e.trainingId)?.status === "IN_PROGRESS") ??
    [...upcoming]
      .filter((e) => trainingById.get(e.trainingId)?.status === "PLANNED")
      .sort((a, b) =>
        (trainingById.get(a.trainingId)?.dateDebut ?? "").localeCompare(
          trainingById.get(b.trainingId)?.dateDebut ?? "",
        ),
      )[0] ??
    null;
  const heroTraining = heroEnrollment ? trainingById.get(heroEnrollment.trainingId) : undefined;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
        actions={
          <div className="flex items-center gap-2">
            <Link href="/trainings/catalog">
              <Button type="button" variant="secondary">
                <GraduationCap className="h-4 w-4" />
                {t("mine.browse")}
              </Button>
            </Link>
            {canRequest && (
              <Button type="button" onClick={() => setRequestOpen(true)}>
                <Plus className="h-4 w-4" />
                {t("mine.request.cta")}
              </Button>
            )}
          </div>
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : (
        <>
          <DevelopmentHero
            training={heroTraining}
            todayMs={today.getTime()}
            avgScore={avgScore}
            attestations={attestations}
            completedCount={completed.length}
            daysThisYear={trainingDaysThisYear}
            locale={locale}
          />

          <StatCardGrid>
            <StatCard label={t("mine.kpi.upcoming")} value={upcoming.length} sub={t("status.PLANNED")} tone="blue" />
            <StatCard label={t("mine.kpi.inProgress")} value={inProgressCount} sub={t("status.IN_PROGRESS")} tone="orange" />
            <StatCard label={t("mine.kpi.completed")} value={completed.length} sub={t("enrollmentStatus.COMPLETED")} tone="green" />
            <StatCard
              label={t("mine.kpi.pendingRequests")}
              value={pendingCount}
              sub={t("requestStatus.PENDING")}
              tone={pendingCount > 0 ? "amber" : "gray"}
            />
          </StatCardGrid>

          {/* Mes demandes */}
          {openRequests.length > 0 && (
            <section className="mb-6">
              <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
                <Sparkles className="h-3.5 w-3.5 text-orange-500" />
                {t("mine.requestsTitle")}
                <Badge tone="warning">{openRequests.length}</Badge>
              </h2>
              <div className="grid gap-3 md:grid-cols-2">
                {openRequests.map((r) => (
                  <RequestCard
                    key={r.id}
                    request={r}
                    training={trainingById.get(r.trainingId)}
                    onCancel={() => cancelRequest.mutate(r.id)}
                    cancelling={cancelRequest.isPending && cancelRequest.variables === r.id}
                  />
                ))}
              </div>
            </section>
          )}

          {/* À venir & en cours */}
          <section className="mb-6">
            <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
              <CalendarRange className="h-3.5 w-3.5 text-orange-500" />
              {t("mine.upcomingTitle")}
            </h2>
            {upcoming.length === 0 ? (
              <Card>
                <CardContent padding="md">
                  <p className="text-center text-[13px] text-ink-3">{t("mine.upcomingEmpty")}</p>
                </CardContent>
              </Card>
            ) : (
              <div className="grid gap-3 lg:grid-cols-2">
                {upcoming.map((e) => (
                  <UpcomingCard
                    key={e.id}
                    enrollment={e}
                    training={trainingById.get(e.trainingId)}
                    locale={locale}
                    onOpen={() => router.push(`/trainings/${e.trainingId}`)}
                  />
                ))}
              </div>
            )}
          </section>

          {/* Parcours réalisé */}
          <section>
            <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
              <Award className="h-3.5 w-3.5 text-orange-500" />
              {t("mine.completedTitle")}
            </h2>
            {completed.length === 0 ? (
              <Card>
                <CardContent padding="md">
                  <p className="text-center text-[13px] text-ink-3">{t("mine.completedEmpty")}</p>
                </CardContent>
              </Card>
            ) : (
              <div className="overflow-hidden rounded-[20px] border border-line bg-white shadow-sm-brand">
                <table className="w-full border-collapse">
                  <tbody>
                    {completed.map((e) => {
                      const tr = trainingById.get(e.trainingId);
                      return (
                        <tr key={e.id} className="border-b border-line-soft last:border-b-0 hover:bg-bg-soft">
                          <td className="px-5 py-3">
                            <div className="flex items-center gap-2.5">
                              <GraduationCap className="h-4 w-4 shrink-0 text-orange-500" />
                              <div>
                                <div className="text-[13.5px] font-semibold text-ink">
                                  {tr?.intitule ?? e.trainingId.slice(0, 8)}
                                </div>
                                {tr?.organisme && <div className="text-[11px] text-ink-3">{tr.organisme}</div>}
                              </div>
                            </div>
                          </td>
                          <td className="px-3 py-3 font-mono-tabular text-[12px] text-ink-2">
                            {tr?.dateFin ? formatDate(tr.dateFin, { locale }) : "—"}
                          </td>
                          <td className="px-3 py-3">
                            <ScoreBadge note={e.noteEvaluation} label={t("mine.score")} />
                          </td>
                          <td className="px-5 py-3 text-right">
                            {e.attestationFileId ? (
                              <a
                                href={`/api/files/${e.attestationFileId}`}
                                target="_blank"
                                rel="noreferrer"
                                className="inline-flex items-center gap-1.5 text-[12px] font-semibold text-orange-600 hover:text-orange-700"
                              >
                                <Download className="h-3.5 w-3.5" />
                                {t("mine.attestation")}
                              </a>
                            ) : (
                              <span className="text-[11.5px] text-ink-4">{t("mine.noAttestation")}</span>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}

      <RequestDialog
        key={requestOpen ? "request-open" : "request-closed"}
        open={requestOpen}
        onClose={() => setRequestOpen(false)}
        sessions={requestableSessions}
        locale={locale}
        onSuccess={() => {
          setRequestOpen(false);
          invalidate();
        }}
      />
    </>
  );
}

function trainingDays(dateDebut: string | null, dateFin: string | null): number {
  if (!dateDebut) return 0;
  if (!dateFin) return 1;
  const a = new Date(dateDebut).getTime();
  const b = new Date(dateFin).getTime();
  if (Number.isNaN(a) || Number.isNaN(b)) return 1;
  return Math.max(1, Math.round((b - a) / 86_400_000) + 1);
}

function DevelopmentHero({
  training,
  todayMs,
  avgScore,
  attestations,
  completedCount,
  daysThisYear,
  locale,
}: {
  training: TrainingResponse | undefined;
  todayMs: number;
  avgScore: number | null;
  attestations: number;
  completedCount: number;
  daysThisYear: number;
  locale: "fr" | "en";
}) {
  const t = useTranslations("trainings");
  const startMs = training?.dateDebut ? new Date(training.dateDebut).getTime() : 0;
  const startsInDays = training?.dateDebut ? Math.ceil((startMs - todayMs) / 86_400_000) : 0;
  const isUpcoming = training?.status === "PLANNED" && startsInDays > 0;
  const isOngoing = training?.status === "IN_PROGRESS";

  const stats: { icon: typeof Star; label: string; value: string }[] = [
    {
      icon: Star,
      label: t("mine.devStats.avgScore"),
      value: avgScore != null ? `${avgScore.toFixed(1)}/20` : "—",
    },
    { icon: Award, label: t("mine.devStats.attestations"), value: String(attestations) },
    { icon: GraduationCap, label: t("mine.devStats.completed"), value: String(completedCount) },
    { icon: CalendarClock, label: t("mine.devStats.daysThisYear"), value: String(daysThisYear) },
  ];

  return (
    <div className="mb-6 overflow-hidden rounded-[22px] border border-line bg-gradient-to-br from-ink to-[#23314d] text-white shadow-sm">
      <div className="grid gap-6 p-6 md:grid-cols-[1.3fr_1fr] md:p-7">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-[12px] font-medium uppercase tracking-wide text-white/60">
            <BookOpen className="h-4 w-4" />
            {training == null
              ? t("mine.hero.none")
              : isOngoing
                ? t("mine.hero.ongoing")
                : t("mine.hero.upcoming")}
          </div>

          {training == null ? (
            <p className="text-[14px] text-white/70">{t("mine.hero.noneHint")}</p>
          ) : (
            <>
              <div className="flex items-start gap-2.5">
                <GraduationCap className="mt-1 h-5 w-5 shrink-0 text-orange-400" />
                <div>
                  <p className="text-[21px] font-bold leading-tight">{training.intitule}</p>
                  {training.organisme && (
                    <p className="text-[13px] text-white/70">{training.organisme}</p>
                  )}
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-x-5 gap-y-2 pt-1 text-[13px] text-white/80">
                <span className="inline-flex items-center gap-1.5">
                  <CalendarRange className="h-4 w-4 text-white/50" />
                  <span className="font-mono-tabular">
                    {training.dateDebut ? formatDate(training.dateDebut, { locale }) : "—"}
                    {training.dateFin ? ` → ${formatDate(training.dateFin, { locale })}` : ""}
                  </span>
                </span>
                {training.lieu && (
                  <span className="inline-flex items-center gap-1.5">
                    <MapPin className="h-4 w-4 text-white/50" />
                    {training.lieu}
                  </span>
                )}
              </div>
              {isUpcoming && (
                <div className="inline-flex items-center gap-1.5 rounded-full bg-orange-500/20 px-3 py-1 text-[12.5px] font-medium text-orange-200">
                  <CalendarClock className="h-3.5 w-3.5" />
                  {t("mine.hero.startsIn", { count: startsInDays })}
                </div>
              )}
            </>
          )}
        </div>

        <div className="grid grid-cols-2 gap-3">
          {stats.map((s) => (
            <div key={s.label} className="rounded-[14px] bg-white/5 p-3.5">
              <s.icon className="h-4 w-4 text-orange-300" />
              <p className="mt-1.5 font-mono-tabular text-[22px] font-bold leading-none">{s.value}</p>
              <p className="mt-1 text-[11.5px] text-white/60">{s.label}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function ScoreBadge({ note, label }: { note: number | string | null; label: string }) {
  if (note == null) return <span className="text-[11.5px] text-ink-4">—</span>;
  const value = typeof note === "string" ? Number(note) : note;
  if (!Number.isFinite(value)) return <span className="text-[11.5px] text-ink-4">—</span>;
  const tone = value >= 12 ? "success" : value >= 10 ? "warning" : "danger";
  return (
    <Badge tone={tone}>
      {label} {value.toFixed(1)}/20
    </Badge>
  );
}

function RequestCard({
  request,
  training,
  onCancel,
  cancelling,
}: {
  request: TrainingRequestResponse;
  training: TrainingResponse | undefined;
  onCancel: () => void;
  cancelling: boolean;
}) {
  const t = useTranslations("trainings");
  return (
    <Card>
      <CardContent padding="md">
        <div className="mb-2 flex items-start justify-between gap-3">
          <div className="flex items-start gap-2">
            <GraduationCap className="mt-0.5 h-4 w-4 shrink-0 text-orange-500" />
            <div>
              <div className="text-[14px] font-bold text-ink">
                {training?.intitule ?? request.trainingId.slice(0, 8)}
              </div>
              {training?.organisme && <div className="text-[11.5px] text-ink-3">{training.organisme}</div>}
            </div>
          </div>
          <Badge tone={trainingRequestStatusTone(request.status)}>
            {t(`requestStatus.${request.status}`)}
          </Badge>
        </div>
        {request.motivation && (
          <p className="mb-2 rounded-[10px] bg-bg-soft/70 px-3 py-2 text-[12px] text-ink-2">
            <span className="text-[10.5px] font-semibold uppercase tracking-wider text-ink-4">
              {t("mine.motivationLabel")}
            </span>
            <br />
            {request.motivation}
          </p>
        )}
        {request.status === "REJECTED" && request.decisionReason && (
          <p className="mb-2 rounded-[10px] bg-danger-50 px-3 py-2 text-[12px] text-danger-600">
            <span className="text-[10.5px] font-semibold uppercase tracking-wider">
              {t("mine.rejectionLabel")}
            </span>
            <br />
            {request.decisionReason}
          </p>
        )}
        {request.status === "PENDING" && (
          <div className="flex justify-end">
            <Button
              type="button"
              variant="secondary"
              className="!h-8 !px-3 !text-[12px]"
              onClick={onCancel}
              disabled={cancelling}
            >
              {cancelling ? (
                <Loader2 className="h-3.5 w-3.5 animate-spin" />
              ) : (
                <XCircle className="h-3.5 w-3.5" />
              )}
              {t("mine.cancelRequest")}
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function UpcomingCard({
  enrollment,
  training,
  locale,
  onOpen,
}: {
  enrollment: EnrollmentResponse;
  training: TrainingResponse | undefined;
  locale: "fr" | "en";
  onOpen: () => void;
}) {
  const t = useTranslations("trainings");
  return (
    <Card clickable onClick={onOpen}>
      <CardContent padding="md">
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="flex items-start gap-2">
            <GraduationCap className="mt-0.5 h-4 w-4 shrink-0 text-orange-500" />
            <div>
              <div className="text-[14.5px] font-bold text-ink">
                {training?.intitule ?? enrollment.trainingId.slice(0, 8)}
              </div>
              {training?.organisme && <div className="text-[11.5px] text-ink-3">{training.organisme}</div>}
            </div>
          </div>
          {training && (
            <Badge tone={trainingStatusTone(training.status)}>{t(`status.${training.status}`)}</Badge>
          )}
        </div>
        <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 text-[12px] text-ink-2">
          <span className="inline-flex items-center gap-1.5">
            <CalendarRange className="h-3.5 w-3.5 text-ink-4" />
            <span className="font-mono-tabular">
              {training?.dateDebut ? formatDate(training.dateDebut, { locale }) : "—"}
              {training?.dateFin ? ` → ${formatDate(training.dateFin, { locale })}` : ""}
            </span>
          </span>
          {training?.lieu && (
            <span className="inline-flex items-center gap-1.5">
              <MapPin className="h-3.5 w-3.5 text-ink-4" />
              {training.lieu}
            </span>
          )}
          <Badge tone={enrollmentStatusTone(enrollment.status)}>
            {t(`enrollmentStatus.${enrollment.status}`)}
          </Badge>
        </div>
      </CardContent>
    </Card>
  );
}

function RequestDialog({
  open,
  onClose,
  sessions,
  locale,
  onSuccess,
}: {
  open: boolean;
  onClose: () => void;
  sessions: TrainingResponse[];
  locale: "fr" | "en";
  onSuccess: () => void;
}) {
  const t = useTranslations("trainings");
  const tCommon = useTranslations("common");
  const [trainingId, setTrainingId] = React.useState("");
  const [motivation, setMotivation] = React.useState("");

  const submit = useMutation({
    mutationFn: () =>
      apiFetch<TrainingRequestResponse>("/api/hrm/trainings/requests", {
        method: "POST",
        body: { trainingId, motivation: motivation.trim() || null },
      }),
    onSuccess: () => {
      toast.success(t("mine.request.success"));
      onSuccess();
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error("—");
    },
  });

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={
        <span className="flex items-center gap-2">
          <GraduationCap className="h-5 w-5 text-orange-500" />
          {t("mine.request.title")}
        </span>
      }
      subtitle={t("mine.request.subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!trainingId || submit.isPending}
            onClick={() => submit.mutate()}
          >
            {submit.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              t("mine.request.submit")
            )}
          </Button>
        </>
      }
    >
      {sessions.length === 0 ? (
        <p className="py-4 text-center text-[13px] text-ink-3">{t("mine.request.noSession")}</p>
      ) : (
        <div className="space-y-4">
          <Field label={t("mine.request.sessionLabel")}>
            <select
              value={trainingId}
              onChange={(e) => setTrainingId(e.target.value)}
              className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
            >
              <option value="">{t("mine.request.sessionPlaceholder")}</option>
              {sessions.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.intitule}
                  {s.dateDebut ? ` · ${formatDate(s.dateDebut, { locale })}` : ""}
                </option>
              ))}
            </select>
          </Field>
          <Field label={t("mine.request.motivationLabel")}>
            <Textarea
              rows={4}
              value={motivation}
              onChange={(e) => setMotivation(e.target.value)}
              placeholder={t("mine.request.motivationPlaceholder")}
            />
          </Field>
        </div>
      )}
    </Dialog>
  );
}
