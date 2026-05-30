"use client";

import { useMutation } from "@tanstack/react-query";
import { Loader2, Plus } from "lucide-react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Textarea } from "@/components/ui/input";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type {
  CreateSkillRequest,
  SkillResponse,
} from "@/server/ksm/modules/skills";

type FormValues = { name: string; categorie: string; description: string };

export function NewSkillForm() {
  const t = useTranslations("skills.new");
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
    defaultValues: { name: "", categorie: "", description: "" },
  });

  const mutation = useMutation({
    mutationFn: (v: FormValues) => {
      const body: CreateSkillRequest = {
        name: v.name.trim(),
        categorie: v.categorie.trim() || null,
        description: v.description.trim() || null,
      };
      return apiFetch<SkillResponse>("/api/hrm/skills", { method: "POST", body });
    },
    onSuccess: (skill) => {
      toast.success(t("success"));
      router.push(`/skills/${skill.id}`);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  return (
    <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
      <PageHeader
        ucBadge={useTranslations("skills")("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: useTranslations("skills")("title"), href: "/skills" },
          { label: t("title") },
        ]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            <Link href="/skills">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button type="submit" disabled={!isValid || mutation.isPending}>
              {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
              {t("submit")}
            </Button>
          </>
        }
      />

      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field
              label={t("fields.name")}
              error={errors.name && tVal("required")}
              className="col-span-2"
            >
              <Input
                placeholder={t("fields.namePlaceholder")}
                {...register("name", { required: true, minLength: 2 })}
              />
            </Field>
            <Field label={t("fields.categorie")}>
              <Input
                placeholder={t("fields.categoriePlaceholder")}
                {...register("categorie")}
              />
            </Field>
            <Field label={t("fields.description")} className="col-span-2">
              <Textarea rows={3} {...register("description")} />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
