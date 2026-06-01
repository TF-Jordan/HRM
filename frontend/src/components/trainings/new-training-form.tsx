"use client";

import { useMutation } from "@tanstack/react-query";
import { Loader2, Plus } from "lucide-react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type { PlanTrainingRequest, TrainingResponse } from "@/server/ksm/modules/trainings";

type FormValues = {
  intitule: string;
  organisme: string;
  dateDebut: string;
  dateFin: string;
  cout: string;
  nbPlaces: string;
  lieu: string;
};

export function NewTrainingForm() {
  const t = useTranslations("trainings");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();
  const today = new Date().toISOString().slice(0, 10);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: {
      intitule: "",
      organisme: "",
      dateDebut: today,
      dateFin: today,
      cout: "",
      nbPlaces: "",
      lieu: "",
    },
  });

  const start = watch("dateDebut");
  const end = watch("dateFin");
  const datesInvalid = !!start && !!end && new Date(end) < new Date(start);

  const mutation = useMutation({
    mutationFn: (v: FormValues) => {
      const body: PlanTrainingRequest = {
        intitule: v.intitule.trim(),
        organisme: v.organisme.trim() || null,
        dateDebut: v.dateDebut || null,
        dateFin: v.dateFin || null,
        cout: v.cout ? Number(v.cout) : null,
        nbPlaces: v.nbPlaces ? Number(v.nbPlaces) : null,
        lieu: v.lieu.trim() || null,
      };
      return apiFetch<TrainingResponse>("/api/hrm/trainings", { method: "POST", body });
    },
    onSuccess: (created) => {
      toast.success(t("new.success"));
      router.push(`/trainings/${created.id}`);
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
          { label: t("title"), href: "/trainings" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
        actions={
          <>
            <Link href="/trainings">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button type="submit" disabled={!isValid || datesInvalid || mutation.isPending}>
              {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
              {t("new.submit")}
            </Button>
          </>
        }
      />

      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field
              label={t("new.fields.intitule")}
              error={errors.intitule && tVal("required")}
              className="col-span-2"
            >
              <Input
                placeholder={t("new.fields.intitulePlaceholder")}
                {...register("intitule", { required: true, minLength: 3 })}
              />
            </Field>
            <Field label={t("new.fields.organisme")}>
              <Input placeholder={t("new.fields.organismePlaceholder")} {...register("organisme")} />
            </Field>
            <Field label={t("new.fields.lieu")}>
              <Input {...register("lieu")} />
            </Field>
            <Field label={t("new.fields.dateDebut")}>
              <Input type="date" {...register("dateDebut", { required: true })} />
            </Field>
            <Field
              label={t("new.fields.dateFin")}
              error={datesInvalid ? tVal("dateRange.endBeforeStart") : undefined}
            >
              <Input type="date" min={start || today} {...register("dateFin")} />
            </Field>
            <Field label={t("new.fields.cout")}>
              <Input type="number" min={0} step={1000} {...register("cout")} />
            </Field>
            <Field label={t("new.fields.nbPlaces")}>
              <Input type="number" min={1} step={1} {...register("nbPlaces")} />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
