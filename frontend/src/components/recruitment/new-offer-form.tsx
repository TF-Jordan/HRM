"use client";

import { useMutation } from "@tanstack/react-query";
import { Loader2, Send } from "lucide-react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Textarea } from "@/components/ui/input";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type { CreateJobOfferRequest, JobOfferResponse } from "@/server/ksm/modules/recruitment";

type FormValues = {
  poste: string;
  departement: string;
  localisation: string;
  competencesRequises: string;
  dateLimite: string;
  packageSalarial: string;
};

export function NewOfferForm() {
  const t = useTranslations("recruitment");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();
  const today = new Date().toISOString().slice(0, 10);

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: {
      poste: "",
      departement: "",
      localisation: "",
      competencesRequises: "",
      dateLimite: today,
      packageSalarial: "",
    },
  });

  const create = useMutation({
    mutationFn: (v: FormValues) => {
      const body: CreateJobOfferRequest = {
        poste: v.poste.trim(),
        departement: v.departement.trim() || null,
        localisation: v.localisation.trim() || null,
        competencesRequises: v.competencesRequises.trim() || null,
        dateLimite: v.dateLimite || null,
        packageSalarial: v.packageSalarial.trim() || null,
      };
      return apiFetch<JobOfferResponse>("/api/hrm/job-offers", { method: "POST", body });
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  async function submit(values: FormValues, alsoPublish: boolean) {
    try {
      const offer = await create.mutateAsync(values);
      if (alsoPublish) {
        try {
          await apiFetch<JobOfferResponse>(`/api/hrm/job-offers/${offer.id}/publish`, {
            method: "POST",
          });
          toast.success(t("new.publishedSuccess"));
        } catch (cause) {
          if (cause instanceof BffApiError) toast.error(cause.message);
          else toast.error(tErrors("unknown"));
        }
      } else {
        toast.success(t("new.success"));
      }
      router.push(`/recruitment/offers/${offer.id}`);
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
          { label: t("title"), href: "/recruitment" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
        actions={
          <>
            <Link href="/recruitment">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button
              type="button"
              variant="secondary"
              disabled={!isValid || create.isPending}
              onClick={handleSubmit((v) => submit(v, false))}
            >
              {create.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              {t("new.submit")}
            </Button>
            <Button
              type="button"
              disabled={!isValid || create.isPending}
              onClick={handleSubmit((v) => submit(v, true))}
            >
              <Send className="h-4 w-4" />
              {t("new.submitAndPublish")}
            </Button>
          </>
        }
      />

      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field
              label={t("new.fields.poste")}
              error={errors.poste && tVal("required")}
              className="col-span-2"
            >
              <Input
                placeholder={t("new.fields.postePlaceholder")}
                {...register("poste", { required: true, minLength: 3 })}
              />
            </Field>
            <Field label={t("new.fields.departement")}>
              <Input {...register("departement")} />
            </Field>
            <Field label={t("new.fields.localisation")}>
              <Input {...register("localisation")} />
            </Field>
            <Field label={t("new.fields.dateLimite")}>
              <Input type="date" {...register("dateLimite")} />
            </Field>
            <Field label={t("new.fields.package")}>
              <Input placeholder={t("new.fields.packagePlaceholder")} {...register("packageSalarial")} />
            </Field>
            <Field label={t("new.fields.competences")} className="col-span-2">
              <Textarea
                rows={4}
                placeholder={t("new.fields.competencesPlaceholder")}
                {...register("competencesRequises")}
              />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}

