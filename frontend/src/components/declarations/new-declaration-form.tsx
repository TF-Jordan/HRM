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
import {
  DECLARATION_FORMATS,
  DECLARATION_TYPES,
} from "@/lib/declaration-status";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type {
  CreateSocialDeclarationRequest,
  DeclarationFormat,
  DeclarationType,
  SocialDeclarationResponse,
} from "@/server/ksm/modules/declarations";

type FormValues = {
  type: DeclarationType;
  periode: string;
  format: DeclarationFormat;
};

function defaultPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

export function NewDeclarationForm() {
  const t = useTranslations("declarations");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: { type: "CNPS", periode: defaultPeriode(), format: "PDF" },
  });

  const mutation = useMutation({
    mutationFn: (v: FormValues) => {
      const body: CreateSocialDeclarationRequest = {
        type: v.type,
        periode: v.periode.trim(),
        format: v.format,
      };
      return apiFetch<SocialDeclarationResponse>("/api/hrm/declarations", {
        method: "POST",
        body,
      });
    },
    onSuccess: (decl) => {
      toast.success(t("new.success"));
      router.push(`/declarations/${decl.id}`);
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
          { label: t("title"), href: "/declarations" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
        actions={
          <>
            <Link href="/declarations">
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
            <Field label={t("new.fields.type")}>
              <select
                {...register("type", { required: true })}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                {DECLARATION_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {t(`type.${type}`)}
                  </option>
                ))}
              </select>
            </Field>
            <Field label={t("new.fields.format")}>
              <select
                {...register("format", { required: true })}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                {DECLARATION_FORMATS.map((fmt) => (
                  <option key={fmt} value={fmt}>
                    {t(`format.${fmt}`)}
                  </option>
                ))}
              </select>
            </Field>
            <Field
              label={t("new.fields.periode")}
              error={errors.periode && tVal("required")}
              className="col-span-2"
            >
              <Input
                placeholder={t("new.fields.periodePlaceholder")}
                {...register("periode", { required: true, minLength: 4 })}
              />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
