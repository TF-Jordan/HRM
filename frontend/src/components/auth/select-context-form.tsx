"use client";

import { Building2, ChevronRight, Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { cn } from "@/lib/utils";

type Org = {
  organizationId: string;
  organizationName?: string;
  organizationCode?: string;
};

type Context = {
  contextId: string;
  tenantId: string;
  organizations: Org[];
};

export function SelectContextForm() {
  const t = useTranslations("auth.selectContext");
  const tErrors = useTranslations("errors");
  const router = useRouter();

  const [selectionToken, setSelectionToken] = React.useState<string | null>(null);
  const [contexts, setContexts] = React.useState<Context[]>([]);
  const [pickedContext, setPickedContext] = React.useState<string | null>(null);
  const [pickedOrg, setPickedOrg] = React.useState<string | null>(null);
  const [submitting, setSubmitting] = React.useState(false);

  React.useEffect(() => {
    if (typeof window === "undefined") return;
    const token = sessionStorage.getItem("hrm.selectionToken");
    const cached = sessionStorage.getItem("hrm.discoveryContexts");
    if (!token) {
      router.replace("/login");
      return;
    }
    setSelectionToken(token);
    if (cached) {
      try {
        setContexts(JSON.parse(cached) as Context[]);
      } catch {
        router.replace("/login");
      }
    }
  }, [router]);

  async function submit() {
    if (!selectionToken || !pickedContext) return;
    setSubmitting(true);
    try {
      await apiFetch("/api/auth/select-context", {
        method: "POST",
        body: { selectionToken, contextId: pickedContext, organizationId: pickedOrg },
      });
      sessionStorage.removeItem("hrm.selectionToken");
      sessionStorage.removeItem("hrm.discoveryContexts");
      router.push("/dashboard");
    } catch (cause) {
      if (cause instanceof BffApiError) {
        toast.error(cause.message);
      } else {
        toast.error(tErrors("unknown"));
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Card>
      <CardContent padding="lg">
        <div className="mb-6 text-center">
          <h1 className="font-display text-[24px] font-bold tracking-tight text-ink">
            {t("title")}
          </h1>
          <p className="mt-1.5 text-[13.5px] text-ink-3">{t("subtitle")}</p>
        </div>
        <div className="flex flex-col gap-2">
          {contexts.flatMap((ctx) =>
            ctx.organizations.length > 0
              ? ctx.organizations.map((org) => {
                  const active = pickedContext === ctx.contextId && pickedOrg === org.organizationId;
                  return (
                    <button
                      key={`${ctx.contextId}:${org.organizationId}`}
                      type="button"
                      onClick={() => {
                        setPickedContext(ctx.contextId);
                        setPickedOrg(org.organizationId);
                      }}
                      className={cn(
                        "flex items-center gap-3 rounded-[14px] border bg-white p-3.5 text-left transition-all duration-150",
                        active
                          ? "border-orange-400 shadow-orange-brand"
                          : "border-line hover:border-line-strong",
                      )}
                    >
                      <span className="grid h-10 w-10 place-items-center rounded-xl bg-orange-50 text-orange-600">
                        <Building2 className="h-5 w-5" />
                      </span>
                      <span className="flex-1">
                        <span className="block text-[14px] font-semibold text-ink">
                          {org.organizationName ?? org.organizationCode ?? org.organizationId}
                        </span>
                        <span className="block font-mono-tabular text-[11px] text-ink-4">
                          tenant · {ctx.tenantId.slice(0, 8)}
                        </span>
                      </span>
                      <ChevronRight className="h-4 w-4 text-ink-3" />
                    </button>
                  );
                })
              : [
                  <button
                    key={ctx.contextId}
                    type="button"
                    onClick={() => {
                      setPickedContext(ctx.contextId);
                      setPickedOrg(null);
                    }}
                    className={cn(
                      "flex items-center gap-3 rounded-[14px] border bg-white p-3.5 text-left",
                      pickedContext === ctx.contextId
                        ? "border-orange-400 shadow-orange-brand"
                        : "border-line",
                    )}
                  >
                    <span className="text-[14px] font-semibold text-ink">
                      Tenant {ctx.tenantId.slice(0, 8)}…
                    </span>
                  </button>,
                ],
          )}
        </div>
        <Button
          type="button"
          onClick={submit}
          disabled={!pickedContext || submitting}
          className="mt-6 w-full"
        >
          {submitting ? <Loader2 className="h-4 w-4 animate-spin" /> : t("submit")}
        </Button>
      </CardContent>
    </Card>
  );
}
