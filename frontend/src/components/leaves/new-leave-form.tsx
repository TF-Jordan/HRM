"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { CalendarRange, Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Textarea } from "@/components/ui/input";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { workingDaysBetween } from "@/lib/holidays-cm";
import type { EmployeeResponse, LeaveType } from "@/server/ksm/modules/employees";
import type { LeaveResponse } from "@/server/ksm/modules/leaves";

type FormValues = {
  type: LeaveType;
  dateDebut: string;
  dateFin: string;
  motif: string;
};

type MinePayload = {
  employee: EmployeeResponse | null;
  leaves: LeaveResponse[];
};

export function NewLeaveForm() {
  const t = useTranslations("leaves");
  const tNew = useTranslations("leaves.new");
  const tEmpType = useTranslations("employees.leaveType");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();

  const today = new Date().toISOString().slice(0, 10);
  const mine = useQuery({
    queryKey: ["hrm", "leaves", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/leaves/mine"),
  });

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: { type: "ANNUAL", dateDebut: today, dateFin: today, motif: "" },
  });

  const start = watch("dateDebut");
  const end = watch("dateFin");
  const days = React.useMemo(() => workingDaysBetween(start, end), [start, end]);
  const datesInvalid = !!start && !!end && new Date(end) < new Date(start);

  const mutation = useMutation({
    mutationFn: async (v: FormValues) => {
      const employeeId = mine.data?.employee?.id;
      if (!employeeId) throw new Error("No employee record");
      return apiFetch<LeaveResponse>("/api/hrm/leaves", {
        method: "POST",
        body: {
          employeeId,
          type: v.type,
          dateDebut: v.dateDebut,
          dateFin: v.dateFin,
          motif: v.motif.trim() || undefined,
        },
      });
    },
    onSuccess: (leave) => {
      toast.success(tNew("success"));
      router.push(`/leaves/${leave.id}`);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  return (
    <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: "Mes congés", href: "/leaves" },
          { label: tNew("title") },
        ]}
        title={tNew("title")}
        subtitle={tNew("subtitle")}
        actions={
          <>
            <Link href="/leaves">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button type="submit" disabled={!isValid || datesInvalid || mutation.isPending}>
              {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : tNew("submit")}
            </Button>
          </>
        }
      />

      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field label={tNew("fields.type")}>
              <select
                {...register("type", { required: true })}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                <option value="ANNUAL">{tEmpType("ANNUAL")}</option>
                <option value="SICK">{tEmpType("SICK")}</option>
                <option value="MATERNITY">{tEmpType("MATERNITY")}</option>
                <option value="PATERNITY">{tEmpType("PATERNITY")}</option>
                <option value="UNPAID">{tEmpType("UNPAID")}</option>
                <option value="SPECIAL">{tEmpType("SPECIAL")}</option>
              </select>
            </Field>
            <Field
              label={tNew("fields.duration")}
              hint={tNew("hint")}
              error={datesInvalid ? tVal("dateRange.endBeforeStart") : undefined}
            >
              <div className="flex items-center gap-2 rounded-[11px] border border-line bg-bg-soft px-3.5 py-[11px]">
                <CalendarRange className="h-4 w-4 text-orange-600" />
                <span className="font-mono-tabular text-[14px] font-bold text-ink">
                  {days.toFixed(1)}
                </span>
                <span className="text-[12.5px] text-ink-3">jours ouvrables</span>
              </div>
            </Field>
            <Field
              label={tNew("fields.dateDebut")}
              error={errors.dateDebut && errors.dateDebut.message}
            >
              <Input type="date" min={today} {...register("dateDebut", { required: true })} />
            </Field>
            <Field
              label={tNew("fields.dateFin")}
              error={errors.dateFin && errors.dateFin.message}
            >
              <Input
                type="date"
                min={start || today}
                {...register("dateFin", { required: true })}
              />
            </Field>
          </div>
          <div className="mt-4">
            <Field label={tNew("fields.motif")}>
              <Textarea rows={3} {...register("motif")} />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
