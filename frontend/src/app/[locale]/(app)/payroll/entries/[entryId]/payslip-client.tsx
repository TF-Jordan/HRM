"use client";

import { useTranslations } from "next-intl";
import { Printer } from "lucide-react";
import { usePayslipLines } from "@/hooks/modules/usePayroll";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import type { PayslipLine } from "@/lib/types/hrm/payroll";

const EARNING_TYPES = ["EARNING"];
const DEDUCTION_TYPES = ["DEDUCTION", "TAX"];

export function PayslipClient({ entryId }: { entryId: string }) {
  const t = useTranslations("accounting.payslip");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const lines = usePayslipLines(entryId);

  if (lines.isLoading) return <Skeleton className="h-80 w-full rounded-[20px]" />;

  const all = [...(lines.data ?? [])].sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const earnings = all.filter((l) => EARNING_TYPES.includes(l.type));
  const deductions = all.filter((l) => DEDUCTION_TYPES.includes(l.type));
  const brut = earnings.reduce((s, l) => s + Number(l.montant), 0);
  const totalRet = deductions.reduce((s, l) => s + Number(l.montant), 0);
  const net = brut - totalRet;

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        crumbs={[{ label: tNav("items.payroll"), href: "/payroll" }, { label: t("title") }]}
        title={t("title")}
        actions={
          <Button variant="secondary" onClick={() => window.print()}>
            <Printer className="size-4" />
            {t("print")}
          </Button>
        }
      />

      <Card className="overflow-hidden p-0">
        <table className="w-full border-collapse text-[13px]">
          <thead>
            <tr className="border-b border-line-soft bg-cream-soft/40 text-[10.5px] uppercase tracking-[0.1em] text-ink-4">
              <th className="px-4 py-2.5 text-left font-semibold">{t("table.libelle")}</th>
              <th className="px-3 py-2.5 text-right font-semibold">{t("table.base")}</th>
              <th className="px-3 py-2.5 text-right font-semibold">{t("table.taux")}</th>
              <th className="px-3 py-2.5 text-right font-semibold">{t("gain")}</th>
              <th className="px-3 py-2.5 text-right font-semibold">{t("retenue")}</th>
            </tr>
          </thead>
          <tbody>
            <SectionRow label={t("remuneration")} />
            {earnings.map((l) => (
              <LineRow key={l.id} l={l} fmt={fmt} side="gain" />
            ))}
            <SubtotalRow label={t("brutImposable")} value={fmt.money(brut)} side="gain" />

            <SectionRow label={t("retenuesSalariales")} />
            {deductions.map((l) => (
              <LineRow key={l.id} l={l} fmt={fmt} side="retenue" />
            ))}
            <SubtotalRow label={t("totalRetenues")} value={fmt.money(totalRet)} side="retenue" />
          </tbody>
        </table>

        <div className="flex items-center justify-between bg-grad-dark px-5 py-4 text-white">
          <span className="text-[11px] font-semibold uppercase tracking-[0.14em] text-white/60">
            {t("netAPayer")}
          </span>
          <span className="font-display text-[26px] font-extrabold tabular">{fmt.money(net)}</span>
        </div>
      </Card>
    </div>
  );
}

function SectionRow({ label }: { label: string }) {
  return (
    <tr className="bg-brand-50/50">
      <td colSpan={5} className="px-4 py-2 text-[11px] font-bold uppercase tracking-[0.1em] text-brand-700">
        {label}
      </td>
    </tr>
  );
}

function LineRow({
  l,
  fmt,
  side,
}: {
  l: PayslipLine;
  fmt: ReturnType<typeof useFormat>;
  side: "gain" | "retenue";
}) {
  return (
    <tr className="border-b border-line-soft/60">
      <td className="px-4 py-2 text-ink">{l.libelle}</td>
      <td className="px-3 py-2 text-right tabular text-ink-3">{l.base != null ? fmt.money(Number(l.base)) : "—"}</td>
      <td className="px-3 py-2 text-right tabular text-ink-3">{l.taux != null ? `${Number(l.taux)}%` : "—"}</td>
      <td className="px-3 py-2 text-right tabular text-ink">{side === "gain" ? fmt.money(Number(l.montant)) : ""}</td>
      <td className="px-3 py-2 text-right tabular text-ink">{side === "retenue" ? fmt.money(Number(l.montant)) : ""}</td>
    </tr>
  );
}

function SubtotalRow({ label, value, side }: { label: string; value: string; side: "gain" | "retenue" }) {
  return (
    <tr className="border-b border-line bg-cream-soft/30 font-semibold">
      <td className="px-4 py-2 text-ink">{label}</td>
      <td />
      <td />
      <td className="px-3 py-2 text-right tabular text-ink">{side === "gain" ? value : ""}</td>
      <td className="px-3 py-2 text-right tabular text-ink">{side === "retenue" ? value : ""}</td>
    </tr>
  );
}
