"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2, Download, FileText } from "lucide-react";
import { toast } from "sonner";
import { useDeclarations, useCreateDeclaration } from "@/hooks/modules/useDeclarations";
import { useFormat } from "@/hooks/useFormat";
import { exportCsv } from "@/lib/csv";
import { exportPdf } from "@/lib/pdf";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Link } from "@/i18n/navigation";
import {
  createDeclarationSchema,
  type CreateDeclarationFormValues,
} from "@/lib/validation/hrm/declaration.schema";

const TYPES = ["CNPS", "DIPE", "IRPP_CAC", "FNE", "CFC"] as const;
const FORMATS = ["CSV", "XML", "EDI", "PDF"] as const;

export function DeclarationsClient() {
  const t = useTranslations("payrollOfficer.declarations");
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const list = useDeclarations();
  const create = useCreateDeclaration();
  const [open, setOpen] = React.useState(false);

  const form = useForm<CreateDeclarationFormValues>({
    resolver: zodResolver(createDeclarationSchema),
    defaultValues: {
      type: "CNPS",
      periode: new Date().toISOString().slice(0, 7),
      format: "CSV",
    },
  });

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-26"
        crumbs={[{ label: tNav("items.declarations") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            <Button
              variant="secondary"
              disabled={!list.data || list.data.length === 0}
              onClick={() =>
                exportCsv(
                  `declarations-${new Date().toISOString().slice(0, 10)}`,
                  list.data ?? [],
                  [
                    { header: t("table.type"), value: (d) => d.type },
                    { header: t("table.periode"), value: (d) => d.periode },
                    { header: t("table.format"), value: (d) => d.format },
                    { header: t("table.status"), value: (d) => d.statut },
                    { header: t("table.generatedAt"), value: (d) => d.generatedAt ?? "" },
                    { header: t("table.submittedAt"), value: (d) => d.submittedAt ?? "" },
                  ],
                )
              }
            >
              <Download className="size-4" />
              {tCommon("actions.exportCsv")}
            </Button>
            <Button
              variant="secondary"
              disabled={!list.data || list.data.length === 0}
              onClick={() =>
                exportPdf(
                  `declarations-${new Date().toISOString().slice(0, 10)}`,
                  list.data ?? [],
                  [
                    { header: t("table.type"), value: (d) => d.type },
                    { header: t("table.periode"), value: (d) => d.periode },
                    { header: t("table.format"), value: (d) => d.format },
                    { header: t("table.status"), value: (d) => d.statut },
                    { header: t("table.generatedAt"), value: (d) => d.generatedAt ?? "" },
                  ],
                  { title: t("title"), subtitle: t("subtitle") },
                )
              }
            >
              <FileText className="size-4" />
              {tCommon("actions.exportPdf")}
            </Button>
            <Button onClick={() => setOpen(true)}>
              <Plus className="size-4" />
              {t("newButton")}
            </Button>
          </>
        }
      />

      {list.isLoading && <Skeleton className="h-32 w-full" />}

      {!list.isLoading && list.data && list.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!list.isLoading && list.data && list.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("table.type"),
                    t("table.periode"),
                    t("table.format"),
                    t("table.status"),
                    t("table.generatedAt"),
                    t("table.submittedAt"),
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
                {list.data.map((d) => (
                  <tr key={d.id} className="cursor-pointer hover:bg-brand-50/40">
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                      <Link href={`/declarations/${d.id}` as never} className="hover:text-brand-700">
                        {t(`types.${d.type}` as never)}
                      </Link>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{d.periode}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 font-mono">{d.format}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]"><StatusBadge kind="socialDeclaration" status={d.statut} /></td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{d.generatedAt ? fmt.date(d.generatedAt) : "—"}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{d.submittedAt ? fmt.date(d.submittedAt) : "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) =>
              create.mutate(values, {
                onSuccess: () => {
                  toast.success(t("form.submit"));
                  setOpen(false);
                  form.reset();
                },
                onError: (err) => toast.error((err as Error).message),
              }),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="declType">{t("form.type")}</Label>
              <Select value={form.watch("type")} onValueChange={(v) => form.setValue("type", v as never)}>
                <SelectTrigger id="declType"><SelectValue /></SelectTrigger>
                <SelectContent>
                  {TYPES.map((tp) => (
                    <SelectItem key={tp} value={tp}>{t(`types.${tp}` as never)}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="declPeriode">{t("form.periode")}</Label>
                <Input id="declPeriode" {...form.register("periode")} />
                {form.formState.errors.periode && (
                  <p className="text-[12px] text-status-red-600">{form.formState.errors.periode.message}</p>
                )}
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="declFormat">{t("form.format")}</Label>
                <Select value={form.watch("format")} onValueChange={(v) => form.setValue("format", v as never)}>
                  <SelectTrigger id="declFormat"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {FORMATS.map((f) => (
                      <SelectItem key={f} value={f}>{f}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>Annuler</Button>
              <Button type="submit" disabled={create.isPending}>
                {create.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
