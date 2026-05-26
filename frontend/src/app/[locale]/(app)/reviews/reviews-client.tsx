"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useReviews, useCreateReview } from "@/hooks/modules/useReviews";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { StatCard } from "@/components/ui-tokens/StatCard";
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

export function ReviewsClient() {
  const t = useTranslations("manager.reviews");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const reviews = useReviews(null);
  const employees = useEmployees();
  const create = useCreateReview();
  const [open, setOpen] = React.useState(false);

  const form = useForm<{ employeeId: string; periode: string }>({
    defaultValues: { employeeId: "", periode: new Date().getFullYear() + "-Q1" },
  });

  const onSubmit = (values: { employeeId: string; periode: string }) => {
    const emp = employees.data?.find((e) => e.id === values.employeeId);
    if (!emp) return;
    create.mutate(
      {
        employeeId: values.employeeId,
        evaluateurPartyId: emp.actorId, // self-eval placeholder; manager actor in real flow
        evaluateurDisplayName: emp.actorDisplayName,
        periode: values.periode,
      },
      {
        onSuccess: () => {
          toast.success(t("form.submit"));
          setOpen(false);
          form.reset();
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );
  };

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-15"
        crumbs={[{ label: tNav("items.reviews") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("newButton")}
          </Button>
        }
      />

      {!reviews.isLoading && reviews.data && reviews.data.length > 0 && (
        <ReviewsOverview reviews={reviews.data} />
      )}

      {reviews.isLoading && <Skeleton className="h-32 w-full" />}

      {!reviews.isLoading && reviews.data && reviews.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!reviews.isLoading && reviews.data && reviews.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[t("table.periode"), t("table.note"), t("table.status"), ""].map((h, i) => (
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
                {reviews.data.map((r) => (
                  <tr key={r.id} className="cursor-pointer hover:bg-brand-50/40">
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                      <Link href={`/reviews/${r.id}` as never} className="hover:text-brand-700">
                        {r.periode}
                      </Link>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {r.noteGlobale != null ? fmt.number(Number(r.noteGlobale)) : "—"}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]"><StatusBadge kind="review" status={r.status} /></td>
                    <td />
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
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="reviewEmployee">Employé</Label>
              <Select
                value={form.watch("employeeId")}
                onValueChange={(v) => form.setValue("employeeId", v)}
              >
                <SelectTrigger id="reviewEmployee">
                  <SelectValue placeholder="—" />
                </SelectTrigger>
                <SelectContent>
                  {employees.data?.map((e) => (
                    <SelectItem key={e.id} value={e.id}>
                      {e.matricule} — {e.actorDisplayName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="reviewPeriode">{t("form.periode")}</Label>
              <Input id="reviewPeriode" {...form.register("periode")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
                Annuler
              </Button>
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

const REVIEW_STATUS_HEX: Record<string, string> = {
  FINALIZED: "#10b981",
  ACKNOWLEDGED: "#3b82f6",
  SUBMITTED: "#f59e0b",
  DRAFT: "#9a9283",
};

const SCORE_BUCKETS: Array<{ note: number; label: string; hex: string }> = [
  { note: 5, label: "Exceptionnel", hex: "#10b981" },
  { note: 4, label: "Excellent", hex: "#34d399" },
  { note: 3, label: "Solide", hex: "#f59e0b" },
  { note: 2, label: "À développer", hex: "#fb8533" },
  { note: 1, label: "Insuffisant", hex: "#ef4444" },
];

function ReviewsOverview({ reviews }: { reviews: import("@/lib/types/hrm/review").Review[] }) {
  const withNote = reviews.filter((r) => r.noteGlobale != null);
  const avg = withNote.length
    ? withNote.reduce((s, r) => s + Number(r.noteGlobale), 0) / withNote.length
    : 0;
  const byStatus: Record<string, number> = {};
  for (const r of reviews) byStatus[r.status] = (byStatus[r.status] ?? 0) + 1;
  const total = reviews.length || 1;
  const completedPct = Math.round(((byStatus.FINALIZED ?? 0) / total) * 100);

  let acc = 0;
  const parts: string[] = [];
  for (const s of ["FINALIZED", "ACKNOWLEDGED", "SUBMITTED", "DRAFT"]) {
    const n = byStatus[s] ?? 0;
    if (!n) continue;
    const start = (acc / total) * 100;
    acc += n;
    parts.push(`${REVIEW_STATUS_HEX[s]} ${start}% ${(acc / total) * 100}%`);
  }
  const donut = parts.length
    ? `conic-gradient(${parts.join(", ")})`
    : "conic-gradient(var(--color-cream-2) 0 100%)";

  const buckets = SCORE_BUCKETS.map((b) => ({
    ...b,
    count: withNote.filter((r) => Math.round(Number(r.noteGlobale)) === b.note).length,
  }));
  const maxBucket = Math.max(1, ...buckets.map((b) => b.count));

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard tone="orange" label="Évaluations" value={reviews.length} footer="sur la période" />
        <StatCard tone="amber" label="À clôturer" value={byStatus.SUBMITTED ?? 0} footer="en attente revue" />
        <StatCard tone="green" label="Score moyen" value={`${avg.toFixed(1)} / 5`} footer={`${withNote.length} notées`} />
        <StatCard tone="blue" label="Finalisées" value={byStatus.FINALIZED ?? 0} footer={`${completedPct}%`} />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Distribution des scores</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2.5">
            {buckets.map((b) => (
              <div key={b.note} className="flex items-center gap-3">
                <span className="w-28 shrink-0 text-[12.5px] text-ink-2">{b.label}</span>
                <div className="h-2.5 flex-1 overflow-hidden rounded-full bg-cream-2">
                  <div className="h-full rounded-full" style={{ width: `${(b.count / maxBucket) * 100}%`, background: b.hex }} />
                </div>
                <span className="w-8 shrink-0 text-right text-[12px] font-semibold tabular text-ink">{b.count}</span>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Statut des évaluations</CardTitle>
          </CardHeader>
          <CardContent className="flex items-center gap-5">
            <div className="relative grid size-28 shrink-0 place-items-center rounded-full" style={{ background: donut }}>
              <div className="grid size-20 place-items-center rounded-full bg-white text-center">
                <div>
                  <div className="font-display text-[20px] font-extrabold leading-none text-ink tabular">{completedPct}%</div>
                  <div className="text-[9px] text-ink-4">finalisées</div>
                </div>
              </div>
            </div>
            <ul className="flex-1 space-y-1.5 text-[12.5px]">
              {["FINALIZED", "ACKNOWLEDGED", "SUBMITTED", "DRAFT"].map((s) => (
                <li key={s} className="flex items-center justify-between">
                  <span className="flex items-center gap-2 text-ink-2">
                    <span className="size-2.5 rounded-full" style={{ background: REVIEW_STATUS_HEX[s] }} />
                    {s}
                  </span>
                  <span className="font-semibold text-ink tabular">{byStatus[s] ?? 0}</span>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
