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
import { Field, Input } from "@/components/ui/input";
import { SectionTitle } from "@/components/ui/section-title";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { holidaysCmForYear } from "@/lib/holidays-cm";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { TimesheetResponse } from "@/server/ksm/modules/timesheets";

type FormValues = {
  periode: string;
  heuresNormales: string;
  heuresSupplementaires: string;
  heuresNuit: string;
  heuresWeekend: string;
  absencesNonJustifiees: string;
};

type MinePayload = { employee: EmployeeResponse | null };

function currentPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

function holidaysInMonth(periode: string): number {
  const [y, m] = periode.split("-").map(Number);
  if (!y || !m) return 0;
  const prefix = `${y}-${String(m).padStart(2, "0")}`;
  let count = 0;
  for (const iso of holidaysCmForYear(y)) {
    if (iso.startsWith(prefix)) count += 1;
  }
  return count;
}

export function NewTimesheetForm({ initialPeriode }: { initialPeriode?: string }) {
  const t = useTranslations("timesheets");
  const tNew = useTranslations("timesheets.new");
  const tHours = useTranslations("timesheets.hours");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const router = useRouter();

  const mine = useQuery({
    queryKey: ["hrm", "timesheets", "mine-employee"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/timesheets/mine"),
  });

  const {
    register,
    handleSubmit,
    watch,
    formState: { isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: {
      periode: initialPeriode ?? currentPeriode(),
      heuresNormales: "",
      heuresSupplementaires: "0",
      heuresNuit: "0",
      heuresWeekend: "0",
      absencesNonJustifiees: "0",
    },
  });

  const periode = watch("periode");
  const holidays = React.useMemo(() => holidaysInMonth(periode), [periode]);

  const mutation = useMutation({
    mutationFn: async (v: FormValues) => {
      const employeeId = mine.data?.employee?.id;
      if (!employeeId) throw new Error("No employee record");
      return apiFetch<TimesheetResponse>("/api/hrm/timesheets", {
        method: "POST",
        body: {
          employeeId,
          periode: v.periode,
          heuresNormales: Number(v.heuresNormales || 0),
          heuresSupplementaires: Number(v.heuresSupplementaires || 0),
          heuresNuit: Number(v.heuresNuit || 0),
          heuresWeekend: Number(v.heuresWeekend || 0),
          absencesNonJustifiees: Number(v.absencesNonJustifiees || 0),
        },
      });
    },
    onSuccess: (ts) => {
      toast.success(tNew("success"));
      router.push(`/timesheets/${ts.id}`);
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
          { label: t("my.title"), href: "/timesheets/my" },
          { label: tNew("title") },
        ]}
        title={tNew("title")}
        subtitle={tNew("subtitle")}
        actions={
          <>
            <Link href="/timesheets/my">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button type="submit" disabled={!isValid || mutation.isPending}>
              {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : tNew("submit")}
            </Button>
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <div className="flex flex-wrap items-end gap-4">
            <Field label={tNew("fields.periode")} className="w-[220px]">
              <Input type="month" max={currentPeriode()} {...register("periode", { required: true })} />
            </Field>
            <div className="flex items-center gap-2 rounded-[11px] border border-line bg-bg-soft px-3.5 py-[11px]">
              <CalendarRange className="h-4 w-4 text-orange-600" />
              <span className="text-[12.5px] text-ink-3">
                {tNew("holidaysHint", { count: holidays })}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      <SectionTitle>{tHours("total")}</SectionTitle>
      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4 md:grid-cols-3">
            <Field label={tHours("normales")}>
              <Input
                type="number"
                min="0"
                step="0.5"
                {...register("heuresNormales", { required: true, min: 0 })}
                placeholder="160"
              />
            </Field>
            <Field label={tHours("supplementaires")}>
              <Input type="number" min="0" step="0.5" {...register("heuresSupplementaires")} />
            </Field>
            <Field label={tHours("nuit")}>
              <Input type="number" min="0" step="0.5" {...register("heuresNuit")} />
            </Field>
            <Field label={tHours("weekend")}>
              <Input type="number" min="0" step="0.5" {...register("heuresWeekend")} />
            </Field>
            <Field label={tHours("absences")}>
              <Input type="number" min="0" step="0.5" {...register("absencesNonJustifiees")} />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
