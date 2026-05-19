"use client";

import { useTranslations } from "next-intl";
import { Construction } from "lucide-react";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";

export function ComingSoon({ titleKey, hint }: { titleKey: string; hint?: string }) {
  const tNav = useTranslations("navigation");
  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        crumbs={[{ label: tNav(titleKey) }]}
        title={tNav(titleKey)}
        subtitle="Bientôt disponible"
      />
      <Card>
        <CardContent className="flex flex-col items-center gap-3 py-12 text-center">
          <div className="grid size-12 place-items-center rounded-2xl bg-brand-50 text-brand-600">
            <Construction className="size-6" />
          </div>
          <div className="font-display text-[18px] font-semibold text-ink">
            Module à venir
          </div>
          {hint && <p className="max-w-md text-[13.5px] text-ink-3">{hint}</p>}
        </CardContent>
      </Card>
    </div>
  );
}
