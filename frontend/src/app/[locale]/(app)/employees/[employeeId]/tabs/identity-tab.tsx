"use client";

import { useTranslations } from "next-intl";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useFormat } from "@/hooks/useFormat";
import type { Employee } from "@/lib/types/hrm/employee";

export function IdentityTab({ employee }: { employee: Employee }) {
  const t = useTranslations("employees");
  const fmt = useFormat();

  const fields: Array<{ label: string; value: React.ReactNode }> = [
    { label: t("detail.fields.matricule"), value: <span className="font-mono">{employee.matricule}</span> },
    { label: t("detail.fields.actor"), value: employee.actorDisplayName },
    { label: t("detail.fields.hireDate"), value: fmt.date(employee.dateEmbauche) },
    { label: t("detail.fields.department"), value: employee.departmentCode ?? "—" },
    { label: t("detail.fields.category"), value: employee.categorie },
    { label: t("detail.fields.echelon"), value: employee.echelon ?? "—" },
    { label: t("detail.fields.cnps"), value: employee.numCnps ?? "—" },
    {
      label: t("detail.fields.paymentMode"),
      value: <Badge tone="gray" withDot={false}>{t(`paymentModes.${employee.modePaiement}` as never)}</Badge>,
    },
    {
      label: t("detail.fields.bankAccount"),
      value: employee.compteBancaire ?? "—",
    },
    {
      label: t("detail.fields.mobileMoney"),
      value: employee.numMobileMoney
        ? `${employee.numMobileMoney}${employee.operateurMm ? ` (${employee.operateurMm})` : ""}`
        : "—",
    },
  ];

  return (
    <Card>
      <CardContent>
        <dl className="grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
          {fields.map((f) => (
            <div key={f.label} className="flex flex-col gap-1 border-b border-line-soft py-2 last:border-b-0">
              <dt className="text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                {f.label}
              </dt>
              <dd className="text-[14px] text-ink">{f.value}</dd>
            </div>
          ))}
        </dl>
      </CardContent>
    </Card>
  );
}
