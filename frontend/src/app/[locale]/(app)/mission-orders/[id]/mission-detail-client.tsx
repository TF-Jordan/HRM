"use client";

import { useTranslations } from "next-intl";
import { Loader2, Check, Play, Flag, X, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import { useMission, useMissionTransition } from "@/hooks/modules/useMissions";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { WorkflowTimeline, type WorkflowStep } from "@/components/ui-tokens/WorkflowTimeline";

export function MissionDetailClient({ id }: { id: string }) {
  const t = useTranslations("manager.missions");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const mission = useMission(id);
  const transition = useMissionTransition(id);

  if (mission.isLoading) return <Skeleton className="h-40 w-full" />;
  if (!mission.data) {
    return (
      <Card>
        <CardContent className="flex items-start gap-3">
          <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
          <div className="text-sm">Mission not found</div>
        </CardContent>
      </Card>
    );
  }

  const m = mission.data;
  const status = m.status;

  const steps: WorkflowStep[] = [
    { key: "DRAFT", label: "Brouillon", state: stateFor("DRAFT", status) },
    { key: "APPROVED", label: "Approuvé", state: stateFor("APPROVED", status) },
    { key: "IN_PROGRESS", label: "En cours", state: stateFor("IN_PROGRESS", status) },
    { key: "COMPLETED", label: "Terminé", state: stateFor("COMPLETED", status) },
  ];

  const run = (action: "approve" | "start" | "complete" | "cancel") =>
    transition.mutate(action, {
      onSuccess: () => toast.success(t(`actions.${action}` as never)),
      onError: (err) => toast.error((err as Error).message),
    });

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-21"
        crumbs={[
          { label: tNav("items.missionOrders"), href: "/mission-orders" },
          { label: m.destination },
        ]}
        title={
          <div className="flex flex-wrap items-center gap-3">
            <span>{m.destination}</span>
            <StatusBadge kind="mission" status={status} />
          </div>
        }
        subtitle={m.objet}
        actions={
          <>
            {status === "DRAFT" && (
              <Button onClick={() => run("approve")} disabled={transition.isPending}>
                {transition.isPending ? <Loader2 className="size-4 animate-spin" /> : <Check className="size-4" />}
                {t("actions.approve")}
              </Button>
            )}
            {status === "APPROVED" && (
              <Button onClick={() => run("start")} disabled={transition.isPending}>
                {transition.isPending ? <Loader2 className="size-4 animate-spin" /> : <Play className="size-4" />}
                {t("actions.start")}
              </Button>
            )}
            {status === "IN_PROGRESS" && (
              <Button onClick={() => run("complete")} disabled={transition.isPending}>
                {transition.isPending ? <Loader2 className="size-4 animate-spin" /> : <Flag className="size-4" />}
                {t("actions.complete")}
              </Button>
            )}
            {status !== "COMPLETED" && status !== "CANCELLED" && (
              <Button variant="destructive" onClick={() => run("cancel")} disabled={transition.isPending}>
                <X className="size-4" />
                {t("actions.cancel")}
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
        <CardContent>
          <dl className="grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
            <Field label={t("form.destination")} value={m.destination} />
            <Field label={t("form.objet")} value={m.objet} />
            <Field label={t("form.dateDebut")} value={fmt.date(m.dateDebut)} />
            <Field label={t("form.dateFin")} value={fmt.date(m.dateFin)} />
            <Field label={t("form.montantAvance")} value={fmt.money(m.montantAvance)} />
            <Field label={t("form.centreCout")} value={m.centreCout ?? "—"} />
          </dl>
        </CardContent>
      </Card>
    </div>
  );
}

function stateFor(target: string, current: string): WorkflowStep["state"] {
  const order = ["DRAFT", "APPROVED", "IN_PROGRESS", "COMPLETED"];
  const ti = order.indexOf(target);
  const ci = order.indexOf(current);
  if (current === "CANCELLED") return ti === 0 ? "active" : "todo";
  if (ci === -1) return "todo";
  if (ti < ci) return "done";
  if (ti === ci) return "active";
  return "todo";
}

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1 border-b border-line-soft py-2 last:border-b-0">
      <dt className="text-[11px] font-semibold uppercase tracking-wider text-ink-3">{label}</dt>
      <dd className="text-[14px] text-ink">{value}</dd>
    </div>
  );
}
