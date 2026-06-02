"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Check, ChevronLeft, Loader2, Send, Wallet, X } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { categoryTone, expenseStatusTone } from "@/lib/expense-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  ExpenseLineResponse,
  ExpenseReportResponse,
  ExpenseReportStatus,
} from "@/server/ksm/modules/expenses";

export function ExpenseDetail({ expenseReportId }: { expenseReportId: string }) {
  const t = useTranslations("expenses");
  const tCat = useTranslations("expenses.category");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const { session } = useSession();
  const canManage = useCan("hrm:expense:manage");
  const canCreate = useCan("hrm:expense:create");

  const query = useQuery({
    queryKey: ["hrm", "expense", expenseReportId],
    queryFn: () => apiFetch<ExpenseReportResponse>(`/api/hrm/expenses/${expenseReportId}`),
  });
  const linesQuery = useQuery({
    queryKey: ["hrm", "expense", expenseReportId, "lines"],
    queryFn: () => apiFetch<ExpenseLineResponse[]>(`/api/hrm/expenses/${expenseReportId}/lines`),
  });
  const meQuery = useQuery({
    queryKey: ["hrm", "expenses", "mine"],
    queryFn: () =>
      apiFetch<{ employee: EmployeeResponse | null }>(`/api/hrm/expenses/mine`),
    enabled: !!session,
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "expense", expenseReportId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "expenses"] });
  }, [queryClient, expenseReportId]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const action = (path: string) =>
    apiFetch<ExpenseReportResponse>(`/api/hrm/expenses/${expenseReportId}/${path}`, { method: "POST" });

  const submitM = useMutation({ mutationFn: () => action("submit"), onSuccess: () => { toast.success(t("detail.submitSuccess")); invalidate(); }, onError: handleError });
  const approveM = useMutation({ mutationFn: () => action("approve"), onSuccess: () => { toast.success(t("detail.approveSuccess")); invalidate(); }, onError: handleError });
  const rejectM = useMutation({ mutationFn: () => action("reject"), onSuccess: () => { toast.success(t("detail.rejectSuccess")); invalidate(); }, onError: handleError });
  const reimburseM = useMutation({ mutationFn: () => action("reimburse"), onSuccess: () => { toast.success(t("detail.reimburseSuccess")); invalidate(); }, onError: handleError });

  if (query.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (query.error || !query.data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {query.error instanceof BffApiError ? query.error.message : "—"}
      </div>
    );
  }

  const r = query.data;
  const lines = linesQuery.data ?? [];
  const isMine = !!meQuery.data?.employee && meQuery.data.employee.id === r.employeeId;
  const reference = shortRef(r.id);

  const showSubmit = canCreate && isMine && r.status === "DRAFT";
  const showApproveReject = canManage && r.status === "SUBMITTED";
  const showReimburse = canManage && r.status === "APPROVED";

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: canManage ? t("title") : t("mine.title"), href: canManage ? "/expenses" : "/expenses/mine" },
          { label: reference },
        ]}
        title={
          <span className="flex items-center gap-3">
            <Wallet className="h-6 w-6 text-orange-500" />
            {r.motif ?? reference}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={expenseStatusTone(r.status)}>{t(`status.${r.status}`)}</Badge>
            <span className="font-mono-tabular text-[12px] text-ink-3">{reference}</span>
            <span className="text-[12.5px] text-ink-3">{r.periode}</span>
          </span>
        }
        actions={
          <>
            <Link href={canManage ? "/expenses" : "/expenses/mine"}>
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("detail.back")}
              </Button>
            </Link>
            {showApproveReject && (
              <Button type="button" variant="secondary" onClick={() => rejectM.mutate()} disabled={rejectM.isPending}>
                <X className="h-4 w-4" />
                {t("detail.actions.reject")}
              </Button>
            )}
            {showApproveReject && (
              <Button type="button" onClick={() => approveM.mutate()} disabled={approveM.isPending}>
                {approveM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4" />}
                {t("detail.actions.approve")}
              </Button>
            )}
            {showReimburse && (
              <Button type="button" onClick={() => reimburseM.mutate()} disabled={reimburseM.isPending}>
                {reimburseM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Wallet className="h-4 w-4" />}
                {t("detail.actions.reimburse")}
              </Button>
            )}
            {showSubmit && (
              <Button type="button" onClick={() => submitM.mutate()} disabled={submitM.isPending}>
                {submitM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                {t("detail.actions.submit")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={workflowSteps(r.status, t)} />
        </CardContent>
      </Card>

      <Card>
        <CardContent padding="lg">
          <dl className="mb-5 grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-3">
            <Detail label={t("detail.period")} value={r.periode} mono />
            <Detail label={t("detail.submittedBy")} value={`${r.employeeId.slice(0, 8)}…`} mono />
            {r.missionOrderId && (
              <Detail
                label={t("detail.mission")}
                value={
                  <Link
                    href={`/mission-orders/${r.missionOrderId}`}
                    className="font-mono-tabular text-orange-600 hover:text-orange-700"
                  >
                    {t("detail.missionLink")} · {r.missionOrderId.slice(0, 8)}…
                  </Link>
                }
              />
            )}
          </dl>

          <div className="mb-3 text-[13px] font-bold text-ink">{t("detail.lines.title")}</div>
          {linesQuery.isLoading ? (
            <Loader2 className="h-5 w-5 animate-spin text-orange-500" />
          ) : lines.length === 0 ? (
            <p className="rounded-[12px] border border-dashed border-line bg-bg-soft px-4 py-6 text-center text-[12.5px] text-ink-3">
              {t("detail.lines.empty")}
            </p>
          ) : (
            <div className="overflow-hidden rounded-[12px] border border-line">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="bg-bg-dim">
                    <th className="px-4 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">{t("detail.lines.category")}</th>
                    <th className="px-4 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">{t("detail.lines.description")}</th>
                    <th className="px-4 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">{t("detail.lines.justificatif")}</th>
                    <th className="px-4 py-2.5 text-right text-[11px] font-semibold uppercase tracking-wider text-ink-3">{t("detail.lines.amount")}</th>
                  </tr>
                </thead>
                <tbody>
                  {lines.map((l) => (
                    <tr key={l.id} className="border-t border-line-soft">
                      <td className="px-4 py-2.5">
                        <Badge tone={categoryTone(l.categorie)}>{categoryLabel(l.categorie, tCat)}</Badge>
                      </td>
                      <td className="px-4 py-2.5 text-[13px] text-ink">{l.description}</td>
                      <td className="px-4 py-2.5">
                        {l.justificatifFileId ? (
                          <a
                            href={`/api/files/${l.justificatifFileId}`}
                            target="_blank"
                            rel="noreferrer"
                            className="inline-flex"
                          >
                            <Badge tone="success">{t("detail.lines.attached")}</Badge>
                          </a>
                        ) : (
                          <Badge tone="danger">{t("detail.lines.missing")}</Badge>
                        )}
                      </td>
                      <td className="px-4 py-2.5 text-right font-mono-tabular text-[13px] font-semibold text-ink">
                        {formatNumber(Number(l.montant), locale)}
                      </td>
                    </tr>
                  ))}
                  <tr className="border-t border-line bg-bg-dim">
                    <td colSpan={3} className="px-4 py-3 text-[13px] font-bold text-ink">
                      {t("detail.total")}
                    </td>
                    <td className="px-4 py-3 text-right font-display font-mono-tabular text-[16px] font-extrabold text-ink">
                      {formatNumber(Number(r.totalMontant ?? 0), locale)}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          )}
          <p className="mt-6 text-[12px] text-ink-4">
            ID · <span className="font-mono-tabular">{r.id}</span>
          </p>
        </CardContent>
      </Card>
    </>
  );
}

function Detail({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div>
      <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">{label}</Label>
      <p className={mono ? "mt-0.5 font-mono-tabular text-[13.5px] font-bold text-ink" : "mt-0.5 text-[14px] font-medium text-ink"}>
        {value}
      </p>
    </div>
  );
}

function categoryLabel(categorie: string | null, tCat: (k: string) => string): string {
  const up = (categorie ?? "").toUpperCase();
  const known = ["TRANSPORT", "REPAS", "HEBERGEMENT", "MATERIEL", "AUTRES"];
  return known.includes(up) ? tCat(up) : categorie ?? "—";
}

function workflowSteps(status: ExpenseReportStatus, t: (k: string) => string): WorkflowStep[] {
  if (status === "REJECTED") {
    return [
      { key: "draft", label: t("detail.workflow.draft"), state: "done" },
      { key: "submitted", label: t("detail.workflow.submitted"), state: "done" },
      { key: "decision", label: t("detail.workflow.rejected"), state: "active" },
    ];
  }
  const order: ExpenseReportStatus[] = ["DRAFT", "SUBMITTED", "APPROVED", "REIMBURSED"];
  const idx = order.indexOf(status);
  const mk = (key: string, label: string, pos: number): WorkflowStep => ({
    key,
    label,
    state: pos < idx ? "done" : pos === idx ? "active" : "pending",
  });
  return [
    mk("draft", t("detail.workflow.draft"), 0),
    mk("submitted", t("detail.workflow.submitted"), 1),
    mk("approved", t("detail.workflow.approved"), 2),
    mk("reimbursed", t("detail.workflow.reimbursed"), 3),
  ];
}

function shortRef(uuid: string): string {
  return `NF-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}
