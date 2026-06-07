"use client";

import { useMutation } from "@tanstack/react-query";
import { AlertTriangle, Banknote, CalendarRange, GraduationCap, Loader2, Plus } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { TrainingPreviewCard } from "@/components/trainings/training-preview-card";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
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
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const today = new Date().toISOString().slice(0, 10);

  const {
    register,
    handleSubmit,
    control,
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

  const values = useWatch({ control }) as Partial<FormValues>;
  const start = values.dateDebut ?? "";
  const end = values.dateFin ?? "";
  // Chronology guard: the end date can never precede the start date.
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

  const coutNum = values.cout ? Number(values.cout) : null;
  const placesNum = values.nbPlaces ? Number(values.nbPlaces) : null;

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
              {mutation.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Plus className="h-4 w-4" />
              )}
              {t("new.submit")}
            </Button>
          </>
        }
      />

      <div className="grid gap-5 lg:grid-cols-[1fr_320px]">
        <div className="flex flex-col gap-5">
          <Card>
            <CardContent padding="lg">
              <SectionTitle icon={GraduationCap} label={t("new.sections.about")} />
              <div className="mt-4 grid grid-cols-2 gap-4">
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
                  <Input
                    placeholder={t("new.fields.organismePlaceholder")}
                    {...register("organisme")}
                  />
                </Field>
                <Field label={t("new.fields.lieu")}>
                  <Input placeholder={t("new.fields.lieuPlaceholder")} {...register("lieu")} />
                </Field>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent padding="lg">
              <SectionTitle icon={CalendarRange} label={t("new.sections.schedule")} />
              <div className="mt-4 grid grid-cols-2 gap-4">
                <Field label={t("new.fields.dateDebut")} error={errors.dateDebut && tVal("required")}>
                  <Input type="date" {...register("dateDebut", { required: true })} />
                </Field>
                <Field
                  label={t("new.fields.dateFin")}
                  error={datesInvalid ? tVal("dateRange.endBeforeStart") : undefined}
                >
                  <Input
                    type="date"
                    min={start || today}
                    className={datesInvalid ? "border-danger-400 focus:border-danger-500" : undefined}
                    {...register("dateFin", {
                      required: true,
                      validate: (v) =>
                        !start || !v || new Date(v) >= new Date(start)
                          ? true
                          : tVal("dateRange.endBeforeStart"),
                    })}
                  />
                </Field>
              </div>
              {datesInvalid && (
                <div className="mt-3 flex items-center gap-2 rounded-[10px] border border-danger-200 bg-danger-50 px-3 py-2 text-[12px] font-medium text-danger-600">
                  <AlertTriangle className="h-3.5 w-3.5 shrink-0" />
                  {tVal("dateRange.endBeforeStart")}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardContent padding="lg">
              <SectionTitle icon={Banknote} label={t("new.sections.logistics")} />
              <div className="mt-4 grid grid-cols-2 gap-4">
                <Field label={t("new.fields.cout")}>
                  <Input type="number" min={0} step={1000} {...register("cout")} />
                </Field>
                <Field label={t("new.fields.nbPlaces")}>
                  <Input type="number" min={1} step={1} {...register("nbPlaces")} />
                </Field>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Live preview — identical to the catalog card */}
        <aside className="lg:sticky lg:top-2 lg:self-start">
          <TrainingPreviewCard
            headerLabel={t("new.preview.title")}
            title={values.intitule?.trim() || t("new.preview.untitled")}
            organisme={values.organisme}
            lieu={values.lieu}
            dateDebut={start || null}
            dateFin={datesInvalid ? null : end || null}
            cout={coutNum}
            nbPlaces={placesNum}
            locale={locale}
          />
        </aside>
      </div>
    </form>
  );
}

function SectionTitle({
  icon: Icon,
  label,
}: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
}) {
  return (
    <h2 className="flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
      <Icon className="h-4 w-4 text-orange-500" />
      {label}
    </h2>
  );
}
