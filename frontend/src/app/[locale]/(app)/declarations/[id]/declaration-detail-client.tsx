"use client";

import { useTranslations } from "next-intl";
import { FileCheck, Send, CheckCheck, Loader2, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import { useDeclaration, useDeclarationTransition } from "@/hooks/modules/useDeclarations";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { WorkflowTimeline } from "@/components/ui-tokens/WorkflowTimeline";

export function DeclarationDetailClient({ id }: { id: string }) {
  const t = useTranslations("payrollOfficer.declarations");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const decl = useDeclaration(id);
  const transition = useDeclarationTransition(id);

  if (decl.isLoading) return <Skeleton className="h-40 w-full" />;
  if (!decl.data) {
    return (
      <Card>
        <CardContent className="flex items-start gap-3">
          <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
          <div className="text-sm">Declaration not found</div>
        </CardContent>
      </Card>
    );
  }

  const d = decl.data;
  const order = ["DRAFT", "GENERATED", "SUBMITTED", "ACKNOWLEDGED"];
  const ci = order.indexOf(d.statut);
  const steps = order.map((s, i) => ({
    key: s,
    label: s,
    state: i < ci ? ("done" as const) : i === ci ? ("active" as const) : ("todo" as const),
  }));

  const run = (action: "generate" | "submit" | "acknowledge") =>
    transition.mutate(action, {
      onSuccess: () => toast.success(t(`detail.${action}` as never)),
      onError: (err) => toast.error((err as Error).message),
    });

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-26"
        crumbs={[
          { label: tNav("items.declarations"), href: "/declarations" },
          { label: `${t(`types.${d.type}` as never)} — ${d.periode}` },
        ]}
        title={
          <div className="flex flex-wrap items-center gap-3">
            <span>{t(`types.${d.type}` as never)} — {d.periode}</span>
            <StatusBadge kind="socialDeclaration" status={d.statut} />
          </div>
        }
        subtitle={`Format: ${d.format}`}
        actions={
          <>
            {d.statut === "DRAFT" && (
              <Button onClick={() => run("generate")} disabled={transition.isPending}>
                {transition.isPending ? <Loader2 className="size-4 animate-spin" /> : <FileCheck className="size-4" />}
                {t("detail.generate")}
              </Button>
            )}
            {d.statut === "GENERATED" && (
              <Button onClick={() => run("submit")} disabled={transition.isPending}>
                {transition.isPending ? <Loader2 className="size-4 animate-spin" /> : <Send className="size-4" />}
                {t("detail.submit")}
              </Button>
            )}
            {d.statut === "SUBMITTED" && (
              <Button onClick={() => run("acknowledge")} disabled={transition.isPending}>
                {transition.isPending ? <Loader2 className="size-4 animate-spin" /> : <CheckCheck className="size-4" />}
                {t("detail.acknowledge")}
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
            <Field label={t("form.type")} value={t(`types.${d.type}` as never)} />
            <Field label={t("form.periode")} value={d.periode} />
            <Field label={t("form.format")} value={<span className="font-mono">{d.format}</span>} />
            <Field
              label={t("detail.fichierId")}
              value={d.fichierId ? <span className="font-mono text-[12px]">{d.fichierId}</span> : "—"}
            />
            <Field label={t("table.generatedAt")} value={d.generatedAt ? fmt.date(d.generatedAt) : "—"} />
            <Field label={t("table.submittedAt")} value={d.submittedAt ? fmt.date(d.submittedAt) : "—"} />
          </dl>
        </CardContent>
      </Card>
    </div>
  );
}

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1 border-b border-line-soft py-2 last:border-b-0">
      <dt className="text-[11px] font-semibold uppercase tracking-wider text-ink-3">{label}</dt>
      <dd className="text-[14px] text-ink">{value}</dd>
    </div>
  );
}
