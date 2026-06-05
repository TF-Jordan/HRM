"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Building2,
  ChevronDown,
  ChevronRight,
  Loader2,
  Percent,
  Plus,
  PowerOff,
  Table2,
  Trash2,
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
import { formatMoney } from "@/lib/format";
import { cn } from "@/lib/utils";
import type {
  CreateLookupTableRequest,
  CreateTaxBracketTableRequest,
  LookupTableResponse,
  TaxBracketTableResponse,
} from "@/server/ksm/modules/payroll";

const COUNTRIES = [
  { code: "CM", name: "Cameroun" },
  { code: "GA", name: "Gabon" },
  { code: "CI", name: "Côte d'Ivoire" },
  { code: "SN", name: "Sénégal" },
  { code: "TD", name: "Tchad" },
  { code: "CG", name: "Congo" },
] as const;

type DeactivateTarget = { kind: "bracket" | "lookup"; id: string; code: string; label: string };

export function TaxBrackets() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canManage = useCan("hrm:payroll:run");
  const qc = useQueryClient();

  const [country, setCountry] = React.useState("CM");
  const [creatingBracket, setCreatingBracket] = React.useState(false);
  const [creatingLookup, setCreatingLookup] = React.useState(false);
  const [toDeactivate, setToDeactivate] = React.useState<DeactivateTarget | null>(null);

  const bracketsQuery = useQuery({
    queryKey: ["hrm", "payroll", "tax-brackets", country],
    queryFn: () =>
      apiFetch<TaxBracketTableResponse[]>(
        `/api/hrm/payroll/tax-brackets?countryCode=${encodeURIComponent(country)}`,
      ),
  });
  const lookupsQuery = useQuery({
    queryKey: ["hrm", "payroll", "lookup-tables", country],
    queryFn: () =>
      apiFetch<LookupTableResponse[]>(
        `/api/hrm/payroll/lookup-tables?countryCode=${encodeURIComponent(country)}`,
      ),
  });

  const createBracket = useMutation({
    mutationFn: (body: CreateTaxBracketTableRequest) =>
      apiFetch<TaxBracketTableResponse>("/api/hrm/payroll/tax-brackets", { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("brackets.created"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "tax-brackets", country] });
      setCreatingBracket(false);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("brackets.createError")),
  });

  const createLookup = useMutation({
    mutationFn: (body: CreateLookupTableRequest) =>
      apiFetch<LookupTableResponse>("/api/hrm/payroll/lookup-tables", { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("brackets.created"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "lookup-tables", country] });
      setCreatingLookup(false);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("brackets.createError")),
  });

  const deactivate = useMutation({
    mutationFn: (target: DeactivateTarget) => {
      const base = target.kind === "bracket" ? "tax-brackets" : "lookup-tables";
      return apiFetch(`/api/hrm/payroll/${base}/${target.id}`, { method: "DELETE" });
    },
    onSuccess: (_data, target) => {
      toast.success(t("brackets.deactivated"));
      const key = target.kind === "bracket" ? "tax-brackets" : "lookup-tables";
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", key, country] });
      setToDeactivate(null);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("brackets.deactivateError")),
  });

  const isLoading = bracketsQuery.isLoading || lookupsQuery.isLoading;
  const error = bracketsQuery.error ?? lookupsQuery.error;

  return (
    <>
      <PageHeader
        ucBadge={t("brackets.uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }, { label: t("brackets.title") }]}
        title={t("brackets.title")}
        subtitle={t("brackets.subtitle")}
        actions={
          <div className="flex items-center gap-2 rounded-[11px] border border-line bg-white px-3 py-1.5 shadow-xs-brand">
            <Building2 className="h-4 w-4 text-ink-3" />
            <select
              value={country}
              onChange={(e) => setCountry(e.target.value)}
              className="bg-transparent text-[13px] font-semibold text-ink outline-none"
              aria-label={t("brackets.country")}
            >
              {COUNTRIES.map((c) => (
                <option key={c.code} value={c.code}>
                  {c.name} ({c.code})
                </option>
              ))}
            </select>
          </div>
        }
      />

      {isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {error instanceof BffApiError ? error.message : "—"}
        </div>
      ) : (
        <div className="flex flex-col gap-6">
          <BracketTablesCard
            tables={(bracketsQuery.data ?? []).slice().sort((a, b) => a.code.localeCompare(b.code))}
            canManage={canManage}
            locale={locale}
            onCreate={() => setCreatingBracket(true)}
            onDeactivate={(tbl) =>
              setToDeactivate({ kind: "bracket", id: tbl.id, code: tbl.code, label: tbl.label })
            }
            t={t}
          />
          <LookupTablesCard
            tables={(lookupsQuery.data ?? []).slice().sort((a, b) => a.code.localeCompare(b.code))}
            canManage={canManage}
            locale={locale}
            onCreate={() => setCreatingLookup(true)}
            onDeactivate={(tbl) =>
              setToDeactivate({ kind: "lookup", id: tbl.id, code: tbl.code, label: tbl.label })
            }
            t={t}
          />
        </div>
      )}

      {creatingBracket && (
        <BracketCreateDialog
          country={country}
          isSaving={createBracket.isPending}
          onClose={() => setCreatingBracket(false)}
          onSubmit={(body) => createBracket.mutate(body)}
          t={t}
        />
      )}
      {creatingLookup && (
        <LookupCreateDialog
          country={country}
          isSaving={createLookup.isPending}
          onClose={() => setCreatingLookup(false)}
          onSubmit={(body) => createLookup.mutate(body)}
          t={t}
        />
      )}
      {toDeactivate && (
        <Dialog
          open
          onClose={() => setToDeactivate(null)}
          size="sm"
          title={t("brackets.deactivateTitle")}
          subtitle={`${toDeactivate.code} · ${toDeactivate.label}`}
          footer={
            <>
              <Button variant="secondary" onClick={() => setToDeactivate(null)} disabled={deactivate.isPending}>
                {t("brackets.dialog.cancel")}
              </Button>
              <Button
                className="bg-danger-600 hover:bg-danger-600/90"
                onClick={() => deactivate.mutate(toDeactivate)}
                disabled={deactivate.isPending}
              >
                {deactivate.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    {t("brackets.deactivating")}
                  </>
                ) : (
                  t("brackets.deactivate")
                )}
              </Button>
            </>
          }
        >
          <p className="text-[13.5px] text-ink-2">{t("brackets.deactivateConfirm")}</p>
        </Dialog>
      )}
    </>
  );
}

/* ----------------------------- Bracket tables ----------------------------- */

function BracketTablesCard({
  tables,
  canManage,
  locale,
  onCreate,
  onDeactivate,
  t,
}: {
  tables: TaxBracketTableResponse[];
  canManage: boolean;
  locale: "fr" | "en";
  onCreate: () => void;
  onDeactivate: (t: TaxBracketTableResponse) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [expanded, setExpanded] = React.useState<string | null>(tables[0]?.id ?? null);

  return (
    <Card>
      <SectionHeader
        icon={Percent}
        tone="orange"
        title={t("brackets.progressiveTitle")}
        subtitle={t("brackets.progressiveSubtitle")}
        count={tables.length}
        canManage={canManage}
        onCreate={onCreate}
        createLabel={t("brackets.newScale")}
      />
      {tables.length === 0 ? (
        <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("brackets.emptyScales")}</div>
      ) : (
        <div className="divide-y divide-line-soft">
          {tables.map((tbl) => {
            const isOpen = expanded === tbl.id;
            return (
              <div key={tbl.id}>
                <button
                  type="button"
                  onClick={() => setExpanded(isOpen ? null : tbl.id)}
                  className="flex w-full items-center gap-3 px-6 py-3.5 text-left hover:bg-bg-soft"
                >
                  {isOpen ? (
                    <ChevronDown className="h-4 w-4 shrink-0 text-ink-3" />
                  ) : (
                    <ChevronRight className="h-4 w-4 shrink-0 text-ink-3" />
                  )}
                  <div className="min-w-0 flex-1">
                    <div className="font-mono-tabular text-[12px] font-semibold text-ink">{tbl.code}</div>
                    <div className="text-[12.5px] text-ink-2">{tbl.label}</div>
                  </div>
                  <span className="text-[11.5px] text-ink-3">
                    {t("brackets.bracketCount", { count: tbl.brackets.length })}
                  </span>
                  <span className="text-[11.5px] text-ink-3">
                    {tbl.effectiveFrom}
                    {tbl.effectiveTo ? ` → ${tbl.effectiveTo}` : ""}
                  </span>
                  {tbl.active ? (
                    <Badge tone="success" showDot={false}>
                      {t("brackets.active")}
                    </Badge>
                  ) : (
                    <Badge tone="gray" showDot={false}>
                      {t("brackets.inactive")}
                    </Badge>
                  )}
                  {canManage && tbl.active && (
                    <span
                      role="button"
                      tabIndex={0}
                      onClick={(e) => {
                        e.stopPropagation();
                        onDeactivate(tbl);
                      }}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") {
                          e.stopPropagation();
                          onDeactivate(tbl);
                        }
                      }}
                      className="grid h-7 w-7 place-items-center rounded-[8px] text-danger-600 hover:bg-danger-50"
                      title={t("brackets.deactivate")}
                    >
                      <PowerOff className="h-3.5 w-3.5" />
                    </span>
                  )}
                </button>
                {isOpen && (
                  <div className="bg-bg-soft/40 px-6 pb-4 pt-1">
                    <table className="w-full text-[12.5px]">
                      <thead>
                        <tr className="text-left text-[10.5px] uppercase tracking-[0.05em] text-ink-3">
                          <th className="py-2 pr-3">{t("brackets.cols.order")}</th>
                          <th className="py-2 pr-3">{t("brackets.cols.range")}</th>
                          <th className="py-2 pr-3 text-right">{t("brackets.cols.rate")}</th>
                        </tr>
                      </thead>
                      <tbody className="font-mono-tabular divide-y divide-line-soft">
                        {tbl.brackets
                          .slice()
                          .sort((a, b) => a.ordre - b.ordre)
                          .map((b) => (
                            <tr key={b.ordre}>
                              <td className="py-2 pr-3 text-ink-3">{b.ordre}</td>
                              <td className="py-2 pr-3 text-ink-2">
                                {formatRange(b.lowerBound, b.upperBound, locale, t)}
                              </td>
                              <td className="py-2 pr-3 text-right font-semibold text-ink">
                                {formatPct(b.rate, locale)}
                              </td>
                            </tr>
                          ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </Card>
  );
}

/* ----------------------------- Lookup tables ------------------------------ */

function LookupTablesCard({
  tables,
  canManage,
  locale,
  onCreate,
  onDeactivate,
  t,
}: {
  tables: LookupTableResponse[];
  canManage: boolean;
  locale: "fr" | "en";
  onCreate: () => void;
  onDeactivate: (t: LookupTableResponse) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [expanded, setExpanded] = React.useState<string | null>(tables[0]?.id ?? null);

  return (
    <Card>
      <SectionHeader
        icon={Table2}
        tone="violet"
        title={t("brackets.lookupTitle")}
        subtitle={t("brackets.lookupSubtitle")}
        count={tables.length}
        canManage={canManage}
        onCreate={onCreate}
        createLabel={t("brackets.newGrid")}
      />
      {tables.length === 0 ? (
        <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("brackets.emptyGrids")}</div>
      ) : (
        <div className="divide-y divide-line-soft">
          {tables.map((tbl) => {
            const isOpen = expanded === tbl.id;
            return (
              <div key={tbl.id}>
                <button
                  type="button"
                  onClick={() => setExpanded(isOpen ? null : tbl.id)}
                  className="flex w-full items-center gap-3 px-6 py-3.5 text-left hover:bg-bg-soft"
                >
                  {isOpen ? (
                    <ChevronDown className="h-4 w-4 shrink-0 text-ink-3" />
                  ) : (
                    <ChevronRight className="h-4 w-4 shrink-0 text-ink-3" />
                  )}
                  <div className="min-w-0 flex-1">
                    <div className="font-mono-tabular text-[12px] font-semibold text-ink">{tbl.code}</div>
                    <div className="text-[12.5px] text-ink-2">{tbl.label}</div>
                  </div>
                  <span className="text-[11.5px] text-ink-3">
                    {t("brackets.stepCount", { count: tbl.entries.length })}
                  </span>
                  <span className="text-[11.5px] text-ink-3">
                    {tbl.effectiveFrom}
                    {tbl.effectiveTo ? ` → ${tbl.effectiveTo}` : ""}
                  </span>
                  {tbl.active ? (
                    <Badge tone="success" showDot={false}>
                      {t("brackets.active")}
                    </Badge>
                  ) : (
                    <Badge tone="gray" showDot={false}>
                      {t("brackets.inactive")}
                    </Badge>
                  )}
                  {canManage && tbl.active && (
                    <span
                      role="button"
                      tabIndex={0}
                      onClick={(e) => {
                        e.stopPropagation();
                        onDeactivate(tbl);
                      }}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") {
                          e.stopPropagation();
                          onDeactivate(tbl);
                        }
                      }}
                      className="grid h-7 w-7 place-items-center rounded-[8px] text-danger-600 hover:bg-danger-50"
                      title={t("brackets.deactivate")}
                    >
                      <PowerOff className="h-3.5 w-3.5" />
                    </span>
                  )}
                </button>
                {isOpen && (
                  <div className="bg-bg-soft/40 px-6 pb-4 pt-1">
                    <table className="w-full text-[12.5px]">
                      <thead>
                        <tr className="text-left text-[10.5px] uppercase tracking-[0.05em] text-ink-3">
                          <th className="py-2 pr-3">{t("brackets.cols.order")}</th>
                          <th className="py-2 pr-3">{t("brackets.cols.range")}</th>
                          <th className="py-2 pr-3 text-right">{t("brackets.cols.amount")}</th>
                        </tr>
                      </thead>
                      <tbody className="font-mono-tabular divide-y divide-line-soft">
                        {tbl.entries
                          .slice()
                          .sort((a, b) => a.ordre - b.ordre)
                          .map((e) => (
                            <tr key={e.ordre}>
                              <td className="py-2 pr-3 text-ink-3">{e.ordre}</td>
                              <td className="py-2 pr-3 text-ink-2">
                                {formatRange(e.lowerBound, e.upperBound, locale, t)}
                              </td>
                              <td className="py-2 pr-3 text-right font-semibold text-ink">
                                {formatMoney(Number(e.amount), { locale, withCurrency: false })}
                              </td>
                            </tr>
                          ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </Card>
  );
}

/* ------------------------------- Create dialogs --------------------------- */

type BracketRow = { lowerBound: string; upperBound: string; rate: string };
type EntryRow = { lowerBound: string; upperBound: string; amount: string };

function BracketCreateDialog({
  country,
  isSaving,
  onClose,
  onSubmit,
  t,
}: {
  country: string;
  isSaving: boolean;
  onClose: () => void;
  onSubmit: (body: CreateTaxBracketTableRequest) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [code, setCode] = React.useState("");
  const [label, setLabel] = React.useState("");
  const [effectiveFrom, setEffectiveFrom] = React.useState(`${new Date().getFullYear()}-01-01`);
  const [effectiveTo, setEffectiveTo] = React.useState("");
  const [rows, setRows] = React.useState<BracketRow[]>([{ lowerBound: "0", upperBound: "", rate: "" }]);
  const [error, setError] = React.useState<string | null>(null);

  function submit() {
    if (!code.trim() || !label.trim() || !effectiveFrom) {
      setError(t("brackets.validation.header"));
      return;
    }
    const valid = rows.filter((r) => r.rate.trim() !== "");
    if (valid.length === 0) {
      setError(t("brackets.validation.rows"));
      return;
    }
    const brackets = valid.map((r, i) => ({
      ordre: i + 1,
      lowerBound: Number(r.lowerBound) || 0,
      upperBound: r.upperBound.trim() === "" ? null : Number(r.upperBound),
      rate: Number(r.rate) / 100,
    }));
    onSubmit({
      code: code.trim().toUpperCase(),
      label: label.trim(),
      countryCode: country,
      effectiveFrom,
      effectiveTo: effectiveTo || null,
      brackets,
    });
  }

  return (
    <Dialog
      open
      onClose={onClose}
      size="lg"
      title={t("brackets.dialog.bracketTitle")}
      subtitle={t("brackets.dialog.subtitle", { country })}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSaving}>
            {t("brackets.dialog.cancel")}
          </Button>
          <Button onClick={submit} disabled={isSaving}>
            {isSaving ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("brackets.dialog.saving")}
              </>
            ) : (
              t("brackets.dialog.save")
            )}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label={t("brackets.fields.code")} hint={t("brackets.hints.code")}>
          <Input value={code} onChange={(e) => setCode(e.target.value)} placeholder="IRPP_CM_2026" />
        </Field>
        <Field label={t("brackets.fields.label")}>
          <Input value={label} onChange={(e) => setLabel(e.target.value)} placeholder="Barème IRPP Cameroun" />
        </Field>
        <Field label={t("brackets.fields.effectiveFrom")}>
          <Input type="date" value={effectiveFrom} onChange={(e) => setEffectiveFrom(e.target.value)} />
        </Field>
        <Field label={t("brackets.fields.effectiveTo")} hint={t("brackets.hints.effectiveTo")}>
          <Input type="date" value={effectiveTo} onChange={(e) => setEffectiveTo(e.target.value)} />
        </Field>
      </div>

      <RowsEditor
        title={t("brackets.dialog.bracketsTitle")}
        columns={[t("brackets.cols.lower"), t("brackets.cols.upper"), t("brackets.fields.rate")]}
        rows={rows}
        renderRow={(row, idx) => (
          <>
            <NumInput value={row.lowerBound} onChange={(v) => updateRow(setRows, idx, "lowerBound", v)} />
            <NumInput
              value={row.upperBound}
              placeholder={t("brackets.openTop")}
              onChange={(v) => updateRow(setRows, idx, "upperBound", v)}
            />
            <NumInput value={row.rate} step="0.01" placeholder="10" onChange={(v) => updateRow(setRows, idx, "rate", v)} />
          </>
        )}
        onAdd={() => setRows((p) => [...p, { lowerBound: "", upperBound: "", rate: "" }])}
        onRemove={(idx) => setRows((p) => p.filter((_, i) => i !== idx))}
        addLabel={t("brackets.addRow")}
      />
      <p className="mt-2 text-[11.5px] text-ink-4">{t("brackets.hints.rate")}</p>
      {error && <p className="mt-2 text-[12px] text-danger-600">{error}</p>}
    </Dialog>
  );
}

function LookupCreateDialog({
  country,
  isSaving,
  onClose,
  onSubmit,
  t,
}: {
  country: string;
  isSaving: boolean;
  onClose: () => void;
  onSubmit: (body: CreateLookupTableRequest) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [code, setCode] = React.useState("");
  const [label, setLabel] = React.useState("");
  const [effectiveFrom, setEffectiveFrom] = React.useState(`${new Date().getFullYear()}-01-01`);
  const [effectiveTo, setEffectiveTo] = React.useState("");
  const [rows, setRows] = React.useState<EntryRow[]>([{ lowerBound: "0", upperBound: "", amount: "" }]);
  const [error, setError] = React.useState<string | null>(null);

  function submit() {
    if (!code.trim() || !label.trim() || !effectiveFrom) {
      setError(t("brackets.validation.header"));
      return;
    }
    const valid = rows.filter((r) => r.amount.trim() !== "");
    if (valid.length === 0) {
      setError(t("brackets.validation.rows"));
      return;
    }
    const entries = valid.map((r, i) => ({
      ordre: i + 1,
      lowerBound: Number(r.lowerBound) || 0,
      upperBound: r.upperBound.trim() === "" ? null : Number(r.upperBound),
      amount: Number(r.amount) || 0,
    }));
    onSubmit({
      code: code.trim().toUpperCase(),
      label: label.trim(),
      countryCode: country,
      effectiveFrom,
      effectiveTo: effectiveTo || null,
      entries,
    });
  }

  return (
    <Dialog
      open
      onClose={onClose}
      size="lg"
      title={t("brackets.dialog.lookupTitle")}
      subtitle={t("brackets.dialog.subtitle", { country })}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSaving}>
            {t("brackets.dialog.cancel")}
          </Button>
          <Button onClick={submit} disabled={isSaving}>
            {isSaving ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("brackets.dialog.saving")}
              </>
            ) : (
              t("brackets.dialog.save")
            )}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label={t("brackets.fields.code")} hint={t("brackets.hints.code")}>
          <Input value={code} onChange={(e) => setCode(e.target.value)} placeholder="RAV_CM" />
        </Field>
        <Field label={t("brackets.fields.label")}>
          <Input value={label} onChange={(e) => setLabel(e.target.value)} placeholder="Redevance Audio-Visuelle" />
        </Field>
        <Field label={t("brackets.fields.effectiveFrom")}>
          <Input type="date" value={effectiveFrom} onChange={(e) => setEffectiveFrom(e.target.value)} />
        </Field>
        <Field label={t("brackets.fields.effectiveTo")} hint={t("brackets.hints.effectiveTo")}>
          <Input type="date" value={effectiveTo} onChange={(e) => setEffectiveTo(e.target.value)} />
        </Field>
      </div>

      <RowsEditor
        title={t("brackets.dialog.stepsTitle")}
        columns={[t("brackets.cols.lower"), t("brackets.cols.upper"), t("brackets.cols.amount")]}
        rows={rows}
        renderRow={(row, idx) => (
          <>
            <NumInput value={row.lowerBound} onChange={(v) => updateRow(setRows, idx, "lowerBound", v)} />
            <NumInput
              value={row.upperBound}
              placeholder={t("brackets.openTop")}
              onChange={(v) => updateRow(setRows, idx, "upperBound", v)}
            />
            <NumInput value={row.amount} placeholder="0" onChange={(v) => updateRow(setRows, idx, "amount", v)} />
          </>
        )}
        onAdd={() => setRows((p) => [...p, { lowerBound: "", upperBound: "", amount: "" }])}
        onRemove={(idx) => setRows((p) => p.filter((_, i) => i !== idx))}
        addLabel={t("brackets.addRow")}
      />
      {error && <p className="mt-2 text-[12px] text-danger-600">{error}</p>}
    </Dialog>
  );
}

/* --------------------------------- Shared --------------------------------- */

function RowsEditor<Row>({
  title,
  columns,
  rows,
  renderRow,
  onAdd,
  onRemove,
  addLabel,
}: {
  title: string;
  columns: string[];
  rows: Row[];
  renderRow: (row: Row, idx: number) => React.ReactNode;
  onAdd: () => void;
  onRemove: (idx: number) => void;
  addLabel: string;
}) {
  return (
    <div className="mt-5">
      <div className="mb-2 flex items-center justify-between">
        <span className="text-[12px] font-semibold uppercase tracking-[0.05em] text-ink-3">{title}</span>
        <Button variant="secondary" size="sm" onClick={onAdd} type="button">
          <Plus className="h-3.5 w-3.5" />
          {addLabel}
        </Button>
      </div>
      <div className="grid grid-cols-[repeat(3,1fr)_auto] items-center gap-2 text-[10.5px] uppercase tracking-[0.05em] text-ink-4">
        {columns.map((c) => (
          <span key={c} className="px-1">
            {c}
          </span>
        ))}
        <span className="w-8" />
      </div>
      <div className="mt-1.5 flex flex-col gap-2">
        {rows.map((row, idx) => (
          <div key={idx} className="grid grid-cols-[repeat(3,1fr)_auto] items-center gap-2">
            {renderRow(row, idx)}
            <button
              type="button"
              onClick={() => onRemove(idx)}
              disabled={rows.length === 1}
              className="grid h-9 w-8 place-items-center rounded-[8px] text-ink-3 hover:bg-danger-50 hover:text-danger-600 disabled:opacity-30"
              aria-label="remove"
            >
              <Trash2 className="h-3.5 w-3.5" />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

function NumInput({
  value,
  onChange,
  placeholder,
  step,
}: {
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
  step?: string;
}) {
  return (
    <Input
      type="number"
      step={step}
      value={value}
      placeholder={placeholder}
      onChange={(e) => onChange(e.target.value)}
      className="font-mono-tabular px-2.5 py-2 text-[12.5px]"
    />
  );
}

function SectionHeader({
  icon: Icon,
  tone,
  title,
  subtitle,
  count,
  canManage,
  onCreate,
  createLabel,
}: {
  icon: typeof Percent;
  tone: "orange" | "violet";
  title: string;
  subtitle: string;
  count: number;
  canManage: boolean;
  onCreate: () => void;
  createLabel: string;
}) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line-soft px-6 py-4">
      <div className="flex items-center gap-3">
        <IconTile icon={Icon} tone={tone} size="md" />
        <div>
          <h3 className="text-[15px] font-bold tracking-tight text-ink">
            {title} <span className="text-ink-3">· {count}</span>
          </h3>
          <p className="text-[12px] text-ink-3">{subtitle}</p>
        </div>
      </div>
      {canManage && (
        <Button onClick={onCreate}>
          <Plus className="h-4 w-4" />
          {createLabel}
        </Button>
      )}
    </div>
  );
}

/* ------------------------------- Formatters ------------------------------- */

function updateRow<Row, K extends keyof Row>(
  setRows: React.Dispatch<React.SetStateAction<Row[]>>,
  idx: number,
  key: K,
  value: Row[K],
) {
  setRows((prev) => prev.map((r, i) => (i === idx ? { ...r, [key]: value } : r)));
}

function formatPct(rate: number | string, locale: "fr" | "en"): string {
  return `${(Number(rate) * 100).toLocaleString(locale === "fr" ? "fr-FR" : "en-US", {
    maximumFractionDigits: 2,
  })}%`;
}

function formatRange(
  lower: number | string,
  upper: number | string | null,
  locale: "fr" | "en",
  t: ReturnType<typeof useTranslations<"payroll">>,
): string {
  const lo = formatMoney(Number(lower), { locale, withCurrency: false });
  if (upper == null) return t("brackets.rangeFrom", { from: lo });
  const hi = formatMoney(Number(upper), { locale, withCurrency: false });
  return `${lo} – ${hi}`;
}
