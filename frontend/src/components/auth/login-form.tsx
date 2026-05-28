"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { loginSchema, type LoginFormValues } from "@/lib/validation/auth";

type LoginResult = {
  step: "select_context" | "change_password" | "authenticated";
  user?: { fullName: string };
  selectionToken?: string;
  contexts?: Array<{
    contextId: string;
    tenantId: string;
    organizations: Array<{ organizationId: string; organizationName?: string; organizationCode?: string }>;
  }>;
};

export function LoginForm({ reason }: { reason?: string }) {
  const t = useTranslations("auth.login");
  const tErrors = useTranslations("errors");
  const router = useRouter();
  const [showPassword, setShowPassword] = React.useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
    mode: "onTouched",
  });

  React.useEffect(() => {
    if (reason === "expired") {
      toast.warning(tErrors("unauthorized"));
    }
  }, [reason, tErrors]);

  async function onSubmit(values: LoginFormValues) {
    try {
      const result = await apiFetch<LoginResult>("/api/auth/login", {
        method: "POST",
        body: values,
      });

      if (result.step === "select_context") {
        if (typeof window !== "undefined") {
          if (result.selectionToken) {
            sessionStorage.setItem("hrm.selectionToken", result.selectionToken);
          }
          if (result.contexts) {
            sessionStorage.setItem("hrm.discoveryContexts", JSON.stringify(result.contexts));
          }
        }
        router.push("/select-context");
        return;
      }
      if (result.step === "change_password") {
        router.push("/change-password");
        return;
      }
      router.push("/dashboard");
    } catch (cause) {
      if (cause instanceof BffApiError) {
        if (cause.status === 401 || cause.status === 403) {
          setError("password", { message: tErrors("unauthorized") });
          toast.error(tErrors("unauthorized"));
        } else {
          toast.error(cause.message);
        }
      } else {
        toast.error(tErrors("unknown"));
      }
    }
  }

  return (
    <Card className="overflow-hidden">
      <CardContent padding="lg">
        <div className="mb-6 text-center">
          <h1 className="font-display text-[28px] font-bold tracking-tight text-ink">
            {t("title")}
          </h1>
          <p className="mt-1.5 text-[14px] text-ink-3">{t("subtitle")}</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
          <Field
            label={t("email")}
            error={errors.email && (errors.email.message ?? "")}
          >
            <Input
              type="email"
              autoComplete="email"
              placeholder="vous@example.com"
              {...register("email")}
            />
          </Field>

          <Field
            label={t("password")}
            error={errors.password && (errors.password.message ?? "")}
          >
            <div className="relative">
              <Input
                type={showPassword ? "text" : "password"}
                autoComplete="current-password"
                className="pr-11"
                {...register("password")}
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-3 hover:text-ink-2"
              >
                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
          </Field>

          <Button type="submit" disabled={isSubmitting} className="mt-2 w-full">
            {isSubmitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("loading")}
              </>
            ) : (
              t("submit")
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
