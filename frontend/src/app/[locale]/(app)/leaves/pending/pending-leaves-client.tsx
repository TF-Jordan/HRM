"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { Check, X, Loader2 } from "lucide-react";
import { toast } from "sonner";
import {
  usePendingLeaves,
  useApproveLeave,
  useRejectLeave,
} from "@/hooks/modules/useLeaves";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export function PendingLeavesClient() {
  const t = useTranslations("manager.pendingLeaves");
  const tNav = useTranslations("navigation");
  const tStatuses = useTranslations("statuses.leaveType");
  const fmt = useFormat();
  const list = usePendingLeaves();
  const approve = useApproveLeave();
  const reject = useRejectLeave();
  const [rejectingId, setRejectingId] = React.useState<string | null>(null);
  const form = useForm<{ commentaire: string }>({ defaultValues: { commentaire: "" } });

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-10"
        crumbs={[{ label: tNav("items.leaves") }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
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
                  {[t("table.type"), t("table.dateDebut"), t("table.dateFin"), t("table.jours"), t("table.motif"), ""].map(
                    (h, i) => (
                      <th
                        key={i}
                        className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                      >
                        {h}
                      </th>
                    ),
                  )}
                </tr>
              </thead>
              <tbody>
                {list.data.map((l) => (
                  <tr key={l.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                      {tStatuses(l.type)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.date(l.dateDebut)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.date(l.dateFin)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">
                      {fmt.number(Number(l.nbJours))}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">{l.motif ?? "—"}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-right">
                      <div className="flex justify-end gap-1.5">
                        <Button
                          size="sm"
                          onClick={() =>
                            approve.mutate(l.id, {
                              onSuccess: () => toast.success(t("approve")),
                              onError: (err) => toast.error((err as Error).message),
                            })
                          }
                        >
                          <Check className="size-4" />
                          {t("approve")}
                        </Button>
                        <Button
                          size="sm"
                          variant="secondary"
                          onClick={() => setRejectingId(l.id)}
                        >
                          <X className="size-4" />
                          {t("reject")}
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <Dialog open={!!rejectingId} onOpenChange={(v) => !v && setRejectingId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("rejectTitle")}</DialogTitle>
            <DialogDescription>{t("rejectDescription")}</DialogDescription>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) => {
              if (!rejectingId) return;
              reject.mutate(
                { leaveId: rejectingId, commentaire: values.commentaire },
                {
                  onSuccess: () => {
                    toast.success(t("reject"));
                    setRejectingId(null);
                    form.reset();
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              );
            })}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="commentaire">{t("commentaire")}</Label>
              <Input id="commentaire" {...form.register("commentaire", { required: true, minLength: 1 })} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setRejectingId(null)}>
                Annuler
              </Button>
              <Button type="submit" variant="destructive" disabled={reject.isPending}>
                {reject.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("reject")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
