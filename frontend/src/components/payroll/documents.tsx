"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  BadgeCheck,
  Download,
  FileSignature,
  FileText,
  Loader2,
  type LucideIcon,
  Receipt,
  ScrollText,
  ShieldAlert,
  ShieldCheck,
  ShieldQuestion,
  User,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { toast } from "sonner";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { IconTile } from "@/components/ui/icon-tile";
import { Field, Input } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatPeriodShort } from "@/lib/payroll-status";
import { cn } from "@/lib/utils";
import type { ContractResponse, EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  DocumentVerification,
  FinalSettlementResponse,
  PayrollDocumentResponse,
  PayrollDocumentType,
  PayrollEntryResponse,
  PayrollRunResponse,
  PayrollRunStatus,
} from "@/server/ksm/modules/payroll";

function typeMeta(type: PayrollDocumentType | string): {
  icon: LucideIcon;
  tone: "info" | "warning" | "success";
} {
  switch (type) {
    case "PAYSLIP":
      return { icon: Receipt, tone: "info" };
    case "FINAL_SETTLEMENT":
      return { icon: ScrollText, tone: "warning" };
    default:
      return { icon: BadgeCheck, tone: "success" };
  }
}

type DialogKind = "payslip" | "certificate" | "settlement" | null;
type VerifyState = "loading" | DocumentVerification;

export function Documents() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canManage = useCan("hrm:payroll:run");
  const qc = useQueryClient();

  const [employeeId, setEmployeeId] = React.useState("");
  const [search, setSearch] = React.useState("");
  const [dialog, setDialog] = React.useState<DialogKind>(null);
  const [verifications, setVerifications] = React.useState<Record<string, VerifyState>>({});

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const documentsQuery = useQuery({
    queryKey: ["hrm", "payroll", "documents", employeeId],
    queryFn: () =>
      apiFetch<PayrollDocumentResponse[]>(
        `/api/hrm/payroll/documents?employeeId=${encodeURIComponent(employeeId)}`,
      ),
    enabled: Boolean(employeeId),
  });

  const employees = React.useMemo(() => {
    const q = search.trim().toLowerCase();
    return (employeesQuery.data ?? [])
      .filter((e) => {
        if (!q) return true;
        const name = (e.actorDisplayName ?? "").toLowerCase();
        return name.includes(q) || e.matricule.toLowerCase().includes(q);
      })
      .sort((a, b) => (a.actorDisplayName ?? a.matricule).localeCompare(b.actorDisplayName ?? b.matricule));
  }, [employeesQuery.data, search]);

  const selectedEmployee = React.useMemo(
    () => (employeesQuery.data ?? []).find((e) => e.id === employeeId) ?? null,
    [employeesQuery.data, employeeId],
  );

  const docs = React.useMemo(
    () =>
      (documentsQuery.data ?? [])
        .slice()
        .sort((a, b) => (b.signedAt ?? "").localeCompare(a.signedAt ?? "")),
    [documentsQuery.data],
  );

  const counts = React.useMemo(
    () => ({
      total: docs.length,
      payslips: docs.filter((d) => d.type === "PAYSLIP").length,
      settlements: docs.filter((d) => d.type === "FINAL_SETTLEMENT").length,
      certificates: docs.filter((d) => d.type === "WORK_CERTIFICATE").length,
    }),
    [docs],
  );

  const onGenerated = (label: string) => {
    toast.success(label);
    qc.invalidateQueries({ queryKey: ["hrm", "payroll", "documents", employeeId] });
    setDialog(null);
  };
  const onGenError = (e: unknown) =>
    toast.error(e instanceof BffApiError ? e.message : t("documents.genError"));

  const verify = useMutation({
    mutationFn: (id: string) =>
      apiFetch<DocumentVerification>(`/api/hrm/payroll/documents/${id}/verify`),
    onMutate: (id) => setVerifications((p) => ({ ...p, [id]: "loading" })),
    onSuccess: (data, id) => {
      setVerifications((p) => ({ ...p, [id]: data }));
      toast[data.valid ? "success" : "error"](
        data.valid ? t("documents.verifyValid") : t("documents.verifyInvalid"),
      );
    },
    onError: (e, id) => {
      setVerifications((p) => {
        const next = { ...p };
        delete next[id];
        return next;
      });
      toast.error(e instanceof BffApiError ? e.message : t("documents.verifyError"));
    },
  });

  return (
    <>
      <PageHeader
        ucBadge={t("documents.uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }, { label: t("documents.title") }]}
        title={t("documents.title")}
        subtitle={t("documents.subtitle")}
      />

      <div className="flex flex-col gap-5">
        <Card className="p-5">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div className="w-full sm:max-w-md">
              <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                {t("documents.selectEmployee")}
              </label>
              <div className="flex gap-2">
                <Input
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder={t("documents.searchEmployee")}
                  className="sm:w-44"
                />
                <select
                  value={employeeId}
                  onChange={(e) => setEmployeeId(e.target.value)}
                  className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
                >
                  <option value="">{t("documents.employeePlaceholder")}</option>
                  {employees.map((e) => (
                    <option key={e.id} value={e.id}>
                      {(e.actorDisplayName ?? e.matricule) + " · " + e.matricule}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            {selectedEmployee && (
              <div className="flex items-center gap-2.5 rounded-[12px] border border-line-soft bg-bg-soft/60 px-3.5 py-2">
                <IconTile icon={User} tone="orange" size="sm" />
                <div>
                  <div className="text-[13.5px] font-semibold text-ink">
                    {selectedEmployee.actorDisplayName ?? selectedEmployee.matricule}
                  </div>
                  <div className="font-mono-tabular text-[11px] text-ink-3">
                    {selectedEmployee.matricule}
                  </div>
                </div>
              </div>
            )}
          </div>

          {selectedEmployee && canManage && (
            <div className="mt-4 flex flex-wrap gap-2 border-t border-line-soft pt-4">
              <Button variant="secondary" size="sm" onClick={() => setDialog("payslip")}>
                <Receipt className="h-4 w-4" />
                {t("documents.generatePayslip")}
              </Button>
              <Button variant="secondary" size="sm" onClick={() => setDialog("certificate")}>
                <BadgeCheck className="h-4 w-4" />
                {t("documents.generateCertificate")}
              </Button>
              <Button variant="secondary" size="sm" onClick={() => setDialog("settlement")}>
                <ScrollText className="h-4 w-4" />
                {t("documents.generateSettlement")}
              </Button>
            </div>
          )}
        </Card>

        {!employeeId ? (
          <Card className="grid place-items-center gap-2 px-6 py-16 text-center">
            <IconTile icon={FileSignature} tone="orange" size="md" />
            <p className="text-[14px] font-semibold text-ink">{t("documents.noEmployeeTitle")}</p>
            <p className="max-w-sm text-[13px] text-ink-3">{t("documents.noEmployeeHint")}</p>
          </Card>
        ) : documentsQuery.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
          </div>
        ) : documentsQuery.error ? (
          <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
            {documentsQuery.error instanceof BffApiError ? documentsQuery.error.message : "—"}
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
              <StatCard icon={FileText} tone="orange" label={t("documents.stats.total")} value={String(counts.total)} />
              <StatCard icon={Receipt} tone="info" label={t("documents.type.PAYSLIP")} value={String(counts.payslips)} />
              <StatCard icon={ScrollText} tone="warning" label={t("documents.type.FINAL_SETTLEMENT")} value={String(counts.settlements)} />
              <StatCard icon={BadgeCheck} tone="success" label={t("documents.type.WORK_CERTIFICATE")} value={String(counts.certificates)} />
            </div>

            <Card>
              <div className="border-b border-line-soft px-6 py-4">
                <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("documents.table.title")}</h3>
                <p className="text-[12px] text-ink-3">{t("documents.table.subtitle")}</p>
              </div>
              {docs.length === 0 ? (
                <div className="px-6 py-12 text-center text-[13px] text-ink-3">{t("documents.table.empty")}</div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-[13px]">
                    <thead>
                      <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                        <th className="px-6 py-3">{t("documents.cols.type")}</th>
                        <th className="px-3 py-3">{t("documents.cols.period")}</th>
                        <th className="px-3 py-3">{t("documents.cols.signedAt")}</th>
                        <th className="px-3 py-3">{t("documents.cols.code")}</th>
                        <th className="px-3 py-3">{t("documents.cols.seal")}</th>
                        <th className="px-6 py-3 text-right">{t("documents.cols.actions")}</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-line-soft">
                      {docs.map((d) => {
                        const meta = typeMeta(d.type);
                        const v = verifications[d.id];
                        return (
                          <tr key={d.id} className="hover:bg-bg-soft">
                            <td className="px-6 py-3">
                              <div className="flex items-center gap-2.5">
                                <IconTile icon={meta.icon} tone={meta.tone} size="sm" />
                                <div>
                                  <div className="text-[13px] font-semibold text-ink">
                                    {t(`documents.type.${d.type}`)}
                                  </div>
                                  <div className="text-[11.5px] text-ink-3">{d.fileName}</div>
                                </div>
                              </div>
                            </td>
                            <td className="px-3 py-3 text-ink-2">
                              {d.periode ? formatPeriodShort(d.periode, locale) : "—"}
                            </td>
                            <td className="px-3 py-3 text-[12.5px] text-ink-2">
                              {d.signedAt ? new Date(d.signedAt).toLocaleString(locale === "fr" ? "fr-FR" : "en-US") : "—"}
                            </td>
                            <td className="font-mono-tabular px-3 py-3 text-[12px] text-ink-3">
                              {d.verificationCode ?? "—"}
                            </td>
                            <td className="px-3 py-3">
                              {v === "loading" ? (
                                <span className="inline-flex items-center gap-1 text-[12px] text-ink-3">
                                  <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                  {t("documents.verifying")}
                                </span>
                              ) : v ? (
                                <Badge tone={v.valid ? "success" : "warning"} showDot={false}>
                                  {v.valid ? (
                                    <>
                                      <ShieldCheck className="h-3 w-3" /> {t("documents.sealValid")}
                                    </>
                                  ) : (
                                    <>
                                      <ShieldAlert className="h-3 w-3" /> {t("documents.sealInvalid")}
                                    </>
                                  )}
                                </Badge>
                              ) : (
                                <span className="inline-flex items-center gap-1 text-[12px] text-ink-4">
                                  <ShieldQuestion className="h-3.5 w-3.5" />
                                  {t("documents.sealUnchecked")}
                                </span>
                              )}
                            </td>
                            <td className="px-6 py-3">
                              <div className="flex items-center justify-end gap-1.5">
                                <a
                                  href={`/api/files/${d.fileId}`}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="inline-flex items-center gap-1.5 rounded-[9px] border border-line bg-white px-2.5 py-1.5 text-[12.5px] font-semibold text-ink-2 shadow-xs-brand hover:bg-bg-soft"
                                >
                                  <Download className="h-3.5 w-3.5" />
                                  {t("documents.download")}
                                </a>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => verify.mutate(d.id)}
                                  disabled={v === "loading"}
                                >
                                  <ShieldCheck className="h-3.5 w-3.5" />
                                  {t("documents.verify")}
                                </Button>
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </Card>
          </>
        )}
      </div>

      {dialog === "payslip" && selectedEmployee && (
        <PayslipDialog
          employeeId={selectedEmployee.id}
          onClose={() => setDialog(null)}
          onGenerated={() => onGenerated(t("documents.payslipGenerated"))}
          onError={onGenError}
          t={t}
          locale={locale}
        />
      )}
      {dialog === "certificate" && selectedEmployee && (
        <CertificateDialog
          employeeId={selectedEmployee.id}
          onClose={() => setDialog(null)}
          onGenerated={() => onGenerated(t("documents.certificateGenerated"))}
          onError={onGenError}
          t={t}
        />
      )}
      {dialog === "settlement" && selectedEmployee && (
        <SettlementDialog
          employeeId={selectedEmployee.id}
          onClose={() => setDialog(null)}
          onGenerated={() => onGenerated(t("documents.settlementGenerated"))}
          onError={onGenError}
          t={t}
          locale={locale}
        />
      )}
    </>
  );
}

function PayslipDialog({
  employeeId,
  onClose,
  onGenerated,
  onError,
  t,
  locale,
}: {
  employeeId: string;
  onClose: () => void;
  onGenerated: () => void;
  onError: (e: unknown) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
}) {
  const [runId, setRunId] = React.useState("");
  const [error, setError] = React.useState<string | null>(null);

  const runsQuery = useQuery({
    queryKey: ["hrm", "payroll", "runs"],
    queryFn: () => apiFetch<PayrollRunResponse[]>("/api/hrm/payroll"),
  });

  const runs = React.useMemo(
    () =>
      (runsQuery.data ?? [])
        .filter((r) => r.status !== "DRAFT" && r.status !== "VARIABLES_LOCKED")
        .sort((a, b) => b.periode.localeCompare(a.periode)),
    [runsQuery.data],
  );

  const generate = useMutation({
    mutationFn: async (selectedRunId: string) => {
      const entries = await apiFetch<PayrollEntryResponse[]>(
        `/api/hrm/payroll/${selectedRunId}/entries`,
      );
      const entry = entries.find((e) => e.employeeId === employeeId);
      if (!entry) {
        throw new BffApiError({
          ok: false,
          status: 404,
          errorCode: "NO_ENTRY",
          message: t("documents.noEntry"),
        });
      }
      return apiFetch<PayrollDocumentResponse>(
        `/api/hrm/payroll/documents/payslip?entryId=${entry.id}`,
        { method: "POST" },
      );
    },
    onSuccess: onGenerated,
    onError,
  });

  return (
    <Dialog
      open
      onClose={onClose}
      size="sm"
      title={t("documents.payslipDialogTitle")}
      subtitle={t("documents.payslipDialogSubtitle")}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={generate.isPending}>
            {t("documents.dialog.cancel")}
          </Button>
          <Button
            onClick={() => {
              if (!runId) {
                setError(t("documents.selectRun"));
                return;
              }
              generate.mutate(runId);
            }}
            disabled={generate.isPending}
          >
            {generate.isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("documents.generating")}
              </>
            ) : (
              t("documents.generate")
            )}
          </Button>
        </>
      }
    >
      {runsQuery.isLoading ? (
        <div className="grid place-items-center py-6">
          <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
        </div>
      ) : runs.length === 0 ? (
        <p className="text-[13px] text-ink-3">{t("documents.noRuns")}</p>
      ) : (
        <Field label={t("documents.period")}>
          <select
            value={runId}
            onChange={(e) => setRunId(e.target.value)}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="">{t("documents.periodPlaceholder")}</option>
            {runs.map((r) => (
              <option key={r.id} value={r.id}>
                {formatPeriodShort(r.periode, locale)} · {t(`status.${r.status as PayrollRunStatus}`)}
              </option>
            ))}
          </select>
        </Field>
      )}
      {error && <p className="mt-2 text-[12px] text-danger-600">{error}</p>}
    </Dialog>
  );
}

function CertificateDialog({
  employeeId,
  onClose,
  onGenerated,
  onError,
  t,
}: {
  employeeId: string;
  onClose: () => void;
  onGenerated: () => void;
  onError: (e: unknown) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  // The job title is taken straight from the employee's active contract (set at creation);
  // we no longer ask for it. KSM resolves the same value server-side when none is sent.
  const contractsQuery = useQuery({
    queryKey: ["hrm", "contracts", employeeId],
    queryFn: () => apiFetch<ContractResponse[]>(`/api/hrm/employees/${employeeId}/contracts`),
  });

  const activeContract = (contractsQuery.data ?? []).find(
    (c) => c.status === "ACTIVE" || c.status === "TRIAL",
  );
  const savedPosition = activeContract?.position?.trim() ?? "";

  const generate = useMutation({
    mutationFn: () =>
      apiFetch<PayrollDocumentResponse>(
        `/api/hrm/payroll/documents/work-certificate?employeeId=${encodeURIComponent(employeeId)}`,
        { method: "POST" },
      ),
    onSuccess: onGenerated,
    onError,
  });

  return (
    <Dialog
      open
      onClose={onClose}
      size="sm"
      title={t("documents.certificateDialogTitle")}
      subtitle={t("documents.certificateDialogSubtitle")}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={generate.isPending}>
            {t("documents.dialog.cancel")}
          </Button>
          <Button
            onClick={() => generate.mutate()}
            disabled={generate.isPending || contractsQuery.isLoading}
          >
            {generate.isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("documents.generating")}
              </>
            ) : (
              t("documents.generate")
            )}
          </Button>
        </>
      }
    >
      <Field label={t("documents.position")}>
        {contractsQuery.isLoading ? (
          <p className="text-[13px] text-ink-3">…</p>
        ) : savedPosition ? (
          <p className="rounded-[11px] border border-line bg-bg-soft px-3.5 py-[11px] text-[13.5px] font-medium text-ink">
            {savedPosition}
          </p>
        ) : (
          <p className="text-[12.5px] text-ink-3">{t("documents.positionFallback")}</p>
        )}
      </Field>
    </Dialog>
  );
}

function SettlementDialog({
  employeeId,
  onClose,
  onGenerated,
  onError,
  t,
  locale,
}: {
  employeeId: string;
  onClose: () => void;
  onGenerated: () => void;
  onError: (e: unknown) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
}) {
  const [settlementId, setSettlementId] = React.useState("");
  const [error, setError] = React.useState<string | null>(null);

  const settlementsQuery = useQuery({
    queryKey: ["hrm", "payroll", "final-settlements", employeeId],
    queryFn: () =>
      apiFetch<FinalSettlementResponse[]>(
        `/api/hrm/payroll/final-settlements?employeeId=${encodeURIComponent(employeeId)}`,
      ),
  });

  const settlements = settlementsQuery.data ?? [];

  const generate = useMutation({
    mutationFn: (id: string) =>
      apiFetch<PayrollDocumentResponse>(
        `/api/hrm/payroll/documents/final-settlement?settlementId=${id}`,
        { method: "POST" },
      ),
    onSuccess: onGenerated,
    onError,
  });

  return (
    <Dialog
      open
      onClose={onClose}
      size="sm"
      title={t("documents.settlementDialogTitle")}
      subtitle={t("documents.settlementDialogSubtitle")}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={generate.isPending}>
            {t("documents.dialog.cancel")}
          </Button>
          <Button
            onClick={() => {
              if (!settlementId) {
                setError(t("documents.selectSettlement"));
                return;
              }
              generate.mutate(settlementId);
            }}
            disabled={generate.isPending || settlements.length === 0}
          >
            {generate.isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("documents.generating")}
              </>
            ) : (
              t("documents.generate")
            )}
          </Button>
        </>
      }
    >
      {settlementsQuery.isLoading ? (
        <div className="grid place-items-center py-6">
          <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
        </div>
      ) : settlements.length === 0 ? (
        <p className="text-[13px] text-ink-3">{t("documents.noSettlements")}</p>
      ) : (
        <Field label={t("documents.settlement")}>
          <select
            value={settlementId}
            onChange={(e) => setSettlementId(e.target.value)}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="">{t("documents.settlementPlaceholder")}</option>
            {settlements.map((s) => (
              <option key={s.id} value={s.id}>
                {(s.periode ? formatPeriodShort(s.periode, locale) : s.departureDate) + " · " + s.status}
              </option>
            ))}
          </select>
        </Field>
      )}
      {error && <p className="mt-2 text-[12px] text-danger-600">{error}</p>}
    </Dialog>
  );
}

function StatCard({
  icon: Icon,
  tone,
  label,
  value,
}: {
  icon: LucideIcon;
  tone: "orange" | "info" | "warning" | "success";
  label: string;
  value: string;
}) {
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between">
        <div className="min-w-0">
          <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">{label}</div>
          <div className={cn("font-display font-mono-tabular mt-1.5 text-[24px] font-extrabold tracking-tight text-ink")}>
            {value}
          </div>
        </div>
        <IconTile icon={Icon} tone={tone} size="sm" />
      </div>
    </Card>
  );
}
