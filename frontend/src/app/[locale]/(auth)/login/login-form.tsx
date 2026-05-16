"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { useRouter } from "@/i18n/navigation";

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
});

type FormValues = z.infer<typeof schema>;

export function LoginForm() {
  const t = useTranslations("auth.login");
  const tValidation = useTranslations("validation");
  const tErrors = useTranslations("errors");
  const router = useRouter();
  const [serverError, setServerError] = React.useState<string | null>(null);
  const [submitting, setSubmitting] = React.useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: "", password: "" },
  });

  const onSubmit = async (values: FormValues) => {
    setServerError(null);
    setSubmitting(true);
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(values),
        credentials: "include",
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        setServerError(body?.message ?? t("errorGeneric"));
        return;
      }
      const json = await res.json();
      const redirectTo = (json?.redirectTo as string | undefined) ?? "/dashboard";
      router.push(redirectTo as never);
    } catch {
      setServerError(tErrors("network"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="w-full max-w-md">
      <div className="mb-8 flex items-center gap-3">
        <div className="grid size-12 place-items-center rounded-2xl bg-grad-orange font-display text-2xl font-extrabold text-white shadow-brand">
          H
        </div>
        <div className="flex flex-col leading-tight">
          <span className="font-display text-lg font-bold tracking-tight text-ink">HR Core</span>
          <span className="text-[11px] font-semibold uppercase tracking-widest text-ink-3">
            KSM platform
          </span>
        </div>
      </div>

      <Card>
        <CardContent className="space-y-6">
          <div>
            <h1 className="font-display text-[28px] font-bold tracking-tight text-ink">
              {t("title")}
            </h1>
            <p className="mt-1.5 text-[14px] text-ink-3">{t("subtitle")}</p>
          </div>

          {serverError && (
            <div
              role="alert"
              className="rounded-xl border border-status-red-500/30 bg-status-red-50 px-4 py-3 text-sm text-status-red-600"
            >
              <div className="font-semibold">{t("errorTitle")}</div>
              <div className="mt-0.5 text-[13px]">{serverError}</div>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
            <div className="space-y-2">
              <Label htmlFor="email">{t("emailLabel")}</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                placeholder={t("emailPlaceholder")}
                aria-invalid={!!errors.email}
                {...register("email")}
              />
              {errors.email && (
                <p className="text-[12px] text-status-red-600">
                  {tValidation(errors.email.message === "Invalid email" ? "email" : "required")}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">{t("passwordLabel")}</Label>
                <a
                  href="forgot-password"
                  className="text-[12px] font-medium text-brand-600 hover:text-brand-700 hover:underline"
                >
                  {t("forgotPassword")}
                </a>
              </div>
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                placeholder={t("passwordPlaceholder")}
                aria-invalid={!!errors.password}
                {...register("password")}
              />
              {errors.password && (
                <p className="text-[12px] text-status-red-600">
                  {tValidation("minLength", { min: 6 })}
                </p>
              )}
            </div>

            <Button type="submit" size="lg" disabled={submitting} className="w-full">
              {submitting && <Loader2 className="size-4 animate-spin" />}
              {submitting ? t("submitting") : t("submit")}
            </Button>
          </form>

          <div className="border-t border-line-soft pt-5 text-center text-[12px] text-ink-3">
            {t("noAccount")}{" "}
            <span className="font-medium text-ink-2">{t("contactAdmin")}</span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
