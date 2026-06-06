"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { Loader2, Send } from "lucide-react";
import { useTranslations } from "next-intl";
import { useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Textarea } from "@/components/ui/input";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { MissionOrderResponse } from "@/server/ksm/modules/missions";

type FormValues = {
  employeeId: string;
  destination: string;
  objet: string;
  dateDebut: string;
  dateFin: string;
  montantAvance: string;
  centreCout: string;
};

export function NewMissionForm() {
  const t = useTranslations("missions");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();
  const today = new Date().toISOString().slice(0, 10);

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: {
      employeeId: "",
      destination: "",
      objet: "",
      dateDebut: today,
      dateFin: today,
      montantAvance: "",
      centreCout: "",
    },
  });

  const start = useWatch({ control, name: "dateDebut" });
  const end = useWatch({ control, name: "dateFin" });
  const datesInvalid = !!start && !!end && new Date(end) < new Date(start);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const createMutation = useMutation({
    mutationFn: (v: FormValues) =>
      apiFetch<MissionOrderResponse>("/api/hrm/mission-orders", {
        method: "POST",
        body: {
          employeeId: v.employeeId,
          destination: v.destination.trim(),
          objet: v.objet.trim(),
          dateDebut: v.dateDebut,
          dateFin: v.dateFin,
          montantAvance: v.montantAvance ? Number(v.montantAvance) : null,
          centreCout: v.centreCout.trim() || null,
        },
      }),
    onError: handleError,
  });

  async function submit(values: FormValues, alsoIssue: boolean) {
    try {
      const order = await createMutation.mutateAsync(values);
      if (alsoIssue) {
        try {
          await apiFetch<MissionOrderResponse>(`/api/hrm/mission-orders/${order.id}/issue`, {
            method: "POST",
          });
          toast.success(t("detail.issueSuccess"));
        } catch (cause) {
          handleError(cause);
        }
      } else {
        toast.success(t("new.success"));
      }
      router.push(`/mission-orders/${order.id}`);
    } catch {
      // toast already shown
    }
  }

  return (
    <form onSubmit={handleSubmit((v) => submit(v, false))}>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("queue.title"), href: "/mission-orders" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
        actions={
          <>
            <Link href="/mission-orders">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button
              type="button"
              variant="secondary"
              disabled={!isValid || datesInvalid || createMutation.isPending}
              onClick={handleSubmit((v) => submit(v, false))}
            >
              {createMutation.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : null}
              {t("new.submit")}
            </Button>
            <Button
              type="button"
              disabled={!isValid || datesInvalid || createMutation.isPending}
              onClick={handleSubmit((v) => submit(v, true))}
            >
              <Send className="h-4 w-4" />
              {t("new.submitAndIssue")}
            </Button>
          </>
        }
      />

      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field
              label={t("new.fields.employee")}
              error={errors.employeeId && tVal("required")}
              className="col-span-2"
            >
              <select
                {...register("employeeId", { required: true })}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                <option value="">{t("new.fields.employeePlaceholder")}</option>
                {(employeesQuery.data ?? []).map((e) => (
                  <option key={e.id} value={e.id}>
                    {e.matricule} · {e.actorDisplayName ?? e.actorId.slice(0, 8)}
                  </option>
                ))}
              </select>
            </Field>
            <Field
              label={t("new.fields.destination")}
              error={errors.destination && tVal("required")}
            >
              <Input
                placeholder={t("new.fields.destinationPlaceholder")}
                {...register("destination", { required: true, minLength: 2 })}
              />
            </Field>
            <Field label={t("new.fields.centreCout")}>
              <Input {...register("centreCout")} />
            </Field>
            <Field
              label={t("new.fields.objet")}
              error={errors.objet && tVal("required")}
              className="col-span-2"
            >
              <Textarea
                rows={2}
                placeholder={t("new.fields.objetPlaceholder")}
                {...register("objet", { required: true, minLength: 4 })}
              />
            </Field>
            <Field
              label={t("new.fields.dateDebut")}
              error={errors.dateDebut && tVal("required")}
            >
              <Input type="date" {...register("dateDebut", { required: true })} />
            </Field>
            <Field
              label={t("new.fields.dateFin")}
              error={datesInvalid ? tVal("dateRange.endBeforeStart") : undefined}
            >
              <Input
                type="date"
                min={start || today}
                {...register("dateFin", { required: true })}
              />
            </Field>
            <Field label={t("new.fields.montantAvance")} className="col-span-2">
              <Input
                type="number"
                min={0}
                step={1000}
                {...register("montantAvance")}
              />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
