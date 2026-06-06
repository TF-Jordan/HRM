"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { Check, Loader2, Plus, Send, Trash2, Upload } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { categoryTone, EXPENSE_CATEGORIES, type ExpenseCategory } from "@/lib/expense-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { ExpenseReportResponse } from "@/server/ksm/modules/expenses";
import type { MissionOrderResponse } from "@/server/ksm/modules/missions";
import type { StoredFileResponse } from "@/server/ksm/modules/files";

type StagedLine = {
  key: string;
  categorie: ExpenseCategory;
  description: string;
  montant: string;
  justificatifFileId?: string;
  justificatifName?: string;
  uploading?: boolean;
};

type MinePayload = { employee: EmployeeResponse | null; reports: ExpenseReportResponse[] };

const CURRENT_PERIODE = new Date().toISOString().slice(0, 7); // YYYY-MM

function emptyLine(): StagedLine {
  return { key: crypto.randomUUID(), categorie: "TRANSPORT", description: "", montant: "" };
}

export function NewExpenseForm({ initialMissionOrderId }: { initialMissionOrderId?: string }) {
  const t = useTranslations("expenses");
  const tCat = useTranslations("expenses.category");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();

  const [objet, setObjet] = React.useState("");
  // Pre-selected when arriving from a mission's "regularize advance" CTA.
  const [missionOrderId, setMissionOrderId] = React.useState(initialMissionOrderId ?? "");
  const [lines, setLines] = React.useState<StagedLine[]>([emptyLine()]);

  const mine = useQuery({
    queryKey: ["hrm", "expenses", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/expenses/mine"),
  });
  const missions = useQuery({
    queryKey: ["hrm", "mission-orders", "mine"],
    queryFn: () =>
      apiFetch<{ employee: EmployeeResponse | null; orders: MissionOrderResponse[] }>(
        "/api/hrm/mission-orders/mine",
      ),
  });

  const total = lines.reduce((s, l) => s + (Number(l.montant) || 0), 0);
  const byCategory = React.useMemo(() => {
    const m = new Map<string, number>();
    for (const l of lines) m.set(l.categorie, (m.get(l.categorie) ?? 0) + (Number(l.montant) || 0));
    return [...m.entries()].filter(([, v]) => v > 0);
  }, [lines]);

  const validLines = lines.filter((l) => l.description.trim() && Number(l.montant) > 0);
  const canSubmit = !!objet.trim() && validLines.length > 0;

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  function patchLine(key: string, patch: Partial<StagedLine>) {
    setLines((prev) => prev.map((l) => (l.key === key ? { ...l, ...patch } : l)));
  }

  async function uploadJustificatif(key: string, file: File) {
    patchLine(key, { uploading: true });
    try {
      const fd = new FormData();
      fd.append("file", file, file.name);
      const res = await fetch("/api/files/upload", { method: "POST", body: fd });
      const json = (await res.json()) as { ok: boolean; data?: StoredFileResponse; message?: string };
      if (!res.ok || !json.ok || !json.data) throw new Error(json.message ?? "upload failed");
      patchLine(key, { justificatifFileId: json.data.id, justificatifName: file.name, uploading: false });
    } catch (cause) {
      patchLine(key, { uploading: false });
      handleError(cause);
    }
  }

  const persist = useMutation({
    mutationFn: async (submit: boolean) => {
      const employeeId = mine.data?.employee?.id;
      if (!employeeId) throw new Error("No employee record");
      // 1. create the DRAFT report
      const report = await apiFetch<ExpenseReportResponse>("/api/hrm/expenses", {
        method: "POST",
        body: {
          employeeId,
          periode: CURRENT_PERIODE,
          motif: objet.trim(),
          missionOrderId: missionOrderId || null,
        },
      });
      // 2. add every valid line
      for (const l of validLines) {
        await apiFetch(`/api/hrm/expenses/${report.id}/lines`, {
          method: "POST",
          body: {
            description: l.description.trim(),
            montant: Number(l.montant),
            categorie: l.categorie,
            justificatifFileId: l.justificatifFileId ?? null,
          },
        });
      }
      // 3. optionally submit
      if (submit) {
        await apiFetch(`/api/hrm/expenses/${report.id}/submit`, { method: "POST" });
      }
      return report;
    },
    onSuccess: (report, submit) => {
      toast.success(submit ? t("new.submitted") : t("new.createdDraft"));
      router.push(`/expenses/${report.id}`);
    },
    onError: handleError,
  });

  const missionOptions = (missions.data?.orders ?? []).filter((o) =>
    ["APPROVED", "IN_PROGRESS", "COMPLETED"].includes(o.status),
  );

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("mine.title"), href: "/expenses" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
        actions={
          <>
            <Link href="/expenses">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
          </>
        }
      />

      <div className="grid grid-cols-1 gap-5 lg:grid-cols-[2fr_1fr]">
        <Card>
          <CardContent padding="lg">
            <div className="grid grid-cols-2 gap-4">
              <Field label={t("new.fields.objet")} className="col-span-2">
                <Input
                  value={objet}
                  onChange={(e) => setObjet(e.target.value)}
                  placeholder={t("new.fields.objetPlaceholder")}
                />
              </Field>
              <Field label={t("new.fields.mission")} className="col-span-2">
                <select
                  value={missionOrderId}
                  onChange={(e) => setMissionOrderId(e.target.value)}
                  className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
                >
                  <option value="">{t("new.fields.missionNone")}</option>
                  {missionOptions.map((o) => (
                    <option key={o.id} value={o.id}>
                      {o.destination} · {o.dateDebut} → {o.dateFin}
                    </option>
                  ))}
                </select>
              </Field>
            </div>

            <div className="mb-3 mt-6 flex items-center justify-between">
              <span className="text-[13px] font-bold text-ink">{t("new.lines.title")}</span>
              <Button type="button" variant="secondary" className="!h-8 !px-3 !text-[12px]" onClick={() => setLines((p) => [...p, emptyLine()])}>
                <Plus className="h-3.5 w-3.5" />
                {t("new.lines.add")}
              </Button>
            </div>

            {lines.length === 0 ? (
              <p className="rounded-[12px] border border-dashed border-line bg-bg-soft px-4 py-6 text-center text-[12.5px] text-ink-3">
                {t("new.lines.empty")}
              </p>
            ) : (
              <div className="overflow-hidden rounded-[12px] border border-line">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-bg-dim">
                      <th className="px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">{t("new.lines.category")}</th>
                      <th className="px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">{t("new.lines.description")}</th>
                      <th className="px-3 py-2.5 text-right text-[11px] font-semibold uppercase tracking-wider text-ink-3">{t("new.lines.amount")}</th>
                      <th className="px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">{t("new.lines.justificatif")}</th>
                      <th />
                    </tr>
                  </thead>
                  <tbody>
                    {lines.map((l) => (
                      <tr key={l.key} className="border-t border-line-soft">
                        <td className="px-3 py-2">
                          <select
                            value={l.categorie}
                            onChange={(e) => patchLine(l.key, { categorie: e.target.value as ExpenseCategory })}
                            className="rounded-[9px] border border-line bg-white px-2 py-1.5 text-[12px] text-ink outline-none focus:border-orange-400"
                          >
                            {EXPENSE_CATEGORIES.map((c) => (
                              <option key={c} value={c}>
                                {tCat(c)}
                              </option>
                            ))}
                          </select>
                        </td>
                        <td className="px-3 py-2">
                          <input
                            value={l.description}
                            onChange={(e) => patchLine(l.key, { description: e.target.value })}
                            placeholder={t("new.lines.descriptionPlaceholder")}
                            className="w-full rounded-[9px] border border-line bg-white px-2.5 py-1.5 text-[13px] text-ink outline-none focus:border-orange-400"
                          />
                        </td>
                        <td className="px-3 py-2 text-right">
                          <input
                            type="number"
                            min={0}
                            step={500}
                            value={l.montant}
                            onChange={(e) => patchLine(l.key, { montant: e.target.value })}
                            className="w-28 rounded-[9px] border border-line bg-white px-2.5 py-1.5 text-right font-mono-tabular text-[13px] text-ink outline-none focus:border-orange-400"
                          />
                        </td>
                        <td className="px-3 py-2">
                          {l.justificatifFileId ? (
                            <Badge tone="success">
                              <Check className="h-3 w-3" />
                              {t("new.lines.attached")}
                            </Badge>
                          ) : (
                            <label className="inline-flex cursor-pointer items-center gap-1.5 rounded-[9px] border border-line bg-white px-2.5 py-1.5 text-[12px] text-ink-2 hover:bg-bg-soft">
                              {l.uploading ? (
                                <Loader2 className="h-3.5 w-3.5 animate-spin" />
                              ) : (
                                <Upload className="h-3.5 w-3.5" />
                              )}
                              {l.uploading ? t("new.lines.uploading") : t("new.lines.attach")}
                              <input
                                type="file"
                                className="hidden"
                                onChange={(e) => {
                                  const f = e.target.files?.[0];
                                  if (f) uploadJustificatif(l.key, f);
                                }}
                              />
                            </label>
                          )}
                        </td>
                        <td className="px-3 py-2 text-right">
                          <button
                            type="button"
                            aria-label={t("new.lines.remove")}
                            onClick={() => setLines((p) => p.filter((x) => x.key !== l.key))}
                            className="grid h-7 w-7 place-items-center rounded-md text-ink-3 hover:bg-bg-soft hover:text-danger-600"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        </td>
                      </tr>
                    ))}
                    <tr className="border-t border-line bg-bg-dim">
                      <td colSpan={2} className="px-3 py-3 text-[13px] font-bold text-ink">
                        {t("detail.total")}
                      </td>
                      <td className="px-3 py-3 text-right font-display font-mono-tabular text-[16px] font-extrabold text-ink">
                        {formatNumber(total, locale)}
                      </td>
                      <td colSpan={2} />
                    </tr>
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>

        <div className="flex flex-col gap-4">
          <Card className="border-orange-200 bg-[linear-gradient(135deg,var(--color-orange-50)_0%,#fff_100%)]">
            <CardContent padding="lg">
              <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                {t("new.recap.title")}
              </div>
              <div className="mt-1 font-display font-mono-tabular text-[34px] font-extrabold tracking-tight text-orange-700">
                {formatNumber(total, locale)}
              </div>
              <div className="text-[12px] text-ink-2">{t("new.recap.toReimburse")}</div>
              {byCategory.length > 0 && (
                <>
                  <div className="my-4 h-px bg-line" />
                  <div className="mb-2 text-[11px] font-semibold uppercase tracking-wider text-ink-4">
                    {t("new.recap.byCategory")}
                  </div>
                  <div className="flex flex-col gap-2">
                    {byCategory.map(([cat, val]) => (
                      <div key={cat} className="flex items-center justify-between text-[12.5px]">
                        <Badge tone={categoryTone(cat)}>{tCat(cat)}</Badge>
                        <span className="font-mono-tabular font-semibold text-ink">
                          {formatNumber(val, locale)}
                        </span>
                      </div>
                    ))}
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          <div className="flex gap-2">
            <Button
              type="button"
              variant="secondary"
              className="flex-1 justify-center"
              disabled={!canSubmit || persist.isPending}
              onClick={() => persist.mutate(false)}
            >
              {t("new.saveDraft")}
            </Button>
            <Button
              type="button"
              className="flex-1 justify-center"
              disabled={!canSubmit || persist.isPending}
              onClick={() => persist.mutate(true)}
            >
              {persist.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
              {t("new.submit")}
            </Button>
          </div>
          {!mine.data?.employee && !mine.isLoading && (
            <p className="text-[11.5px] text-danger-600">{tErrors("unknown")}</p>
          )}
        </div>
      </div>
    </>
  );
}
