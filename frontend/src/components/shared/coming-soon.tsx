"use client";

import { ArrowLeft, type LucideIcon, Sparkles } from "lucide-react";
import { useTranslations } from "next-intl";

import { Button } from "@/components/ui/button";
import { AppLink } from "@/components/ui/app-link";
import { cn } from "@/lib/utils";

/**
 * Standard placeholder shown by sidebar items whose target screen is not built yet.
 * Renders the feature's icon and name, a short generic message, and a link back to
 * the role's dashboard. Keeps navigation predictable (no 404s) while the feature
 * is in development.
 */
export function ComingSoon({
  featureKey,
  icon: Icon = Sparkles,
}: {
  /** i18n key under {@code shell.nav.<key>}, used as the feature name. */
  featureKey: string;
  icon?: LucideIcon;
}) {
  const t = useTranslations("shell");
  const tCommon = useTranslations("common.comingSoon");

  return (
    <div className="flex min-h-[60vh] items-center justify-center px-4">
      <div
        className={cn(
          "flex w-full max-w-md flex-col items-center gap-5 rounded-[20px] border border-line",
          "bg-bg-elev p-10 text-center shadow-sm-brand",
        )}
      >
        <span
          className={cn(
            "grid h-14 w-14 place-items-center rounded-2xl bg-grad-orange-soft",
            "text-white shadow-orange-brand",
          )}
        >
          <Icon className="h-7 w-7" />
        </span>

        <div className="flex flex-col gap-1.5">
          <span className="text-[11px] font-semibold uppercase tracking-[0.16em] text-orange-600">
            {tCommon("badge")}
          </span>
          <h1 className="font-display text-h1 text-ink">{t(featureKey)}</h1>
          <p className="text-[14px] leading-relaxed text-ink-3">{tCommon("body")}</p>
        </div>

        <AppLink href="/dashboard">
          <Button variant="secondary" size="default">
            <ArrowLeft className="h-4 w-4" />
            {tCommon("backToDashboard")}
          </Button>
        </AppLink>
      </div>
    </div>
  );
}
