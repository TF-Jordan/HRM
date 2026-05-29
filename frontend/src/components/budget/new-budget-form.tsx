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
import type { TrainingBudgetResponse } from "@/server/ksm/modules/training-budgets";

type FormValues = {
  annee: string;
  agencyId: string;
  montantAlloue: string;
};

export function NewBudgetForm() {
  const t = useTranslations("budget");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();
  const currentYear = new Date().getFullYear();

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: { annee: String(currentYear), agencyId: "", montantAlloue: "" },
  });

  const mutation = useMutation({
    mutationFn: (v: FormValues) =>
      apiFetch<TrainingBudgetResponse>("/api/hrm/training-budgets", {
        method: "POST",
        body: {
          annee: Number(v.annee),
          agencyId: v.agencyId.trim() || null,
          montantAlloue: Number(v.montantAlloue),
        },
      }),
    onSuccess: () => {
      toast.success(t("new.success"));
      router.push("/training-budgets");
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
          { label: t("title"), href: "/training-budgets" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
        actions={
          <>
            <Link href="/training-budgets">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button type="submit" disabled={!isValid || mutation.isPending}>
              {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
              {t("new.submit")}
            </Button>
          </>
        }
      />

      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field label={t("new.fields.annee")} error={errors.annee && tVal("required")}>
              <Input type="number" min={2000} step={1} {...register("annee", { required: true })} />
            </Field>
            <Field label={t("new.fields.agencyId")}>
              <Input {...register("agencyId")} />
            </Field>
            <Field
              label={t("new.fields.montantAlloue")}
              error={errors.montantAlloue && tVal("required")}
              className="col-span-2"
            >
              <Input
                type="number"
                min={0}
                step={10000}
                {...register("montantAlloue", { required: true, min: 1 })}
              />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
