"use client";

import { useTranslations } from "next-intl";
import { useLeaveBalances } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export function LeavesTab({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tStatuses = useTranslations("statuses");
  const fmt = useFormat();
  const annee = new Date().getUTCFullYear();
  const { data, isLoading } = useLeaveBalances(employeeId, annee);

  if (isLoading) return <Skeleton className="h-32 w-full" />;

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardContent className="py-8 text-center text-sm text-ink-3">
          {t("leaves.empty")}
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
      {data.map((b) => (
        <Card key={b.id}>
          <CardContent>
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className="text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {tStatuses(`leaveType.${b.type}` as never)}
                </div>
                <div className="mt-1 font-display text-xl font-extrabold text-ink tabular">
                  {fmt.number(Number(b.soldeRestant))}
                  <span className="ml-1 text-sm font-medium text-ink-3">/ {fmt.number(Number(b.acquis))}</span>
                </div>
                <div className="mt-0.5 text-[12px] text-ink-3">
                  {t("leaves.pris")}: {fmt.number(Number(b.pris))} • {b.annee}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
