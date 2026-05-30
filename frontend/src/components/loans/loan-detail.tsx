"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Check, ChevronLeft, Coins, Loader2, X } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatMoney } from "@/lib/format";
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

export function LoanDetail({ loanId }: { loanId: string }) {
  const t = useTranslations("loans");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const { session } = useSession();
  const canApprove = useCan("hrm:loan:approve");

  const query = useQuery({
    queryKey: ["hrm", "loans", loanId],
    queryFn: () => apiFetch<LoanAdvanceResponse>(`/api/hrm/loans/${loanId}`),
  });
  const meQuery = useQuery({
    queryKey: ["hrm", "loans", "mine"],
    queryFn: () =>
      apiFetch<{ employee: EmployeeResponse | null }>(`/api/hrm/loans/mine`),
    enabled: !!session,
  });
  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
    enabled: canApprove,
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "loans"] });
  }, [queryClient]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const approveM = useMutation({
    mutationFn: () => apiFetch(`/api/hrm/loans/${loanId}/approve`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("detail.approveSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const rejectM = useMutation({
    mutationFn: (motif: string) =>
      apiFetch(`/api/hrm/loans/${loanId}/reject`, { method: "POST", body: { motif } }),
    onSuccess: () => {
      toast.success(t("detail.rejectSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  function onReject() {
    const motif = window.prompt(t("detail.rejectPrompt"), "");
    if (motif === null) return;
    rejectM.mutate(motif.trim() || "—");
  }

  if (query.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (query.error || !query.data) {
    return (
      <Card>
        <div className="px-5 py-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      </Card>
    );
  }

  const l = query.data;
  const kind = loanKindOf(l.nbEcheances);
  const montant = Number(l.montant ?? 0);
  const remaining = Number(l.soldeRestant ?? 0);
  const monthly = Number(l.mensualite ?? 0);
  const pct = loanProgressPct(montant, remaining);
  const reference = shortLoanRef(l.id, kind);
  const employeeName =
    employeesQuery.data?.find((e) => e.id === l.employeeId)?.actorDisplayName ??
    (meQuery.data?.employee?.id === l.employeeId
      ? meQuery.data.employee.actorDisplayName
      : null) ??
    `${l.employeeId.slice(0, 8)}…`;

  const isMine = meQuery.data?.employee?.id === l.employeeId;
  const backHref = canApprove ? "/loans" : "/loans/mine";
  const showApproveReject = canApprove && l.status === "PENDING";

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          {
            label: canApprove ? t("title") : t("mine.title"),
            href: backHref,
          },
          { label: reference },
        ]}
        title={
          <span className="flex items-center gap-3">
            <Coins className="h-6 w-6 text-orange-500" />
            {t("detail.title", { ref: reference })}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={loanStatusTone(l.status)}>{t(`status.${l.status}`)}</Badge>
            <Badge tone={loanKindTone(kind)}>{t(`kind.${kind}`)}</Badge>
            <span className="font-mono-tabular text-[12px] text-ink-3">{reference}</span>
          </span>
        }
        actions={
          <>
            <Link href={backHref}>
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("detail.back")}
              </Button>
            </Link>
            {showApproveReject ? (
              <>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={onReject}
                  disabled={rejectM.isPending}
                >
                  <X className="h-4 w-4" />
                  {t("detail.reject")}
                </Button>
                <Button
                  type="button"
                  onClick={() => approveM.mutate()}
                  disabled={approveM.isPending}
                >
                  {approveM.isPending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Check className="h-4 w-4" />
                  )}
                  {t("detail.approve")}
                </Button>
              </>
            ) : null}
          </>
        }
      />

      <div className="grid grid-cols-1 gap-5 lg:grid-cols-[2fr_1fr]">
        <Card>
          <CardContent padding="lg">
            <div className="mb-5 flex items-center gap-3">
              <Avatar name={employeeName} size="md" />
              <div>
                <div className="text-[15px] font-bold text-ink">{employeeName}</div>
                <div className="text-[11px] uppercase tracking-wider text-ink-3">
                  {t("detail.employee")}
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-x-6 gap-y-5 sm:grid-cols-3">
              <SummaryCell
                label={t("detail.amount")}
                value={formatMoney(montant, { locale, withCurrency: false })}
                unit="XAF"
                big
              />
              <SummaryCell
                label={t("detail.monthly")}
                value={formatMoney(monthly, { locale, withCurrency: false })}
                unit="XAF"
              />
              <SummaryCell
                label={t("detail.remaining")}
                value={formatMoney(remaining, { locale, withCurrency: false })}
                unit="XAF"
              />
              <SummaryCell
                label={t("detail.term")}
                value={t("table.term", { months: l.nbEcheances })}
              />
              <SummaryCell
                label={t("detail.startDate")}
                value={formatDate(l.dateDebut, { locale, pattern: "d MMM yyyy" })}
              />
              <SummaryCell label={t("detail.type")} value={t(`kind.${kind}`)} />
            </div>

            <div className="mt-6">
              <div className="mb-2 flex items-center justify-between text-[11.5px] text-ink-3">
                <span className="uppercase tracking-wider">{t("detail.progress")}</span>
                <span className="font-mono-tabular font-bold text-ink">{pct}%</span>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-bg-soft">
                <div
                  className={cn("h-full rounded-full bg-grad-orange")}
                  style={{ width: `${pct}%` }}
                />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent padding="lg">
            <div className="mb-3 text-[14px] font-bold tracking-tight text-ink">
              {t("detail.summary")}
            </div>
            <div className="flex flex-col gap-3 text-[12.5px]">
              <Row label={t("detail.motif")}>
                <span className="text-ink-2">{l.motif?.trim() || "—"}</span>
              </Row>
              <Row label={t("detail.approvedBy")}>
                <span className="font-mono-tabular text-[11.5px] text-ink-3">
                  {l.approvedBy ? `${l.approvedBy.slice(0, 8)}…` : "—"}
                </span>
              </Row>
              {isMine ? (
                <div className="mt-2 rounded-md bg-bg-soft px-3 py-2 text-[11.5px] text-ink-3">
                  {t("mine.title")}
                </div>
              ) : null}
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}

function SummaryCell({
  label,
  value,
  unit,
  big,
}: {
  label: string;
  value: React.ReactNode;
  unit?: string;
  big?: boolean;
}) {
  return (
    <div>
      <div className="text-[10.5px] uppercase tracking-wider text-ink-3">{label}</div>
      <div
        className={cn(
          "font-mono-tabular tracking-tight text-ink",
          big ? "font-display text-[22px] font-extrabold" : "text-[14px] font-semibold",
        )}
      >
        {value}
        {unit ? (
          <span className="ml-1 text-[11px] font-semibold text-ink-3">{unit}</span>
        ) : null}
      </div>
    </div>
  );
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-3 border-b border-line-soft pb-2 last:border-0 last:pb-0">
      <span className="text-[11.5px] uppercase tracking-wider text-ink-3">{label}</span>
      <span className="text-right">{children}</span>
    </div>
  );
}
