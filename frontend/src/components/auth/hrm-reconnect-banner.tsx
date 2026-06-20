"use client";

import { RefreshCw } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { Button } from "@/components/ui/button";
import { useRouter } from "@/i18n/navigation";
import { apiFetch } from "@/lib/api-client";

/**
 * Non-blocking banner shown when the first-access HRM bootstrap just self-assigned
 * the SUPER_ADMIN role to the OWNER. The freshly granted HRM permissions only apply
 * after a new login (KSM resolves permissions at token issuance), so we invite —
 * but do not force — the user to reconnect. They can keep working meanwhile: their
 * tenant:admin already lets them create the other accounts.
 */
export function HrmReconnectBanner() {
  const t = useTranslations("auth.reconnect");
  const { session, setSession } = useSession();
  const router = useRouter();
  const [reconnecting, setReconnecting] = React.useState(false);

  if (!session?.hrmNeedsReconnect) return null;

  async function reconnect() {
    setReconnecting(true);
    try {
      await apiFetch("/api/auth/logout", { method: "POST" });
    } catch {
      // ignore — we still clear local state and redirect to login
    }
    setSession(null);
    router.replace("/login");
  }

  return (
    <div className="border-b border-warning-500/30 bg-warning-50">
      <div className="mx-auto flex w-full max-w-[1440px] items-center gap-3 px-9 py-2.5">
        <RefreshCw className="h-4 w-4 shrink-0 text-warning-600" />
        <p className="flex-1 text-[13px] text-ink-2">
          <span className="font-semibold text-ink">{t("title")}</span> {t("body")}
        </p>
        <Button type="button" size="sm" variant="secondary" onClick={reconnect} disabled={reconnecting}>
          {reconnecting ? t("reconnecting") : t("action")}
        </Button>
      </div>
    </div>
  );
}
