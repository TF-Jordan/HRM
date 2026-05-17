"use client";

import { useTranslations } from "next-intl";
import { usePayslipLines } from "@/hooks/modules/usePayroll";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export function PayslipClient({ entryId }: { entryId: string }) {
  const t = useTranslations("accounting.payslip");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const lines = usePayslipLines(entryId);

  if (lines.isLoading) return <Skeleton className="h-40 w-full" />;

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-08"
        crumbs={[{ label: tNav("items.payroll"), href: "/payroll" }, { label: t("title") }]}
        title={t("title")}
      />

      <Card>
        <CardContent className="p-0">
          <table className="w-full border-separate border-spacing-0">
            <thead>
              <tr>
                {[t("table.libelle"), "Type", t("table.base"), t("table.taux"), t("table.montant")].map((h, i) => (
                  <th
                    key={i}
                    className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {(lines.data ?? [])
                .sort((a, b) => a.ordreAffichage - b.ordreAffichage)
                .map((l) => (
                  <tr key={l.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">{l.libelle}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[12px] text-ink-3">
                      {t(`type.${l.type}` as never)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">
                      {l.base != null ? fmt.money(Number(l.base)) : "—"}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">
                      {l.taux != null ? `${Number(l.taux)}%` : "—"}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-semibold text-ink tabular text-right">
                      {fmt.money(Number(l.montant))}
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </CardContent>
      </Card>
    </div>
  );
}
