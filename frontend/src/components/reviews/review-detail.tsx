"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Check, ChevronLeft, Loader2, Plus, Send, Star, Target } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label, Textarea } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { reviewStatusTone } from "@/lib/training-status";
import type { ObjectiveResponse, ReviewResponse, ReviewStatus } from "@/server/ksm/modules/reviews";

export function ReviewDetail({ reviewId }: { reviewId: string }) {
  const t = useTranslations("reviews");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();
  const canManage = useCan("hrm:review:manage");
  const [showSubmit, setShowSubmit] = React.useState(false);
  const [showAddObj, setShowAddObj] = React.useState(false);
  const [evalObj, setEvalObj] = React.useState<ObjectiveResponse | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "review", reviewId],
    queryFn: () => apiFetch<ReviewResponse>(`/api/hrm/reviews/${reviewId}`),
  });
  const objectivesQuery = useQuery({
    queryKey: ["hrm", "review", reviewId, "objectives"],
    queryFn: () => apiFetch<ObjectiveResponse[]>(`/api/hrm/reviews/${reviewId}/objectives`),
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "review", reviewId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "reviews"] });
  }, [queryClient, reviewId]);
  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const submitM = useMutation({
    mutationFn: (body: { noteGlobale: number; commentaires: string; planAction: string }) =>
      apiFetch(`/api/hrm/reviews/${reviewId}/submit`, { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("detail.submitSuccess"));
      setShowSubmit(false);
      invalidate();
    },
    onError: handleError,
  });
  const ackM = useMutation({
    mutationFn: () => apiFetch(`/api/hrm/reviews/${reviewId}/acknowledge`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("detail.acknowledgeSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const finM = useMutation({
    mutationFn: () => apiFetch(`/api/hrm/reviews/${reviewId}/finalize`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("detail.finalizeSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const addObjM = useMutation({
    mutationFn: (body: { description: string; poids: number }) =>
      apiFetch(`/api/hrm/reviews/${reviewId}/objectives`, { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("detail.objectives.addSuccess"));
      setShowAddObj(false);
      queryClient.invalidateQueries({ queryKey: ["hrm", "review", reviewId, "objectives"] });
    },
    onError: handleError,
  });
  const evalObjM = useMutation({
    mutationFn: (body: { id: string; noteAtteinte: number; commentaire: string }) =>
      apiFetch(`/api/hrm/reviews/objectives/${body.id}/evaluate`, {
        method: "POST",
        body: { noteAtteinte: body.noteAtteinte, commentaire: body.commentaire },
      }),
    onSuccess: () => {
      toast.success(t("detail.objectives.evaluateSuccess"));
      setEvalObj(null);
      queryClient.invalidateQueries({ queryKey: ["hrm", "review", reviewId, "objectives"] });
    },
    onError: handleError,
  });

  if (query.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (query.error || !query.data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {query.error instanceof BffApiError ? query.error.message : "—"}
      </div>
    );
  }
  const r = query.data;
  const objectives = objectivesQuery.data ?? [];

  const showSubmitBtn = canManage && r.status === "DRAFT";
  const showAck = r.status === "SUBMITTED" && canManage;
  const showFinalize = r.status === "ACKNOWLEDGED" && canManage;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/reviews" },
          { label: shortRef(r.id) },
        ]}
        title={
          <span className="flex items-center gap-3">
            <Star className="h-6 w-6 text-orange-500" />
            {t("queue.title", { periode: r.periode })}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={reviewStatusTone(r.status)}>{t(`status.${r.status}`)}</Badge>
            <span className="text-[12.5px] text-ink-3">
              {t("detail.evaluator")} · {r.evaluateurDisplayName ?? "—"}
            </span>
          </span>
        }
        actions={
          <>
            <Link href="/reviews">
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("detail.back")}
              </Button>
            </Link>
            {showSubmitBtn && (
              <Button type="button" onClick={() => setShowSubmit(true)}>
                <Send className="h-4 w-4" />
                {t("detail.actions.submit")}
              </Button>
            )}
            {showAck && (
              <Button type="button" variant="secondary" onClick={() => ackM.mutate()} disabled={ackM.isPending}>
                {ackM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4" />}
                {t("detail.actions.acknowledge")}
              </Button>
            )}
            {showFinalize && (
              <Button type="button" onClick={() => finM.mutate()} disabled={finM.isPending}>
                {finM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4" />}
                {t("detail.actions.finalize")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={workflowSteps(r.status, t)} />
        </CardContent>
      </Card>

      <Card className="mb-6">
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-4">
            <Detail label={t("detail.periode")} value={r.periode} mono />
            <Detail label={t("detail.evaluator")} value={r.evaluateurDisplayName ?? "—"} />
            <Detail
              label={t("detail.score")}
              value={
                r.noteGlobale != null ? (
                  <span className="flex items-center gap-1.5">
                    <span className="font-display font-mono-tabular text-[20px] font-extrabold text-ink">
                      {Number(r.noteGlobale).toFixed(1)}
                    </span>
                    <span className="text-[12px] text-ink-3">/ 5</span>
                  </span>
                ) : (
                  "—"
                )
              }
            />
          </div>
          {(r.commentaires || r.planAction) && (
            <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
              {r.commentaires && (
                <div>
                  <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                    {t("detail.comments")}
                  </Label>
                  <p className="mt-1 whitespace-pre-line text-[13.5px] text-ink-2">{r.commentaires}</p>
                </div>
              )}
              {r.planAction && (
                <div>
                  <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                    {t("detail.planAction")}
                  </Label>
                  <p className="mt-1 whitespace-pre-line text-[13.5px] text-ink-2">{r.planAction}</p>
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      <div className="mb-3 flex items-center justify-between">
        <span className="flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
          <Target className="h-4 w-4 text-orange-500" />
          {t("detail.objectives.title")}
        </span>
        {canManage && r.status === "DRAFT" && (
          <Button type="button" variant="secondary" className="!h-8 !px-3 !text-[12px]" onClick={() => setShowAddObj(true)}>
            <Plus className="h-3.5 w-3.5" />
            {t("detail.objectives.addTitle")}
          </Button>
        )}
      </div>
      <Card>
        {objectives.length === 0 ? (
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("detail.objectives.empty")}</p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.objectives.columns.description")}
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.objectives.columns.poids")}
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.objectives.columns.note")}
                </th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {objectives.map((o) => (
                <tr key={o.id} className="border-t border-line-soft">
                  <td className="px-5 py-3">
                    <div className="text-[13.5px] font-semibold text-ink">{o.description}</div>
                    {o.commentaire && (
                      <div className="text-[11.5px] text-ink-3">{o.commentaire}</div>
                    )}
                  </td>
                  <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">
                    {o.poids != null ? `${Number(o.poids).toFixed(0)}%` : "—"}
                  </td>
                  <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-bold text-ink">
                    {o.noteAtteinte != null ? Number(o.noteAtteinte).toFixed(1) : "—"}
                  </td>
                  <td className="px-5 py-3 text-right">
                    {canManage && r.status !== "FINALIZED" && (
                      <Button
                        type="button"
                        variant="ghost"
                        className="!h-7 !px-2.5 !text-[11px]"
                        onClick={() => setEvalObj(o)}
                      >
                        {t("detail.objectives.evaluateTitle")}
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <SubmitDialog
        open={showSubmit}
        loading={submitM.isPending}
        onClose={() => setShowSubmit(false)}
        onConfirm={(v) => submitM.mutate(v)}
      />
      <AddObjectiveDialog
        open={showAddObj}
        loading={addObjM.isPending}
        onClose={() => setShowAddObj(false)}
        onConfirm={(v) => addObjM.mutate(v)}
      />
      <EvaluateDialog
        objective={evalObj}
        loading={evalObjM.isPending}
        onClose={() => setEvalObj(null)}
        onConfirm={(noteAtteinte, commentaire) =>
          evalObj && evalObjM.mutate({ id: evalObj.id, noteAtteinte, commentaire })
        }
      />
    </>
  );
}

function Detail({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div>
      <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">{label}</Label>
      <p className={mono ? "mt-0.5 font-mono-tabular text-[13.5px] font-bold text-ink" : "mt-0.5 text-[14px] font-medium text-ink"}>
        {value}
      </p>
    </div>
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

function shortRef(uuid: string): string {
  return `EV-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}

function SubmitDialog({
  open,
  loading,
  onClose,
  onConfirm,
}: {
  open: boolean;
  loading: boolean;
  onClose: () => void;
  onConfirm: (v: { noteGlobale: number; commentaires: string; planAction: string }) => void;
}) {
  const t = useTranslations("reviews.detail");
  const tCommon = useTranslations("common");
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ noteGlobale: string; commentaires: string; planAction: string }>({
    mode: "onChange",
    defaultValues: { noteGlobale: "4", commentaires: "", planAction: "" },
  });
  React.useEffect(() => {
    if (!open) reset({ noteGlobale: "4", commentaires: "", planAction: "" });
  }, [open, reset]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      size="lg"
      title={t("submitTitle")}
      subtitle={t("submitSubtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || loading}
            onClick={handleSubmit((v) =>
              onConfirm({
                noteGlobale: Number(v.noteGlobale),
                commentaires: v.commentaires,
                planAction: v.planAction,
              }),
            )}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
            {t("submitConfirm")}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-3 md:grid-cols-[120px_1fr]">
        <Field label={t("noteLabel")}>
          <Input type="number" step={0.1} min={0} max={5} {...register("noteGlobale", { required: true, min: 0, max: 5 })} />
        </Field>
        <Field label={t("commentsLabel")} className="md:col-span-1">
          <Textarea rows={3} {...register("commentaires", { required: true, minLength: 5 })} />
        </Field>
        <Field label={t("planActionLabel")} className="md:col-span-2">
          <Textarea rows={3} {...register("planAction")} />
        </Field>
      </div>
    </Dialog>
  );
}

function AddObjectiveDialog({
  open,
  loading,
  onClose,
  onConfirm,
}: {
  open: boolean;
  loading: boolean;
  onClose: () => void;
  onConfirm: (v: { description: string; poids: number }) => void;
}) {
  const t = useTranslations("reviews.detail.objectives");
  const tCommon = useTranslations("common");
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ description: string; poids: string }>({ mode: "onChange" });
  React.useEffect(() => {
    if (!open) reset({ description: "", poids: "" });
  }, [open, reset]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("addTitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || loading}
            onClick={handleSubmit((v) =>
              onConfirm({ description: v.description.trim(), poids: Number(v.poids || 0) }),
            )}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
            {t("addConfirm")}
          </Button>
        </>
      }
    >
      <Field label={t("descriptionLabel")}>
        <Textarea
          rows={2}
          placeholder={t("descriptionPlaceholder")}
          {...register("description", { required: true, minLength: 4 })}
        />
      </Field>
      <Field label={t("poidsLabel")}>
        <Input type="number" min={0} max={100} step={5} {...register("poids", { required: true })} />
      </Field>
    </Dialog>
  );
}

function EvaluateDialog({
  objective,
  loading,
  onClose,
  onConfirm,
}: {
  objective: ObjectiveResponse | null;
  loading: boolean;
  onClose: () => void;
  onConfirm: (noteAtteinte: number, commentaire: string) => void;
}) {
  const t = useTranslations("reviews.detail.objectives");
  const tCommon = useTranslations("common");
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ noteAtteinte: string; commentaire: string }>({ mode: "onChange" });
  React.useEffect(() => {
    if (!objective) reset({ noteAtteinte: "", commentaire: "" });
  }, [objective, reset]);

  return (
    <Dialog
      open={!!objective}
      onClose={onClose}
      title={t("evaluateTitle")}
      subtitle={objective?.description}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || loading}
            onClick={handleSubmit((v) => onConfirm(Number(v.noteAtteinte), v.commentaire))}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4" />}
            {t("evaluateConfirm")}
          </Button>
        </>
      }
    >
      <Field label={t("noteLabel")}>
        <Input type="number" step={0.1} min={0} max={5} {...register("noteAtteinte", { required: true })} />
      </Field>
      <Field label={t("commentLabel")}>
        <Textarea rows={2} {...register("commentaire")} />
      </Field>
    </Dialog>
  );
}

