"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { Plus, Send, CheckCheck, Lock, Loader2, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import {
  useReview,
  useReviewObjectives,
  useAddObjective,
  useSubmitReview,
  useReviewTransition,
} from "@/hooks/modules/useReviews";
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
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { WorkflowTimeline } from "@/components/ui-tokens/WorkflowTimeline";

export function ReviewDetailClient({ id }: { id: string }) {
  const t = useTranslations("manager.reviews");
  const tNav = useTranslations("navigation");
  const review = useReview(id);
  const objectives = useReviewObjectives(id);
  const addObj = useAddObjective(id);
  const submit = useSubmitReview(id);
  const transition = useReviewTransition(id);
  const [openObj, setOpenObj] = React.useState(false);
  const [openSubmit, setOpenSubmit] = React.useState(false);

  const objForm = useForm<{ description: string; poids: number }>({
    defaultValues: { description: "", poids: 25 },
  });
  const subForm = useForm<{ noteGlobale: number; commentaires: string; planAction: string }>({
    defaultValues: { noteGlobale: 14, commentaires: "", planAction: "" },
  });

  if (review.isLoading) return <Skeleton className="h-40 w-full" />;
  if (!review.data) {
    return (
      <Card>
        <CardContent className="flex items-start gap-3">
          <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
          <div className="text-sm">Review not found</div>
        </CardContent>
      </Card>
    );
  }

  const r = review.data;
  const order = ["DRAFT", "SUBMITTED", "ACKNOWLEDGED", "FINALIZED"];
  const ci = order.indexOf(r.status);
  const steps = order.map((o, i) => ({
    key: o,
    label: o,
    state: i < ci ? ("done" as const) : i === ci ? ("active" as const) : ("todo" as const),
  }));

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-15"
        crumbs={[{ label: tNav("items.reviews"), href: "/reviews" }, { label: r.periode }]}
        title={
          <div className="flex flex-wrap items-center gap-3">
            <span>Évaluation {r.periode}</span>
            <StatusBadge kind="review" status={r.status} />
          </div>
        }
        subtitle={r.evaluateurDisplayName}
        actions={
          <>
            {r.status === "DRAFT" && (
              <Button onClick={() => setOpenSubmit(true)}>
                <Send className="size-4" />
                {t("detail.submit")}
              </Button>
            )}
            {r.status === "SUBMITTED" && (
              <Button
                onClick={() =>
                  transition.mutate("acknowledge", {
                    onSuccess: () => toast.success(t("detail.acknowledge")),
                    onError: (err) => toast.error((err as Error).message),
                  })
                }
                disabled={transition.isPending}
              >
                <CheckCheck className="size-4" />
                {t("detail.acknowledge")}
              </Button>
            )}
            {r.status === "ACKNOWLEDGED" && (
              <Button
                onClick={() =>
                  transition.mutate("finalize", {
                    onSuccess: () => toast.success(t("detail.finalize")),
                    onError: (err) => toast.error((err as Error).message),
                  })
                }
                disabled={transition.isPending}
              >
                <Lock className="size-4" />
                {t("detail.finalize")}
              </Button>
            )}
          </>
        }
      />

      <Card>
        <CardContent>
          <WorkflowTimeline steps={steps} />
        </CardContent>
      </Card>

      <Card>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="font-display text-base font-bold text-ink">{t("detail.objectives")}</div>
            {r.status === "DRAFT" && (
              <Button variant="secondary" size="sm" onClick={() => setOpenObj(true)}>
                <Plus className="size-4" />
                {t("detail.addObjective")}
              </Button>
            )}
          </div>
          {objectives.data && objectives.data.length === 0 && (
            <p className="py-4 text-center text-sm text-ink-3">—</p>
          )}
          {objectives.data && objectives.data.length > 0 && (
            <ul className="space-y-2">
              {objectives.data.map((o) => (
                <li key={o.id} className="flex items-start justify-between gap-3 rounded-lg border border-line-soft bg-cream-elev px-3 py-2">
                  <span className="text-[13.5px] text-ink">{o.description}</span>
                  <span className="font-mono text-[12px] text-ink-3">{Number(o.poids)}%</span>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <Dialog open={openObj} onOpenChange={setOpenObj}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("detail.addObjective")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={objForm.handleSubmit((values) =>
              addObj.mutate(
                { description: values.description, poids: Number(values.poids) },
                {
                  onSuccess: () => {
                    toast.success("OK");
                    setOpenObj(false);
                    objForm.reset();
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="objDesc">Description</Label>
              <Input id="objDesc" {...objForm.register("description", { required: true })} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="objPoids">Poids (%)</Label>
              <Input id="objPoids" type="number" min={1} max={100} {...objForm.register("poids")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpenObj(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={addObj.isPending}>
                {addObj.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("detail.addObjective")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog open={openSubmit} onOpenChange={setOpenSubmit}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("detail.submit")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={subForm.handleSubmit((values) =>
              submit.mutate(
                {
                  noteGlobale: Number(values.noteGlobale),
                  commentaires: values.commentaires,
                  planAction: values.planAction || null,
                },
                {
                  onSuccess: () => {
                    toast.success(t("detail.submit"));
                    setOpenSubmit(false);
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="noteGlobale">{t("detail.noteGlobale")}</Label>
              <Input id="noteGlobale" type="number" min={0} max={20} step={0.5} {...subForm.register("noteGlobale")} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="commentaires">{t("detail.commentaires")}</Label>
              <Input id="commentaires" {...subForm.register("commentaires", { required: true })} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="planAction">{t("detail.planAction")}</Label>
              <Input id="planAction" {...subForm.register("planAction")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpenSubmit(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={submit.isPending}>
                {submit.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("detail.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
