"use client";

import { useTranslations } from "next-intl";
import { Download } from "lucide-react";
import { useMyPayslips } from "@/hooks/modules/usePayroll";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Link } from "@/i18n/navigation";

export function MyPayslipsClient() {
  const t = useTranslations("selfService.payroll");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const payslips = useMyPayslips();

  const rows = payslips.data ?? [];
  const lastNet = rows[0]?.net ?? 0;
  const ytdNet = rows.reduce((s, r) => s + Number(r.net), 0);

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader crumbs={[{ label: tNav("items.payslips") }]} title={t("title")} subtitle={t("subtitle")} />

      {payslips.isLoading ? (
        <Skeleton className="h-40 w-full rounded-[20px]" />
      ) : rows.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <StatCard tone="orange" label={t("table.net")} value={fmt.moneyShort(lastNet)} footer={rows[0]?.periode ?? ""} />
            <StatCard tone="blue" label="Cumul net" value={fmt.moneyShort(ytdNet)} footer={`${rows.length} bulletins`} />
            <StatCard tone="green" label={t("table.brut")} value={fmt.moneyShort(rows[0]?.brut ?? 0)} footer={rows[0]?.periode ?? ""} />
          </div>

          <Card className="overflow-hidden p-0">
            <table className="w-full border-collapse text-[13px]">
              <thead>
                <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                  <th className="px-4 py-2.5 text-left font-semibold">{t("table.periode")}</th>
                  <th className="px-3 py-2.5 text-right font-semibold">{t("table.brut")}</th>
                  <th className="px-3 py-2.5 text-right font-semibold">{t("table.net")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">Statut</th>
                  <th className="w-10 px-3 py-2.5" />
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.entryId} className="border-b border-line-soft/70 last:border-0 hover:bg-brand-50/40">
                    <td className="px-4 py-2.5 font-semibold text-ink">
                      <Link href={`/payroll/entries/${r.entryId}` as never} className="hover:text-brand-700">
                        {r.periode}
                      </Link>
                    </td>
                    <td className="px-3 py-2.5 text-right tabular text-ink-2">{fmt.money(r.brut)}</td>
                    <td className="px-3 py-2.5 text-right font-semibold tabular text-ink">{fmt.money(r.net)}</td>
                    <td className="px-3 py-2.5">
                      <StatusBadge kind="payrollRun" status={r.status} />
                    </td>
                    <td className="px-3 py-2.5 text-right">
                      <Link
                        href={`/payroll/entries/${r.entryId}` as never}
                        className="grid size-7 place-items-center rounded-lg text-ink-4 hover:bg-cream-soft hover:text-brand-600"
                        aria-label={t("table.net")}
                      >
                        <Download className="size-4" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>
        </>
      )}
    </div>
  );
}
