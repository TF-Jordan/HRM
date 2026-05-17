"use client";

import { useTranslations } from "next-intl";
import { UserX } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

export function NotEmployeeCard() {
  const t = useTranslations("selfService.notEmployee");
  return (
    <Card>
      <CardContent className="flex flex-col items-center gap-3 py-12 text-center">
        <div className="grid size-12 place-items-center rounded-2xl bg-cream-soft text-ink-3">
          <UserX className="size-6" />
        </div>
        <div className="font-display text-lg font-bold text-ink">{t("title")}</div>
        <div className="max-w-md text-sm text-ink-3">{t("subtitle")}</div>
      </CardContent>
    </Card>
  );
}
