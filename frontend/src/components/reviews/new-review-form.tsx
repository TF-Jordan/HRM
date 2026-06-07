"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { Loader2, Plus } from "lucide-react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { ReviewResponse } from "@/server/ksm/modules/reviews";

type FormValues = {
  employeeId: string;
  evaluator: string;
  periode: string;
};

function defaultPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-Q${Math.floor(now.getMonth() / 3) + 1}`;
}

export function NewReviewForm() {
  const t = useTranslations("reviews");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: { employeeId: "", evaluator: "", periode: defaultPeriode() },
  });

  const mutation = useMutation({
    mutationFn: (v: FormValues) =>
      apiFetch<ReviewResponse>("/api/hrm/reviews", {
        method: "POST",
        body: {
          employeeId: v.employeeId,
          evaluateurDisplayName: v.evaluator.trim() || null,
          periode: v.periode.trim(),
        },
      }),
    onSuccess: (created) => {
      toast.success(t("new.success"));
      router.push(`/reviews/${created.id}`);
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
          { label: t("title"), href: "/reviews" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
        actions={
          <>
            <Link href="/reviews">
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
            <Field
              label={t("new.fields.employee")}
              error={errors.employeeId && tVal("required")}
              className="col-span-2"
            >
              <select
                {...register("employeeId", { required: true })}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                <option value="">—</option>
                {(employeesQuery.data ?? []).map((e) => (
                  <option key={e.id} value={e.id}>
                    {e.matricule} · {e.actorDisplayName ?? e.actorId.slice(0, 8)}
                  </option>
                ))}
              </select>
            </Field>
            <Field label={t("new.fields.evaluator")}>
              <Input placeholder={t("new.fields.evaluatorPlaceholder")} {...register("evaluator")} />
            </Field>
            <Field label={t("new.fields.periode")} error={errors.periode && tVal("required")}>
              <Input
                placeholder={t("new.fields.periodePlaceholder")}
                {...register("periode", { required: true, minLength: 3 })}
              />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
