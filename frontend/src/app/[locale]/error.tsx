"use client";

import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";

export default function LocaleError({ reset }: { reset: () => void }) {
  const t = useTranslations("errors");
  return (
    <div className="flex min-h-screen items-center justify-center bg-grad-ambient px-6">
      <div className="max-w-md text-center">
        <h1 className="font-display text-3xl font-bold text-ink">{t("page.title")}</h1>
        <p className="mt-3 text-[14px] text-ink-3">{t("page.subtitle")}</p>
        <div className="mt-6 flex justify-center gap-2">
          <Button onClick={reset}>{t("page.backHome")}</Button>
        </div>
      </div>
    </div>
  );
}
