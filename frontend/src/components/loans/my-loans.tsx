"use client";

import { useQuery } from "@tanstack/react-query";
import { Coins, Loader2, Plus } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import {
  loanKindOf,
  loanKindTone,
  loanProgressPct,
  loanStatusTone,
  shortLoanRef,
} from "@/lib/loan-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { LoanAdvanceResponse } from "@/server/ksm/modules/loans";

type MinePayload = { employee: EmployeeResponse | null; loans: LoanAdvanceResponse[] };

export function MyLoans() {
  const t = useTranslations("loans");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();

  const query = useQuery({
    queryKey: ["hrm", "loans", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/loans/mine"),
    refetchInterval: 60_000,
  });

  const loans = React.useMemo(() => query.data?.loans ?? [], [query.data]);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
        actions={
          <Link href="/loans/new">
            <Button>
              <Plus className="h-4 w-4" />
              {t("mine.newCta")}
            </Button>
          </Link>
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <Card>
          <div className="px-5 py-10 text-center text-ink-3">
            {query.error instanceof BffApiError ? query.error.message : "—"}
          </div>
        </Card>
      ) : loans.length === 0 ? (
        <Card>
          <div className="px-5 py-16 text-center text-[13px] text-ink-3">
            <Coins className="mx-auto mb-3 h-8 w-8 text-ink-4" />
            {t("mine.empty")}
          </div>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {loans.map((l) => {
            const kind = loanKindOf(l.nbEcheances);
            const montant = Number(l.montant ?? 0);
            const remaining = Number(l.soldeRestant ?? 0);
            const monthly = Number(l.mensualite ?? 0);
            const pct = loanProgressPct(montant, remaining);
            return (
              <button
                key={l.id}
                type="button"
                onClick={() => router.push(`/loans/${l.id}`)}
                className="group flex flex-col gap-3 rounded-[20px] border border-line bg-white p-5 text-left shadow-xs-brand transition-shadow hover:shadow-orange-brand"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="font-mono-tabular text-[11px] uppercase tracking-wider text-ink-3">
                      {shortLoanRef(l.id, kind)}
                    </div>
                    <div className="mt-1 flex items-center gap-2">
                      <Badge tone={loanKindTone(kind)}>{t(`kind.${kind}`)}</Badge>
                      <Badge tone={loanStatusTone(l.status)}>{t(`status.${l.status}`)}</Badge>
                    </div>
                  </div>
                </div>
                <div>
                  <div className="text-[11px] uppercase tracking-wider text-ink-3">
                    {t("detail.amount")}
                  </div>
                  <div className="font-display font-mono-tabular text-[22px] font-extrabold tracking-tight text-ink">
                    {formatMoney(montant, { locale, withCurrency: false })}{" "}
                    <span className="text-[12px] font-semibold text-ink-3">XAF</span>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-3 text-[12px]">
                  <Field label={t("detail.monthly")}>
                    {formatMoney(monthly, { locale, withCurrency: false })}
                  </Field>
                  <Field label={t("detail.remaining")}>
                    {formatMoney(remaining, { locale, withCurrency: false })}
                  </Field>
                </div>
                <div>
                  <div className="mb-1 flex items-center justify-between text-[11px] text-ink-3">
                    <span>{t("detail.progress")}</span>
                    <span className="font-mono-tabular">{pct}%</span>
                  </div>
                  <div className="h-1.5 overflow-hidden rounded-full bg-bg-soft">
                    <div
                      className={cn("h-full rounded-full bg-grad-orange")}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      )}
    </>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <div className="text-[10px] uppercase tracking-wider text-ink-3">{label}</div>
      <div className="font-mono-tabular text-[13px] font-semibold text-ink">{children}</div>
    </div>
  );
}
