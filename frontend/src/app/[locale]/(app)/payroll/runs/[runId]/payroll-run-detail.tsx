"use client";

import { useTranslations } from "next-intl";
import { Loader2, ShieldCheck, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import { usePayrollRun, usePayrollEntries, useValidatePayrollRun } from "@/hooks/modules/usePayroll";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { WorkflowTimeline } from "@/components/ui-tokens/WorkflowTimeline";
import { Link } from "@/i18n/navigation";

export function PayrollRunDetail({ runId }: { runId: string }) {
  const t = useTranslations("accounting.payroll");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const run = usePayrollRun(runId);
  const entries = usePayrollEntries(runId);
  const employees = useEmployees();
  const validate = useValidatePayrollRun();

  if (run.isLoading) return <Skeleton className="h-40 w-full" />;
  if (!run.data) {
    return (
      <Card>
        <CardContent className="flex items-start gap-3">
          <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
          <div className="text-sm">Payroll run not found</div>
        </CardContent>
      </Card>
    );
  }

  const r = run.data;
  const empMap = new Map((employees.data ?? []).map((e) => [e.id, e]));
  const order = ["CALCULATED", "VALIDATED", "PAID"];
  const ci = order.indexOf(r.status);
  const steps = order.map((s, i) => ({
    key: s,
    label: s,
    state: i < ci ? ("done" as const) : i === ci ? ("active" as const) : ("todo" as const),
  }));

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-06/07"
        crumbs={[{ label: tNav("items.payroll"), href: "/payroll" }, { label: r.periode }]}
        title={
          <div className="flex flex-wrap items-center gap-3">
            <span>{r.periode}</span>
            <StatusBadge kind="payrollRun" status={r.status} />
          </div>
        }
        subtitle={`${r.nbEmployes} employés — calculé le ${fmt.date(r.calculatedAt)}`}
        actions={
          r.status === "CALCULATED" && (
            <Button
              onClick={() =>
                validate.mutate(runId, {
                  onSuccess: () => toast.success(t("detail.validate")),
                  onError: (err) => toast.error((err as Error).message),
                })
              }
              disabled={validate.isPending}
            >
              {validate.isPending ? <Loader2 className="size-4 animate-spin" /> : <ShieldCheck className="size-4" />}
              {t("detail.validate")}
            </Button>
          )
        }
      />

      <Card>
        <CardContent>
          <WorkflowTimeline steps={steps} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t("detail.summary")}</CardTitle>
        </CardHeader>
        <CardContent>
          <dl className="grid grid-cols-2 gap-x-8 gap-y-4 sm:grid-cols-3">
            <Field label={t("detail.totalBrut")} value={fmt.money(r.totalBrut)} />
            <Field label={t("detail.totalNet")} value={fmt.money(r.totalNet)} />
            <Field label={t("detail.totalCnpsEmploye")} value={fmt.money(r.totalCnpsEmploye)} />
            <Field label={t("detail.totalCnpsEmployeur")} value={fmt.money(r.totalCnpsEmployeur)} />
            <Field label={t("detail.totalIrpp")} value={fmt.money(r.totalIrpp)} />
            <Field label={t("detail.nbEmployes")} value={String(r.nbEmployes)} />
          </dl>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t("detail.entries")}</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {entries.isLoading && <Skeleton className="m-6 h-24" />}
          {!entries.isLoading && entries.data && entries.data.length === 0 && (
            <div className="py-8 text-center text-sm text-ink-3">—</div>
          )}
          {!entries.isLoading && entries.data && entries.data.length > 0 && (
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("detail.entryTable.employee"),
                    t("detail.entryTable.salaireBase"),
                    t("detail.entryTable.brut"),
                    t("detail.entryTable.cotisations"),
                    t("detail.entryTable.impots"),
                    t("detail.entryTable.avances"),
                    t("detail.entryTable.net"),
                    t("detail.entryTable.paymentStatus"),
                  ].map((h, i) => (
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
                {entries.data.map((e) => {
                  const emp = empMap.get(e.employeeId);
                  const cotisations = Number(e.cnpsEmploye) + Number(e.cnpsEmployeur);
                  const impots = Number(e.irpp) + Number(e.cac);
                  return (
                    <tr key={e.id} className="cursor-pointer hover:bg-brand-50/40">
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                        <Link href={`/payroll/entries/${e.id}` as never} className="hover:text-brand-700">
                          {emp ? `${emp.matricule} — ${emp.actorDisplayName}` : e.employeeId.slice(0, 8)}
                        </Link>
                      </td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(e.salaireBase)}</td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(e.brut)}</td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(cotisations)}</td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(impots)}</td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(e.avancesDeduites)}</td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-semibold text-ink tabular text-right">{fmt.money(e.net)}</td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">{e.paymentStatus}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1 border-b border-line-soft py-2 last:border-b-0">
      <dt className="text-[11px] font-semibold uppercase tracking-wider text-ink-3">{label}</dt>
      <dd className="text-[14px] text-ink tabular">{value}</dd>
    </div>
  );
}
