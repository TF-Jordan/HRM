"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, ChevronLeft, Clock, Loader2, Send } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatPeriod } from "@/lib/format";
import { timesheetStatusTone, timesheetTotalHours } from "@/lib/timesheet-status";
import type { TimesheetResponse } from "@/server/ksm/modules/timesheets";

export function TimesheetDetail({ timesheetId }: { timesheetId: string }) {
  const t = useTranslations("timesheets");
  const tDetail = useTranslations("timesheets.detail");
  const tHours = useTranslations("timesheets.hours");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();

  const canCreate = useCan("hrm:timesheet:create");
  const canValidate = useCan("hrm:timesheet:validate");

  const query = useQuery({
    queryKey: ["hrm", "timesheet", timesheetId],
    queryFn: () => apiFetch<TimesheetResponse>(`/api/hrm/timesheets/${timesheetId}`),
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "timesheet", timesheetId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "timesheets"] });
  }, [queryClient, timesheetId]);

  function handleApiError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const submitMutation = useMutation({
    mutationFn: () =>
      apiFetch<TimesheetResponse>(`/api/hrm/timesheets/${timesheetId}/submit`, { method: "POST" }),
    onSuccess: () => {
      toast.success(tDetail("submitSuccess"));
      invalidate();
    },
    onError: handleApiError,
  });

  const validateMutation = useMutation({
    mutationFn: () =>
      apiFetch<TimesheetResponse>(`/api/hrm/timesheets/${timesheetId}/validate`, {
        method: "POST",
      }),
    onSuccess: () => {
      toast.success(tDetail("validateSuccess"));
      invalidate();
    },
    onError: handleApiError,
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
        {query.error instanceof BffApiError ? query.error.message : "Not found"}
      </div>
    );
  }

  const ts = query.data;
  const isDraft = ts.status === "DRAFT";
  const isSubmitted = ts.status === "SUBMITTED";

  const steps: WorkflowStep[] = [
    { key: "draft", label: t("status.DRAFT"), state: ts.status === "DRAFT" ? "active" : "done" },
    {
      key: "submitted",
      label: t("status.SUBMITTED"),
      state:
        ts.status === "SUBMITTED" ? "active" : ts.status === "VALIDATED" ? "done" : "pending",
    },
    {
      key: "validated",
      label: t("status.VALIDATED"),
      state: ts.status === "VALIDATED" ? "done" : "pending",
    },
  ];

  const num = (v: number | string) => Number(v).toFixed(1);
  const rows: Array<[string, string, boolean?]> = [
    [tHours("normales"), num(ts.heuresNormales)],
    [tHours("supplementaires"), num(ts.heuresSupplementaires)],
    [tHours("nuit"), num(ts.heuresNuit)],
    [tHours("weekend"), num(ts.heuresWeekend)],
    [tHours("absences"), num(ts.absencesNonJustifiees), true],
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("admin.title"), href: "/timesheets" },
          { label: formatPeriod(ts.periode, "fr") },
        ]}
        title={
          <span className="flex items-center gap-3 capitalize">
            <Clock className="h-6 w-6 text-orange-500" />
            {formatPeriod(ts.periode, "fr")}
          </span>
        }
        subtitle={
          <span className="flex items-center gap-2">
            <Badge tone={timesheetStatusTone(ts.status)}>{t(`status.${ts.status}`)}</Badge>
            <span className="font-mono-tabular text-ink-3">
              {timesheetTotalHours(ts).toFixed(1)} h
            </span>
          </span>
        }
        actions={
          <>
            <Link href="/timesheets">
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("my.title")}
              </Button>
            </Link>
            {canCreate && isDraft && (
              <Button
                type="button"
                onClick={() => submitMutation.mutate()}
                disabled={submitMutation.isPending}
              >
                {submitMutation.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Send className="h-4 w-4" />
                )}
                {tDetail("actions.submit")}
              </Button>
            )}
            {canValidate && isSubmitted && (
              <Button
                type="button"
                onClick={() => validateMutation.mutate()}
                disabled={validateMutation.isPending}
              >
                {validateMutation.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <CheckCircle2 className="h-4 w-4" />
                )}
                {tDetail("actions.validate")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={steps} />
        </CardContent>
      </Card>

      <Card>
        <CardContent padding="lg">
          <p className="mb-4 text-[11px] font-bold uppercase tracking-[0.1em] text-ink-3">
            {tDetail("breakdown")}
          </p>
          <div className="divide-y divide-line-soft">
            {rows.map(([label, value, danger]) => (
              <div key={label} className="flex items-center justify-between py-3">
                <Label className="text-[13.5px] text-ink-2">{label}</Label>
                <span
                  className={`font-mono-tabular text-[15px] font-semibold ${
                    danger && Number(value) > 0 ? "text-danger-600" : "text-ink"
                  }`}
                >
                  {value}
                </span>
              </div>
            ))}
            <div className="flex items-center justify-between py-3.5">
              <span className="text-[13.5px] font-bold uppercase tracking-wider text-ink">
                {tHours("total")}
              </span>
              <span className="font-mono-tabular text-[18px] font-extrabold text-orange-700">
                {timesheetTotalHours(ts).toFixed(1)}
              </span>
            </div>
          </div>
          <p className="mt-5 text-[12px] text-ink-4">
            ID · <span className="font-mono-tabular">{ts.id}</span>
          </p>
        </CardContent>
      </Card>
    </>
  );
}
