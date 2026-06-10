"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  CheckCircle2,
  Database,
  Download,
  FileUp,
  Loader2,
  Plus,
  Users,
  XCircle,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { toast } from "sonner";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { IconTile } from "@/components/ui/icon-tile";
import { Field, Input } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import type {
  CsvImportReport,
  PayrollDataSource,
  PayrollEmployeeResponse,
} from "@/server/ksm/modules/payroll-employees";

export function PayrollEmployees() {
  const t = useTranslations("payroll.localEmployees");
  const locale = useLocale() as "fr" | "en";
  const canManage = useCan("hrm:payroll:run");
  const qc = useQueryClient();

  const [importOpen, setImportOpen] = React.useState(false);
  const [addOpen, setAddOpen] = React.useState(false);

  const listQuery = useQuery({
    queryKey: ["hrm", "payroll", "local-employees"],
    queryFn: () => apiFetch<PayrollEmployeeResponse[]>("/api/hrm/payroll/employees"),
  });
  const sourceQuery = useQuery({
    queryKey: ["hrm", "payroll", "data-source"],
    queryFn: () => apiFetch<{ source: PayrollDataSource }>("/api/hrm/payroll/employees/data-source"),
  });

  const invalidate = React.useCallback(() => {
    qc.invalidateQueries({ queryKey: ["hrm", "payroll", "local-employees"] });
    qc.invalidateQueries({ queryKey: ["hrm", "payroll", "data-source"] });
  }, [qc]);

  const rows = React.useMemo(
    () =>
      (listQuery.data ?? [])
        .slice()
        .sort((a, b) => Number(b.active) - Number(a.active) || a.matricule.localeCompare(b.matricule)),
    [listQuery.data],
  );
  const activeCount = rows.filter((r) => r.active).length;
  const source = sourceQuery.data?.source ?? "HRM";

  return (
    <>
      <PageHeader
        ucBadge={t("uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canManage ? (
            <>
              <TemplateButton t={t} />
              <Button variant="secondary" onClick={() => setAddOpen(true)}>
                <Plus className="h-4 w-4" /> {t("actions.add")}
              </Button>
              <Button onClick={() => setImportOpen(true)}>
                <FileUp className="h-4 w-4" /> {t("actions.import")}
              </Button>
            </>
          ) : undefined
        }
      />

      <div className="flex flex-col gap-5">
        {/* Data source banner */}
        <Card>
          <div className="flex flex-wrap items-center gap-4 px-6 py-4">
            <IconTile icon={Database} tone={source === "LOCAL" ? "orange" : "info"} size="sm" />
            <div className="min-w-0 flex-1">
              <div className="text-[13px] font-bold text-ink">
                {t(`source.${source}` as "source.LOCAL")}
              </div>
              <p className="text-[12px] text-ink-3">
                {t(`source.${source}Hint` as "source.LOCALHint")}
              </p>
            </div>
            <div className="flex items-center gap-4">
              <Stat label={t("stats.total")} value={String(rows.length)} />
              <Stat label={t("stats.active")} value={String(activeCount)} />
            </div>
          </div>
        </Card>

        {/* Table */}
        <Card>
          <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
            <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("table.title")}</h3>
            <div className="text-[12px] text-ink-3">{rows.length > 0 ? rows.length : ""}</div>
          </div>
          {listQuery.isLoading ? (
            <div className="grid place-items-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
            </div>
          ) : rows.length === 0 ? (
            <div className="flex flex-col items-center gap-3 px-6 py-12 text-center">
              <IconTile icon={Users} tone="orange" size="lg" />
              <div className="text-[14px] font-bold text-ink">{t("table.emptyTitle")}</div>
              <p className="max-w-md text-[13px] text-ink-3">{t("table.emptyHint")}</p>
              {canManage && (
                <Button onClick={() => setImportOpen(true)}>
                  <FileUp className="h-4 w-4" /> {t("actions.import")}
                </Button>
              )}
            </div>
          ) : (
            <EmployeesTable rows={rows} canManage={canManage} t={t} locale={locale} onChanged={invalidate} />
          )}
        </Card>
      </div>

      <ImportDialog
        open={importOpen}
        onClose={() => setImportOpen(false)}
        onImported={invalidate}
        t={t}
      />
      <AddDialog open={addOpen} onClose={() => setAddOpen(false)} onCreated={invalidate} t={t} />
    </>
  );
}

type T = ReturnType<typeof useTranslations<"payroll.localEmployees">>;

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="text-right">
      <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">{label}</div>
      <div className="font-mono-tabular text-[17px] font-bold text-ink">{value}</div>
    </div>
  );
}

function EmployeesTable({
  rows,
  canManage,
  t,
  locale,
  onChanged,
}: {
  rows: PayrollEmployeeResponse[];
  canManage: boolean;
  t: T;
  locale: "fr" | "en";
  onChanged: () => void;
}) {
  const deactivate = useMutation({
    mutationFn: (id: string) =>
      apiFetch<PayrollEmployeeResponse>(`/api/hrm/payroll/employees/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      toast.success(t("toasts.deactivated"));
      onChanged();
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : "—");
    },
  });

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-[13px]">
        <thead>
          <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
            <th className="px-6 py-3">{t("table.employee")}</th>
            <th className="px-4 py-3">{t("table.email")}</th>
            <th className="px-4 py-3">{t("table.department")}</th>
            <th className="px-4 py-3 text-right">{t("table.baseSalary")}</th>
            <th className="px-4 py-3">{t("table.channel")}</th>
            <th className="px-4 py-3">{t("table.status")}</th>
            {canManage && <th className="px-6 py-3" />}
          </tr>
        </thead>
        <tbody className="divide-y divide-line-soft">
          {rows.map((e) => (
            <tr key={e.id} className="hover:bg-bg-soft">
              <td className="px-6 py-3">
                <div className="flex items-center gap-2.5">
                  <Avatar size="md" name={e.displayName} />
                  <div>
                    <div className="text-[13px] font-semibold text-ink">{e.displayName}</div>
                    <div className="font-mono-tabular text-[11px] text-ink-3">{e.matricule}</div>
                  </div>
                </div>
              </td>
              <td className="px-4 py-3 text-[12px] text-ink-2">{e.email ?? "—"}</td>
              <td className="px-4 py-3 text-[12px] text-ink-3">{e.departmentCode ?? "—"}</td>
              <td className="font-mono-tabular px-4 py-3 text-right font-semibold text-ink">
                {formatMoney(Number(e.baseSalary ?? 0), { locale, withCurrency: false })}
              </td>
              <td className="px-4 py-3 text-[12px] text-ink-3">{e.paymentChannel}</td>
              <td className="px-4 py-3">
                <Badge tone={e.active ? "success" : "gray"} showDot={false}>
                  {e.active ? t("table.active") : t("table.inactive")}
                </Badge>
              </td>
              {canManage && (
                <td className="px-6 py-3 text-right">
                  {e.active && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => deactivate.mutate(e.id)}
                      disabled={deactivate.isPending}
                      title={t("actions.deactivate")}
                    >
                      <XCircle className="h-3.5 w-3.5 text-danger-600" />
                    </Button>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function TemplateButton({ t }: { t: T }) {
  const [busy, setBusy] = React.useState(false);
  async function download() {
    setBusy(true);
    try {
      const data = await apiFetch<{ csv: string }>("/api/hrm/payroll/employees/template");
      const blob = new Blob([data.csv], { type: "text/csv;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "modele-employes-paie.csv";
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      toast.error(t("toasts.templateFailed"));
    } finally {
      setBusy(false);
    }
  }
  return (
    <Button variant="secondary" onClick={download} disabled={busy}>
      {busy ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
      {t("actions.template")}
    </Button>
  );
}

function ImportDialog({
  open,
  onClose,
  onImported,
  t,
}: {
  open: boolean;
  onClose: () => void;
  onImported: () => void;
  t: T;
}) {
  const [fileName, setFileName] = React.useState<string | null>(null);
  const [csv, setCsv] = React.useState<string>("");
  const [report, setReport] = React.useState<CsvImportReport | null>(null);

  const importMutation = useMutation({
    mutationFn: () =>
      apiFetch<CsvImportReport>("/api/hrm/payroll/employees/import", {
        method: "POST",
        body: { csv },
      }),
    onSuccess: (data) => {
      setReport(data);
      onImported();
      if (data.errors.length === 0) {
        toast.success(t("toasts.imported", { created: data.created, updated: data.updated }));
      } else {
        toast.warning(t("toasts.importedPartial", { errors: data.errors.length }));
      }
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : t("toasts.importFailed"));
    },
  });

  function reset() {
    setFileName(null);
    setCsv("");
    setReport(null);
  }

  async function onFile(file: File | null) {
    if (!file) return;
    setFileName(file.name);
    setCsv(await file.text());
    setReport(null);
  }

  return (
    <Dialog
      open={open}
      onClose={() => {
        if (!importMutation.isPending) {
          reset();
          onClose();
        }
      }}
      size="md"
      title={t("import.title")}
      subtitle={t("import.subtitle")}
      footer={
        <>
          <Button
            variant="secondary"
            onClick={() => {
              reset();
              onClose();
            }}
            disabled={importMutation.isPending}
          >
            {t("import.close")}
          </Button>
          <Button
            onClick={() => importMutation.mutate()}
            disabled={!csv || importMutation.isPending}
          >
            {importMutation.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <FileUp className="h-4 w-4" />
            )}
            {t("import.confirm")}
          </Button>
        </>
      }
    >
      <label
        className="grid cursor-pointer place-items-center rounded-[14px] border-2 border-dashed border-line bg-bg-soft/60 px-6 py-8 text-center hover:border-orange-300"
        htmlFor="payroll-csv-input"
      >
        <FileUp className="h-7 w-7 text-orange-500" />
        <div className="mt-2 text-[13px] font-semibold text-ink">
          {fileName ?? t("import.dropHint")}
        </div>
        <div className="mt-1 text-[11.5px] text-ink-3">{t("import.formatHint")}</div>
        <input
          id="payroll-csv-input"
          type="file"
          accept=".csv,text/csv"
          className="hidden"
          onChange={(e) => onFile(e.target.files?.[0] ?? null)}
        />
      </label>

      {csv && !report && (
        <p className="mt-3 text-[12px] text-ink-3">
          {t("import.linesDetected", { count: Math.max(0, csv.split("\n").filter(Boolean).length - 1) })}
        </p>
      )}

      {report && (
        <div className="mt-4 rounded-[12px] border border-line bg-white p-4">
          <div className="flex items-center gap-2 text-[13px] font-bold text-ink">
            {report.errors.length === 0 ? (
              <CheckCircle2 className="h-4 w-4 text-success-600" />
            ) : (
              <XCircle className="h-4 w-4 text-warning-600" />
            )}
            {t("import.reportTitle")}
          </div>
          <div className="mt-2 grid grid-cols-3 gap-2 text-center text-[12px]">
            <ReportStat label={t("import.created")} value={report.created} />
            <ReportStat label={t("import.updated")} value={report.updated} />
            <ReportStat label={t("import.failed")} value={report.errors.length} />
          </div>
          {report.errors.length > 0 && (
            <ul className="mt-3 max-h-40 space-y-1 overflow-y-auto text-[12px] text-danger-600">
              {report.errors.map((err, i) => (
                <li key={i}>
                  {err.line > 0 ? `L${err.line} · ` : ""}
                  {err.matricule ? `${err.matricule} — ` : ""}
                  {err.message}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </Dialog>
  );
}

function ReportStat({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-[10px] bg-bg-soft px-2 py-2">
      <div className="font-mono-tabular text-[16px] font-bold text-ink">{value}</div>
      <div className="text-[10.5px] uppercase tracking-[0.05em] text-ink-3">{label}</div>
    </div>
  );
}

function AddDialog({
  open,
  onClose,
  onCreated,
  t,
}: {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
  t: T;
}) {
  const [form, setForm] = React.useState({
    matricule: "",
    displayName: "",
    email: "",
    hireDate: "",
    baseSalary: "",
    departmentCode: "",
  });

  const create = useMutation({
    mutationFn: () =>
      apiFetch("/api/hrm/payroll/employees", {
        method: "POST",
        body: {
          matricule: form.matricule,
          displayName: form.displayName,
          email: form.email || null,
          hireDate: form.hireDate,
          baseSalary: Number(form.baseSalary),
          departmentCode: form.departmentCode || null,
        },
      }),
    onSuccess: () => {
      toast.success(t("toasts.created"));
      setForm({ matricule: "", displayName: "", email: "", hireDate: "", baseSalary: "", departmentCode: "" });
      onCreated();
      onClose();
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : "—");
    },
  });

  const valid =
    form.matricule.trim() &&
    form.displayName.trim() &&
    form.hireDate &&
    Number(form.baseSalary) > 0;

  function set<K extends keyof typeof form>(key: K, value: string) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  return (
    <Dialog
      open={open}
      onClose={() => {
        if (!create.isPending) onClose();
      }}
      size="md"
      title={t("add.title")}
      subtitle={t("add.subtitle")}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={create.isPending}>
            {t("add.cancel")}
          </Button>
          <Button onClick={() => create.mutate()} disabled={!valid || create.isPending}>
            {create.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
            {t("add.confirm")}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label={t("add.matricule")}>
          <Input value={form.matricule} onChange={(e) => set("matricule", e.target.value)} />
        </Field>
        <Field label={t("add.displayName")}>
          <Input value={form.displayName} onChange={(e) => set("displayName", e.target.value)} />
        </Field>
        <Field label={t("add.email")}>
          <Input type="email" value={form.email} onChange={(e) => set("email", e.target.value)} />
        </Field>
        <Field label={t("add.hireDate")}>
          <Input type="date" value={form.hireDate} onChange={(e) => set("hireDate", e.target.value)} />
        </Field>
        <Field label={t("add.baseSalary")}>
          <Input
            type="number"
            min={0}
            value={form.baseSalary}
            onChange={(e) => set("baseSalary", e.target.value)}
          />
        </Field>
        <Field label={t("add.department")}>
          <Input value={form.departmentCode} onChange={(e) => set("departmentCode", e.target.value)} />
        </Field>
      </div>
    </Dialog>
  );
}
