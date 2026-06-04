"use client";

import { useMutation } from "@tanstack/react-query";
import { Loader2, Play } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type { PayrollRunResponse, RunPayrollRequest } from "@/server/ksm/modules/payroll";

type FormValues = { periode: string; agencyId: string };

function defaultPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

export function NewPayrollForm() {
  const t = useTranslations("payroll");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const router = useRouter();

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: { periode: defaultPeriode(), agencyId: "" },
  });

  const mutation = useMutation({
    mutationFn: (v: FormValues) => {
      const body: RunPayrollRequest = {
        period: v.periode.trim(),
        agencyId: v.agencyId.trim() || null,
      };
      return apiFetch<PayrollRunResponse>("/api/hrm/payroll", {
        method: "POST",
        body,
      });
    },
    onSuccess: (run) => {
      toast.success(t("form.successTitle"));
      router.push(`/payroll/${run.id}`);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  return (
    <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
      <PageHeader
        ucBadge={t("uc")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/payroll" },
          { label: t("form.title") },
        ]}
        title={t("form.title")}
        subtitle={t("form.subtitle")}
        actions={
          <>
            <Link href="/payroll">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button type="submit" disabled={!isValid || mutation.isPending}>
              {mutation.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Play className="h-4 w-4" />
              )}
              {mutation.isPending ? t("actions.calculating") : t("form.submit")}
            </Button>
          </>
        }
      />

      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
            <Field
              label={t("form.periode")}
              hint={t("form.periodeHint")}
              error={errors.periode?.message}
            >
              <Input
                {...register("periode", {
                  required: true,
                  pattern: { value: /^\d{4}-\d{2}$/, message: t("form.periodeHint") },
                })}
                placeholder="2026-06"
              />
            </Field>
            <Field label={t("form.agency")} hint={t("form.agencyHint")}>
              <Input {...register("agencyId")} placeholder="UUID (optionnel)" />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
