"use client";

import { Check, Globe } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { usePathname, useRouter } from "@/i18n/navigation";
import { routing } from "@/i18n/routing";
import { cn } from "@/lib/utils";

export function LocaleSwitcher() {
  const t = useTranslations("common.language");
  const locale = useLocale();
  const router = useRouter();
  const pathname = usePathname();
  const [open, setOpen] = React.useState(false);
  const ref = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    function handler(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const labels = {
    fr: t("french"),
    en: t("english"),
  } as const;

  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-label={t("switchTo")}
        className="grid h-[38px] w-[38px] place-items-center rounded-[11px] border border-line bg-white text-ink-2 shadow-xs-brand transition-all duration-200 hover:-translate-y-px hover:border-line-strong hover:text-ink hover:shadow-sm-brand"
      >
        <Globe className="h-[18px] w-[18px]" />
        <span className="sr-only">{labels[locale as keyof typeof labels]}</span>
      </button>
      {open && (
        <div className="absolute right-0 top-[calc(100%+8px)] z-50 min-w-[160px] overflow-hidden rounded-[14px] border border-line bg-white shadow-lg-brand">
          {routing.locales.map((l) => (
            <button
              key={l}
              type="button"
              onClick={() => {
                setOpen(false);
                router.replace(pathname, { locale: l });
              }}
              className={cn(
                "flex w-full items-center justify-between px-3.5 py-2.5 text-left text-[13px] font-medium text-ink-2 transition-colors hover:bg-orange-50 hover:text-orange-700",
                l === locale && "bg-orange-50/50 text-orange-700",
              )}
            >
              {labels[l]}
              {l === locale && <Check className="h-4 w-4" />}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
