"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, Loader2, Plus, Play } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Textarea } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { taskStatusTone } from "@/lib/recruitment-status";
import type {
  CreateOnboardingTaskRequest,
  OnboardingTaskResponse,
} from "@/server/ksm/modules/recruitment";

/**
 * Reusable onboarding tasks board for an employee. Drops cleanly inside the
 * employee 360° tab — manager/HR can add, start and complete tasks.
 */
export function OnboardingTasks({ employeeId, readOnly }: { employeeId: string; readOnly?: boolean }) {
  const t = useTranslations("recruitment");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canCreate = useCan("hrm:onboarding:create") && !readOnly;
  const canManage = useCan("hrm:onboarding:manage") && !readOnly;
  const [showAdd, setShowAdd] = React.useState(false);

  const query = useQuery({
    queryKey: ["hrm", "onboarding-tasks", employeeId],
    queryFn: () =>
      apiFetch<OnboardingTaskResponse[]>(`/api/hrm/onboarding-tasks/employee/${employeeId}`),
  });

  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: ["hrm", "onboarding-tasks", employeeId] });
  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const transition = (id: string, kind: "start" | "complete") =>
    apiFetch(`/api/hrm/onboarding-tasks/${id}/${kind}`, { method: "POST" });

  const startM = useMutation({
    mutationFn: (id: string) => transition(id, "start"),
    onSuccess: () => {
      toast.success(t("onboarding.startSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const completeM = useMutation({
    mutationFn: (id: string) => transition(id, "complete"),
    onSuccess: () => {
      toast.success(t("onboarding.completeSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  return (
    <>
      <div className="mb-3 flex items-center justify-between">
        <span className="text-[13px] font-bold tracking-tight text-ink">
          {t("onboarding.title")}
        </span>
        {canCreate && (
          <Button
            type="button"
            variant="secondary"
            className="!h-8 !px-3 !text-[12px]"
            onClick={() => setShowAdd(true)}
          >
            <Plus className="h-3.5 w-3.5" />
            {t("onboarding.addTitle")}
          </Button>
        )}
      </div>
      <Card>
        {query.isLoading ? (
          <div className="grid place-items-center py-10">
            <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
          </div>
        ) : query.error ? (
          <div className="px-5 py-10 text-center text-ink-3">
            {query.error instanceof BffApiError ? query.error.message : "—"}
          </div>
        ) : (query.data ?? []).length === 0 ? (
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("onboarding.empty")}</p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("onboarding.columns.title")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("onboarding.columns.deadline")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("onboarding.columns.status")}
                </th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {(query.data ?? []).map((task) => (
                <tr key={task.id} className="border-t border-line-soft">
                  <td className="px-5 py-3">
                    <div className="text-[13.5px] font-semibold text-ink">{task.titre}</div>
                    {task.description && (
                      <div className="text-[11.5px] text-ink-3">{task.description}</div>
                    )}
                  </td>
                  <td className="px-3 py-3 font-mono-tabular text-[12.5px] text-ink-2">
                    {task.echeance ? formatDate(task.echeance, { locale }) : "—"}
                  </td>
                  <td className="px-3 py-3">
                    <Badge tone={taskStatusTone(task.status)}>
                      {t(`taskStatus.${task.status}`)}
                    </Badge>
                  </td>
                  <td className="px-5 py-3 text-right">
                    {canManage && task.status === "PENDING" && (
                      <Button
                        type="button"
                        variant="ghost"
                        className="!h-7 !px-2.5 !text-[11px]"
                        onClick={() => startM.mutate(task.id)}
                      >
                        <Play className="h-3 w-3" />
                        {t("onboarding.actions.start")}
                      </Button>
                    )}
                    {canManage && task.status === "IN_PROGRESS" && (
                      <Button
                        type="button"
                        variant="secondary"
                        className="!h-7 !px-2.5 !text-[11px]"
                        onClick={() => completeM.mutate(task.id)}
                      >
                        <CheckCircle2 className="h-3 w-3" />
                        {t("onboarding.actions.complete")}
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <AddTaskDialog
        open={showAdd}
        onClose={() => setShowAdd(false)}
        employeeId={employeeId}
        onCreated={() => {
          setShowAdd(false);
          invalidate();
        }}
      />
    </>
  );
}

function AddTaskDialog({
  open,
  onClose,
  employeeId,
  onCreated,
}: {
  open: boolean;
  onClose: () => void;
  employeeId: string;
  onCreated: () => void;
}) {
  const t = useTranslations("recruitment.onboarding");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");

  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ titre: string; description: string; echeance: string }>({ mode: "onChange" });
  React.useEffect(() => {
    if (!open) reset({ titre: "", description: "", echeance: "" });
  }, [open, reset]);

  const create = useMutation({
    mutationFn: (v: { titre: string; description: string; echeance: string }) => {
      const body: CreateOnboardingTaskRequest = {
        employeeId,
        titre: v.titre.trim(),
        description: v.description.trim() || null,
        echeance: v.echeance || null,
      };
      return apiFetch("/api/hrm/onboarding-tasks", { method: "POST", body });
    },
    onSuccess: () => {
      toast.success(t("addSuccess"));
      onCreated();
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

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
            disabled={!isValid || create.isPending}
            onClick={handleSubmit((v) => create.mutate(v))}
          >
            {create.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
            {tCommon("actions.create")}
          </Button>
        </>
      }
    >
      <Field label={t("fields.titre")}>
        <Input {...register("titre", { required: true, minLength: 3 })} />
      </Field>
      <Field label={t("fields.description")}>
        <Textarea rows={3} {...register("description")} />
      </Field>
      <Field label={t("fields.echeance")}>
        <Input type="date" {...register("echeance")} />
      </Field>
    </Dialog>
  );
}
