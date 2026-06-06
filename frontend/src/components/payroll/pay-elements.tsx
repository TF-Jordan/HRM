"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowDownCircle,
  ArrowUpCircle,
  Building2,
  Info,
  Loader2,
  type LucideIcon,
  Plus,
  Power,
  PowerOff,
  Search,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { toast } from "sonner";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Chip } from "@/components/ui/chip";
import { Dialog } from "@/components/ui/dialog";
import { IconTile } from "@/components/ui/icon-tile";
import { Field, Input } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import { cn } from "@/lib/utils";
import type {
  CalculationMethod,
  CreatePayElementRequest,
  PayElementCategory,
  PayElementResponse,
} from "@/server/ksm/modules/payroll";

/** Jurisdictions known to the engine (catalogue is scoped per countryCode). CM is seeded. */
const COUNTRIES = [
  { code: "CM", name: "Cameroun" },
  { code: "GA", name: "Gabon" },
  { code: "CI", name: "Côte d'Ivoire" },
  { code: "SN", name: "Sénégal" },
  { code: "TD", name: "Tchad" },
  { code: "CG", name: "Congo" },
] as const;

const CATEGORIES: PayElementCategory[] = ["EARNING", "DEDUCTION", "EMPLOYER_CHARGE", "INFORMATIONAL"];
const METHODS: CalculationMethod[] = ["RATE", "BRACKET", "FLAT", "LOOKUP_TABLE", "FORMULA"];
const BASE_REFERENCES = ["GROSS", "BASE_SALARY", "BENEFITS_IN_KIND", "TAXABLE_NET", "IRPP"];

function categoryTone(c: PayElementCategory): "success" | "warning" | "info" | "gray" {
  switch (c) {
    case "EARNING":
      return "success";
    case "DEDUCTION":
      return "warning";
    case "EMPLOYER_CHARGE":
      return "info";
    default:
      return "gray";
  }
}

export function PayElements() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canManage = useCan("hrm:payroll:run");
  const queryClient = useQueryClient();

  const [country, setCountry] = React.useState<string>("CM");
  const [category, setCategory] = React.useState<PayElementCategory | "ALL">("ALL");
  const [search, setSearch] = React.useState("");
  const [showInactive, setShowInactive] = React.useState(true);
  const [creating, setCreating] = React.useState(false);
  const [toDeactivate, setToDeactivate] = React.useState<PayElementResponse | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "payroll", "pay-elements", country],
    queryFn: () =>
      apiFetch<PayElementResponse[]>(
        `/api/hrm/payroll/pay-elements?countryCode=${encodeURIComponent(country)}`,
      ),
  });

  const all = React.useMemo(
    () => (query.data ?? []).slice().sort((a, b) => a.displayOrder - b.displayOrder),
    [query.data],
  );

  const counts = React.useMemo(() => {
    const active = all.filter((e) => e.active);
    return {
      total: active.length,
      EARNING: active.filter((e) => e.category === "EARNING").length,
      DEDUCTION: active.filter((e) => e.category === "DEDUCTION").length,
      EMPLOYER_CHARGE: active.filter((e) => e.category === "EMPLOYER_CHARGE").length,
      INFORMATIONAL: active.filter((e) => e.category === "INFORMATIONAL").length,
    };
  }, [all]);

  const rows = React.useMemo(() => {
    const q = search.trim().toLowerCase();
    return all.filter((e) => {
      if (!showInactive && !e.active) return false;
      if (category !== "ALL" && e.category !== category) return false;
      if (q && !e.code.toLowerCase().includes(q) && !e.label.toLowerCase().includes(q)) return false;
      return true;
    });
  }, [all, category, search, showInactive]);

  const nextDisplayOrder = React.useMemo(
    () => (all.length ? Math.max(...all.map((e) => e.displayOrder)) + 10 : 10),
    [all],
  );

  const create = useMutation({
    mutationFn: (body: CreatePayElementRequest) =>
      apiFetch<PayElementResponse>("/api/hrm/payroll/pay-elements", { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("elements.created"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "payroll", "pay-elements", country] });
      setCreating(false);
    },
    onError: (err) =>
      toast.error(err instanceof BffApiError ? err.message : t("elements.createError")),
  });

  const deactivate = useMutation({
    mutationFn: (id: string) =>
      apiFetch<PayElementResponse>(`/api/hrm/payroll/pay-elements/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      toast.success(t("elements.deactivated"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "payroll", "pay-elements", country] });
      setToDeactivate(null);
    },
    onError: (err) =>
      toast.error(err instanceof BffApiError ? err.message : t("elements.deactivateError")),
  });

  const activate = useMutation({
    mutationFn: (id: string) =>
      apiFetch<PayElementResponse>(`/api/hrm/payroll/pay-elements/${id}/activate`, { method: "PUT" }),
    onSuccess: () => {
      toast.success(t("elements.activated"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "payroll", "pay-elements", country] });
    },
    onError: (err) =>
      toast.error(err instanceof BffApiError ? err.message : t("elements.activateError")),
  });

  return (
    <>
      <PageHeader
        ucBadge={t("elements.uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }, { label: t("elements.title") }]}
        title={t("elements.title")}
        subtitle={t("elements.subtitle")}
        actions={
          <div className="flex items-center gap-2">
            <div className="flex items-center gap-2 rounded-[11px] border border-line bg-white px-3 py-1.5 shadow-xs-brand">
              <Building2 className="h-4 w-4 text-ink-3" />
              <select
                value={country}
                onChange={(e) => setCountry(e.target.value)}
                className="bg-transparent text-[13px] font-semibold text-ink outline-none"
                aria-label={t("elements.country")}
              >
                {COUNTRIES.map((c) => (
                  <option key={c.code} value={c.code}>
                    {c.name} ({c.code})
                  </option>
                ))}
              </select>
            </div>
            {canManage && (
              <Button onClick={() => setCreating(true)}>
                <Plus className="h-4 w-4" />
                {t("elements.new")}
              </Button>
            )}
          </div>
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : (
        <div className="flex flex-col gap-5">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard icon={ArrowUpCircle} tone="success" label={t("category.EARNING")} value={String(counts.EARNING)} sub={t("elements.activeRubrics")} />
            <StatCard icon={ArrowDownCircle} tone="warning" label={t("category.DEDUCTION")} value={String(counts.DEDUCTION)} sub={t("elements.activeRubrics")} />
            <StatCard icon={Building2} tone="info" label={t("category.EMPLOYER_CHARGE")} value={String(counts.EMPLOYER_CHARGE)} sub={t("elements.activeRubrics")} />
            <StatCard icon={Info} tone="violet" label={t("category.INFORMATIONAL")} value={String(counts.INFORMATIONAL)} sub={t("elements.activeRubrics")} />
          </div>

          <Card>
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line-soft px-6 py-4">
              <div className="flex flex-wrap items-center gap-2">
                <Chip active={category === "ALL"} tone="orange" onClick={() => setCategory("ALL")}>
                  {t("elements.filterAll")} ({counts.total})
                </Chip>
                {CATEGORIES.map((c) => (
                  <Chip key={c} active={category === c} onClick={() => setCategory(c)}>
                    {t(`category.${c}`)} ({counts[c]})
                  </Chip>
                ))}
              </div>
              <div className="flex items-center gap-3">
                <label className="flex items-center gap-1.5 text-[12.5px] text-ink-2">
                  <input
                    type="checkbox"
                    checked={showInactive}
                    onChange={(e) => setShowInactive(e.target.checked)}
                    className="h-4 w-4 accent-orange-500"
                  />
                  {t("elements.showInactive")}
                </label>
                <div className="flex items-center gap-2 rounded-[11px] border border-line bg-white px-3 py-1.5 shadow-xs-brand">
                  <Search className="h-4 w-4 text-ink-3" />
                  <input
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder={t("elements.searchPlaceholder")}
                    className="w-40 bg-transparent text-[13px] text-ink outline-none placeholder:text-ink-4"
                  />
                </div>
              </div>
            </div>

            {rows.length === 0 ? (
              <div className="px-6 py-12 text-center text-[13px] text-ink-3">
                {all.length === 0 ? t("elements.emptyCountry") : t("elements.emptyFilter")}
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-[13px]">
                  <thead>
                    <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                      <th className="px-6 py-3">{t("elements.cols.code")}</th>
                      <th className="px-3 py-3">{t("elements.cols.category")}</th>
                      <th className="px-3 py-3">{t("elements.cols.method")}</th>
                      <th className="px-3 py-3">{t("elements.cols.base")}</th>
                      <th className="px-3 py-3 text-right">{t("elements.cols.params")}</th>
                      <th className="px-3 py-3">{t("elements.cols.flags")}</th>
                      <th className="px-3 py-3">{t("elements.cols.effect")}</th>
                      <th className="px-3 py-3">{t("elements.cols.state")}</th>
                      <th className="px-6 py-3" />
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line-soft">
                    {rows.map((e) => (
                      <tr key={e.id} className={cn("hover:bg-bg-soft", !e.active && "opacity-60")}>
                        <td className="px-6 py-3">
                          <div className="font-mono-tabular text-[12px] font-semibold text-ink">{e.code}</div>
                          <div className="text-[12.5px] text-ink-2">{e.label}</div>
                        </td>
                        <td className="px-3 py-3">
                          <Badge tone={categoryTone(e.category)} showDot={false}>
                            {t(`category.${e.category}`)}
                          </Badge>
                        </td>
                        <td className="px-3 py-3 text-ink-2">{t(`method.${e.method}`)}</td>
                        <td className="font-mono-tabular px-3 py-3 text-[12px] text-ink-3">
                          {e.baseReference ?? "—"}
                        </td>
                        <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                          {renderParams(e, locale)}
                        </td>
                        <td className="px-3 py-3">
                          <div className="flex flex-wrap gap-1">
                            {e.taxable && (
                              <span className="rounded-[6px] bg-orange-50 px-1.5 py-0.5 text-[10.5px] font-semibold text-orange-700">
                                {t("elements.taxable")}
                              </span>
                            )}
                            {e.socialContributable && (
                              <span className="rounded-[6px] bg-info-50 px-1.5 py-0.5 text-[10.5px] font-semibold text-info-600">
                                {t("elements.contributable")}
                              </span>
                            )}
                            {!e.taxable && !e.socialContributable && <span className="text-ink-4">—</span>}
                          </div>
                        </td>
                        <td className="px-3 py-3 text-[12px] text-ink-3">
                          {e.effectiveFrom}
                          {e.effectiveTo ? ` → ${e.effectiveTo}` : ""}
                        </td>
                        <td className="px-3 py-3">
                          {e.active ? (
                            <Badge tone="success" showDot={false}>
                              {t("elements.active")}
                            </Badge>
                          ) : (
                            <Badge tone="gray" showDot={false}>
                              {t("elements.inactive")}
                            </Badge>
                          )}
                        </td>
                        <td className="px-6 py-3 text-right">
                          {canManage &&
                            (e.active ? (
                              <Button
                                variant="ghost"
                                size="sm"
                                className="text-danger-600 hover:bg-danger-50"
                                onClick={() => setToDeactivate(e)}
                                title={t("elements.deactivate")}
                              >
                                <PowerOff className="h-3.5 w-3.5" />
                              </Button>
                            ) : (
                              <Button
                                variant="secondary"
                                size="sm"
                                onClick={() => activate.mutate(e.id)}
                                disabled={activate.isPending}
                                title={t("elements.activate")}
                              >
                                <Power className="h-3.5 w-3.5" />
                                {t("elements.activate")}
                              </Button>
                            ))}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        </div>
      )}

      {creating && (
        <CreateDialog
          country={country}
          defaultOrder={nextDisplayOrder}
          isSaving={create.isPending}
          onClose={() => setCreating(false)}
          onSubmit={(body) => create.mutate(body)}
          t={t}
        />
      )}

      {toDeactivate && (
        <Dialog
          open
          onClose={() => setToDeactivate(null)}
          size="sm"
          title={t("elements.deactivateTitle")}
          subtitle={`${toDeactivate.code} · ${toDeactivate.label}`}
          footer={
            <>
              <Button variant="secondary" onClick={() => setToDeactivate(null)} disabled={deactivate.isPending}>
                {t("elements.dialog.cancel")}
              </Button>
              <Button
                className="bg-danger-600 hover:bg-danger-600/90"
                onClick={() => deactivate.mutate(toDeactivate.id)}
                disabled={deactivate.isPending}
              >
                {deactivate.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    {t("elements.deactivating")}
                  </>
                ) : (
                  t("elements.deactivate")
                )}
              </Button>
            </>
          }
        >
          <p className="text-[13.5px] text-ink-2">{t("elements.deactivateConfirm")}</p>
        </Dialog>
      )}
    </>
  );
}

type ElementForm = {
  code: string;
  label: string;
  category: PayElementCategory;
  method: CalculationMethod;
  baseReference: string;
  rate: string;
  ceiling: string;
  floor: string;
  exemptionThreshold: string;
  flatAmount: string;
  bracketTableCode: string;
  lookupTableCode: string;
  taxable: boolean;
  socialContributable: boolean;
  displayOrder: string;
  effectiveFrom: string;
  effectiveTo: string;
};

function CreateDialog({
  country,
  defaultOrder,
  isSaving,
  onClose,
  onSubmit,
  t,
}: {
  country: string;
  defaultOrder: number;
  isSaving: boolean;
  onClose: () => void;
  onSubmit: (body: CreatePayElementRequest) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [form, setForm] = React.useState<ElementForm>({
    code: "",
    label: "",
    category: "EARNING",
    method: "RATE",
    baseReference: "GROSS",
    rate: "",
    ceiling: "",
    floor: "",
    exemptionThreshold: "",
    flatAmount: "",
    bracketTableCode: "",
    lookupTableCode: "",
    taxable: false,
    socialContributable: false,
    displayOrder: String(defaultOrder),
    effectiveFrom: `${new Date().getFullYear()}-01-01`,
    effectiveTo: "",
  });
  const [error, setError] = React.useState<string | null>(null);

  const set =
    <K extends keyof ElementForm>(key: K) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setForm((p) => ({ ...p, [key]: e.target.value }));

  const needsBase = form.method === "RATE" || form.method === "BRACKET" || form.method === "LOOKUP_TABLE";

  function submit() {
    if (!form.code.trim() || !form.label.trim() || !form.effectiveFrom) {
      setError(t("elements.validation.required"));
      return;
    }
    const num = (s: string): number | null => (s.trim() === "" ? null : Number(s));
    const body: CreatePayElementRequest = {
      code: form.code.trim().toUpperCase(),
      label: form.label.trim(),
      category: form.category,
      method: form.method,
      countryCode: country,
      baseReference: needsBase ? form.baseReference.trim() || null : null,
      rate: form.method === "RATE" && form.rate.trim() !== "" ? Number(form.rate) / 100 : null,
      ceiling: form.method === "RATE" ? num(form.ceiling) : null,
      floor: form.method === "RATE" ? num(form.floor) : null,
      flatAmount: form.method === "FLAT" ? num(form.flatAmount) : null,
      bracketTableCode: form.method === "BRACKET" ? form.bracketTableCode.trim() || null : null,
      lookupTableCode: form.method === "LOOKUP_TABLE" ? form.lookupTableCode.trim() || null : null,
      exemptionThreshold: num(form.exemptionThreshold),
      taxable: form.taxable,
      socialContributable: form.socialContributable,
      displayOrder: Number(form.displayOrder) || 0,
      effectiveFrom: form.effectiveFrom,
      effectiveTo: form.effectiveTo || null,
    };
    onSubmit(body);
  }

  return (
    <Dialog
      open
      onClose={onClose}
      size="lg"
      title={t("elements.dialog.title")}
      subtitle={t("elements.dialog.subtitle", { country })}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSaving}>
            {t("elements.dialog.cancel")}
          </Button>
          <Button onClick={submit} disabled={isSaving}>
            {isSaving ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("elements.dialog.saving")}
              </>
            ) : (
              t("elements.dialog.save")
            )}
          </Button>
        </>
      }
    >
      <datalist id="pe-base-refs">
        {BASE_REFERENCES.map((r) => (
          <option key={r} value={r} />
        ))}
      </datalist>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label={t("elements.cols.code")} hint={t("elements.hints.code")}>
          <Input value={form.code} onChange={set("code")} placeholder="PRIME_TRANSPORT" />
        </Field>
        <Field label={t("elements.fields.label")}>
          <Input value={form.label} onChange={set("label")} placeholder={t("elements.fields.labelPlaceholder")} />
        </Field>
        <Field label={t("elements.cols.category")}>
          <Select value={form.category} onChange={set("category")}>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {t(`category.${c}`)}
              </option>
            ))}
          </Select>
        </Field>
        <Field label={t("elements.cols.method")}>
          <Select value={form.method} onChange={set("method")}>
            {METHODS.map((m) => (
              <option key={m} value={m}>
                {t(`method.${m}`)}
              </option>
            ))}
          </Select>
        </Field>

        {needsBase && (
          <Field label={t("elements.cols.base")} hint={t("elements.hints.base")}>
            <Input list="pe-base-refs" value={form.baseReference} onChange={set("baseReference")} placeholder="GROSS" />
          </Field>
        )}

        {form.method === "RATE" && (
          <>
            <Field label={t("elements.fields.rate")} hint={t("elements.hints.rate")}>
              <Input type="number" step="0.01" min={0} value={form.rate} onChange={set("rate")} placeholder="4.2" />
            </Field>
            <Field label={t("elements.fields.floor")} hint={t("elements.hints.money")}>
              <Input type="number" min={0} value={form.floor} onChange={set("floor")} />
            </Field>
            <Field label={t("elements.fields.ceiling")} hint={t("elements.hints.money")}>
              <Input type="number" min={0} value={form.ceiling} onChange={set("ceiling")} />
            </Field>
          </>
        )}

        {form.method === "FLAT" && (
          <Field label={t("elements.fields.flatAmount")} hint={t("elements.hints.money")}>
            <Input type="number" min={0} value={form.flatAmount} onChange={set("flatAmount")} />
          </Field>
        )}

        {form.method === "BRACKET" && (
          <Field label={t("elements.fields.bracketTableCode")} hint={t("elements.hints.bracket")}>
            <Input value={form.bracketTableCode} onChange={set("bracketTableCode")} placeholder="IRPP_CM_2026" />
          </Field>
        )}

        {form.method === "LOOKUP_TABLE" && (
          <Field label={t("elements.fields.lookupTableCode")} hint={t("elements.hints.lookup")}>
            <Input value={form.lookupTableCode} onChange={set("lookupTableCode")} placeholder="RAV_CM" />
          </Field>
        )}

        <Field label={t("elements.fields.exemptionThreshold")} hint={t("elements.hints.exemption")}>
          <Input type="number" min={0} value={form.exemptionThreshold} onChange={set("exemptionThreshold")} />
        </Field>
        <Field label={t("elements.fields.displayOrder")} hint={t("elements.hints.order")}>
          <Input type="number" min={0} value={form.displayOrder} onChange={set("displayOrder")} />
        </Field>
        <Field label={t("elements.fields.effectiveFrom")} hint={t("elements.hints.effectiveFrom")}>
          <Input type="date" value={form.effectiveFrom} onChange={set("effectiveFrom")} />
        </Field>
        <Field label={t("elements.fields.effectiveTo")} hint={t("elements.hints.effectiveTo")}>
          <Input type="date" value={form.effectiveTo} onChange={set("effectiveTo")} />
        </Field>

        <div className="flex flex-wrap items-center gap-5 sm:col-span-2">
          <label className="flex items-center gap-2 text-[13px] text-ink-2">
            <input type="checkbox" checked={form.taxable} onChange={(e) => setForm((p) => ({ ...p, taxable: e.target.checked }))} className="h-4 w-4 accent-orange-500" />
            {t("elements.fields.taxable")}
          </label>
          <label className="flex items-center gap-2 text-[13px] text-ink-2">
            <input type="checkbox" checked={form.socialContributable} onChange={(e) => setForm((p) => ({ ...p, socialContributable: e.target.checked }))} className="h-4 w-4 accent-orange-500" />
            {t("elements.fields.contributable")}
          </label>
        </div>

        {error && <p className="text-[12px] text-danger-600 sm:col-span-2">{error}</p>}
      </div>
    </Dialog>
  );
}

function Select({
  value,
  onChange,
  children,
}: {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  children: React.ReactNode;
}) {
  return (
    <select
      value={value}
      onChange={onChange}
      className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none transition-all duration-200 focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
    >
      {children}
    </select>
  );
}

function StatCard({
  icon: Icon,
  tone,
  label,
  value,
  sub,
}: {
  icon: LucideIcon;
  tone: "orange" | "info" | "violet" | "success" | "warning";
  label: string;
  value: string;
  sub: string;
}) {
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between">
        <div className="min-w-0">
          <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">{label}</div>
          <div className="font-display font-mono-tabular mt-1.5 text-[24px] font-extrabold tracking-tight text-ink">
            {value}
          </div>
          <div className="mt-1 text-[11.5px] text-ink-3">{sub}</div>
        </div>
        <IconTile icon={Icon} tone={tone} size="sm" />
      </div>
    </Card>
  );
}

function renderParams(e: PayElementResponse, locale: "fr" | "en"): React.ReactNode {
  const money = (v: number | string | null | undefined) =>
    v == null ? "—" : formatMoney(Number(v), { locale, withCurrency: false });
  switch (e.method) {
    case "RATE": {
      const rate = e.rate == null ? "—" : `${(Number(e.rate) * 100).toLocaleString(locale === "fr" ? "fr-FR" : "en-US", { maximumFractionDigits: 2 })}%`;
      const clamp = e.ceiling != null ? ` · ≤ ${money(e.ceiling)}` : "";
      return `${rate}${clamp}`;
    }
    case "FLAT":
      return money(e.flatAmount);
    case "BRACKET":
      return e.bracketTableCode ?? "—";
    case "LOOKUP_TABLE":
      return e.lookupTableCode ?? "—";
    default:
      return "—";
  }
}
