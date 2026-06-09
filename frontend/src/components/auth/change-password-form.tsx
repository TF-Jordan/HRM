"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { changePasswordSchema, type ChangePasswordFormValues } from "@/lib/validation/auth";

export function ChangePasswordForm({
  forced,
  embedded = false,
  onSuccess,
}: {
  forced: boolean;
  embedded?: boolean;
  onSuccess?: () => void | Promise<void>;
}) {
  const t = useTranslations("auth.changePassword");
  const tValidation = useTranslations("validation");
  const tErrors = useTranslations("errors");
  const router = useRouter();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
    mode: "onTouched",
  });

  function lookup(key?: string): string | undefined {
    if (!key) return undefined;
    // Try the validation namespace (keys like "validation.password.minLength")
    const parts = key.startsWith("validation.") ? key.slice("validation.".length) : key;
    try {
      return tValidation(parts);
    } catch {
      return key;
    }
  }

  async function onSubmit(values: ChangePasswordFormValues) {
    try {
      await apiFetch("/api/auth/change-password", {
        method: "POST",
        body: {
          currentPassword: values.currentPassword,
          newPassword: values.newPassword,
        },
      });
      toast.success(t("title"));
      if (onSuccess) {
        await onSuccess();
      } else {
        router.push("/dashboard");
      }
    } catch (cause) {
      if (cause instanceof BffApiError) {
        toast.error(cause.message);
      } else {
        toast.error(tErrors("unknown"));
      }
    }
  }

  const form = (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      <Field label={t("current")} error={lookup(errors.currentPassword?.message)}>
        <Input type="password" autoComplete="current-password" {...register("currentPassword")} />
      </Field>
      <Field
        label={t("new")}
        hint={t("constraints")}
        error={lookup(errors.newPassword?.message)}
      >
        <Input type="password" autoComplete="new-password" {...register("newPassword")} />
      </Field>
      <Field label={t("confirm")} error={lookup(errors.confirmPassword?.message)}>
        <Input type="password" autoComplete="new-password" {...register("confirmPassword")} />
      </Field>

      <Button type="submit" disabled={isSubmitting} className={embedded ? "mt-2 w-full" : "mt-2 w-full"}>
        {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : t("submit")}
      </Button>
    </form>
  );

  if (embedded) {
    return form;
  }

  return (
    <Card>
      <CardContent padding="lg">
        <div className="mb-6 text-center">
          <h1 className="font-display text-[24px] font-bold tracking-tight text-ink">
            {t("title")}
          </h1>
          <p className="mt-1.5 text-[13.5px] text-ink-3">
            {forced ? t("subtitle") : t("title")}
          </p>
        </div>
        {form}
      </CardContent>
    </Card>
  );
}
