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
