"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { ChevronLeft, Loader2, Send } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Textarea } from "@/components/ui/input";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import { loanKindOf, loanKindTone } from "@/lib/loan-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { LoanAdvanceResponse } from "@/server/ksm/modules/loans";

type MinePayload = { employee: EmployeeResponse | null; loans: LoanAdvanceResponse[] };

export function NewLoanForm() {
  const t = useTranslations("loans");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();

  const [amount, setAmount] = React.useState("");
  const [term, setTerm] = React.useState("1");
  const [motif, setMotif] = React.useState("");

  const mine = useQuery({
    queryKey: ["hrm", "loans", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/loans/mine"),
  });

  const amountNum = Number(amount);
  const termNum = Number(term);
  const amountValid = Number.isFinite(amountNum) && amountNum > 0;
  const termValid = Number.isInteger(termNum) && termNum >= 1 && termNum <= 60;
  const monthly = amountValid && termValid ? amountNum / termNum : 0;
  const kind = termValid ? loanKindOf(termNum) : "ADVANCE";

  const persist = useMutation({
    mutationFn: async () => {
      const employeeId = mine.data?.employee?.id;
      if (!employeeId) throw new Error(t("new.noEmployee"));
      return apiFetch<LoanAdvanceResponse>("/api/hrm/loans", {
        method: "POST",
        body: {
          employeeId,
          montant: amountNum,
          nbEcheances: termNum,
          motif: motif.trim() || null,
        },
      });
    },
    onSuccess: (data) => {
      toast.success(t("new.success"));
      router.push(`/loans/${data.id}`);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else if (cause instanceof Error) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  const canSubmit = amountValid && termValid && !!mine.data?.employee && !persist.isPending;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("mine.title"), href: "/loans/mine" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
        actions={
          <Link href="/loans/mine">
            <Button type="button" variant="secondary">
              <ChevronLeft className="h-4 w-4" />
              {t("detail.back")}
            </Button>
          </Link>
        }
      />

      {mine.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : !mine.data?.employee ? (
        <Card>
          <CardContent padding="lg">
            <div className="text-center text-[13px] text-ink-3">{t("new.noEmployee")}</div>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-[2fr_1fr]">
          <Card>
            <CardContent padding="lg">
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  if (canSubmit) persist.mutate();
                }}
                className="flex flex-col gap-5"
              >
                <Field
                  label={t("new.amountLabel")}
                  hint={t("new.amountHelp")}
                  error={!amountValid && amount ? t("new.amountInvalid") : undefined}
                >
                  <Input
                    type="number"
                    inputMode="numeric"
                    min={1}
                    step={1}
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    placeholder="100000"
                  />
                </Field>
                <Field
                  label={t("new.termLabel")}
                  hint={t("new.termHelp")}
                  error={!termValid && term ? t("new.termInvalid") : undefined}
                >
                  <Input
                    type="number"
                    inputMode="numeric"
                    min={1}
                    max={60}
                    step={1}
                    value={term}
                    onChange={(e) => setTerm(e.target.value)}
                  />
                </Field>
                <Field label={t("new.motifLabel")}>
                  <Textarea
                    rows={3}
                    value={motif}
                    onChange={(e) => setMotif(e.target.value)}
                    placeholder={t("new.motifPlaceholder")}
                  />
                </Field>
                <div>
                  <Button type="submit" disabled={!canSubmit}>
                    {persist.isPending ? (
                      <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                      <Send className="h-4 w-4" />
                    )}
                    {t("new.submit")}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>

          <Card>
            <CardContent padding="lg">
              <div className="mb-3 text-[14px] font-bold tracking-tight text-ink">
                {t("detail.summary")}
              </div>
              <div className="flex flex-col gap-3 text-[12.5px]">
                <Row label={t("detail.type")}>
                  <Badge tone={loanKindTone(kind)}>{t(`kind.${kind}`)}</Badge>
                </Row>
                <Row label={t("detail.amount")}>
                  <span className="font-mono-tabular font-bold text-ink">
                    {amountValid
                      ? `${formatMoney(amountNum, { locale, withCurrency: false })} XAF`
                      : "—"}
                  </span>
                </Row>
                <Row label={t("detail.term")}>
                  <span className="font-mono-tabular text-ink-2">
                    {termValid ? t("table.term", { months: termNum }) : "—"}
                  </span>
                </Row>
                <Row label={t("new.monthlyLabel")}>
                  <span className="font-mono-tabular font-bold text-orange-600">
                    {monthly > 0
                      ? `${formatMoney(monthly, { locale, withCurrency: false })} XAF`
                      : "—"}
                  </span>
                </Row>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </>
  );
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between gap-3 border-b border-line-soft pb-2 last:border-0 last:pb-0">
      <span className="text-[11.5px] uppercase tracking-wider text-ink-3">{label}</span>
      <span>{children}</span>
    </div>
  );
}
