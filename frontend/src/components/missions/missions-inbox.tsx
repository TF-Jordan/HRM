"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, Loader2, MapPin, XCircle } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Textarea } from "@/components/ui/input";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatNumber } from "@/lib/format";
import { missionStatusTone } from "@/lib/mission-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { MissionOrderResponse } from "@/server/ksm/modules/missions";

type MinePayload = {
  employee: EmployeeResponse | null;
  orders: MissionOrderResponse[];
};

export function MissionsInbox() {
  const t = useTranslations("missions");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const [declineFor, setDeclineFor] = React.useState<MissionOrderResponse | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "mission-orders", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/mission-orders/mine"),
    refetchInterval: 60_000,
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "mission-orders"] });
  }, [queryClient]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const acceptMutation = useMutation({
    mutationFn: (id: string) =>
      apiFetch<MissionOrderResponse>(`/api/hrm/mission-orders/${id}/accept`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("detail.acceptSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  const declineMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      apiFetch<MissionOrderResponse>(`/api/hrm/mission-orders/${id}/decline`, {
        method: "POST",
        body: { reason },
      }),
    onSuccess: () => {
      toast.success(t("detail.declineSuccess"));
      setDeclineFor(null);
      invalidate();
    },
    onError: handleError,
  });

  if (query.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (query.error || !query.data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {query.error instanceof BffApiError ? query.error.message : "—"}
      </div>
    );
  }

  const orders = query.data.orders;
  const pending = orders.filter((o) => o.status === "PENDING_ACCEPTANCE");
  const history = orders.filter((o) => o.status !== "PENDING_ACCEPTANCE");

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("inbox.title") }]}
        title={t("inbox.title")}
        subtitle={t("inbox.subtitle")}
      />

      <section className="mb-6">
        <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
          <span className="inline-block h-2 w-2 animate-pulse rounded-full bg-warning-500 shadow-[0_0_8px_rgba(245,158,11,0.6)]" />
          {t("inbox.pendingTitle")}
          {pending.length > 0 && (
            <Badge tone="warning">{pending.length}</Badge>
          )}
        </h2>
        {pending.length === 0 ? (
          <Card>
            <CardContent padding="md">
              <p className="text-center text-[13px] text-ink-3">{t("inbox.pendingEmpty")}</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-3 md:grid-cols-2">
            {pending.map((o) => (
              <PendingCard
                key={o.id}
                order={o}
                locale={locale}
                onAccept={() => acceptMutation.mutate(o.id)}
                onDecline={() => setDeclineFor(o)}
                accepting={acceptMutation.isPending && acceptMutation.variables === o.id}
              />
            ))}
          </div>
        )}
      </section>

      <section>
        <h2 className="mb-3 text-[13px] font-bold uppercase tracking-wider text-ink-2">
          {t("inbox.historyTitle")}
        </h2>
        {history.length === 0 ? (
          <Card>
            <CardContent padding="md">
              <p className="text-center text-[13px] text-ink-3">{t("inbox.historyEmpty")}</p>
            </CardContent>
          </Card>
        ) : (
          <div className="overflow-hidden rounded-[20px] border border-line bg-white shadow-sm-brand">
            <table className="w-full border-collapse">
              <tbody>
                {history.map((o) => (
                  <tr
                    key={o.id}
                    className="border-b border-line-soft last:border-b-0 hover:bg-bg-soft"
                  >
                    <td className="px-5 py-3">
                      <div className="flex items-start gap-2">
                        <MapPin className="mt-[2px] h-3.5 w-3.5 shrink-0 text-orange-500" />
                        <div>
                          <div className="text-[13.5px] font-semibold text-ink">
                            {o.destination}
                          </div>
                          <div className="text-[11.5px] text-ink-3">{o.objet}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-3 py-3 text-[12px] text-ink-2">
                      {formatDate(o.dateDebut, { locale })} → {formatDate(o.dateFin, { locale })}
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] font-bold text-ink">
                      {o.montantAvance != null
                        ? formatNumber(Number(o.montantAvance), locale)
                        : "—"}
                    </td>
                    <td className="px-3 py-3">
                      <Badge tone={missionStatusTone(o.status)}>{t(`status.${o.status}`)}</Badge>
                    </td>
                    <td className="px-5 py-3 text-right">
                      <Link
                        href={`/mission-orders/${o.id}`}
                        className="text-[12px] font-semibold text-orange-600 hover:text-orange-700"
                      >
                        {tCommon("actions.view")} →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <DeclineDialog
        order={declineFor}
        onClose={() => setDeclineFor(null)}
        onConfirm={(reason) =>
          declineFor && declineMutation.mutate({ id: declineFor.id, reason })
        }
        loading={declineMutation.isPending}
      />
    </>
  );
}

function PendingCard({
  order,
  locale,
  onAccept,
  onDecline,
  accepting,
}: {
  order: MissionOrderResponse;
  locale: "fr" | "en";
  onAccept: () => void;
  onDecline: () => void;
  accepting: boolean;
}) {
  const t = useTranslations("missions");
  return (
    <Card>
      <CardContent padding="md">
        <div className="mb-2 flex items-center justify-between">
          <Badge tone="warning">{t("inbox.myAction")}</Badge>
          <Link
            href={`/mission-orders/${order.id}`}
            className="text-[10.5px] font-semibold uppercase tracking-wider text-ink-4 hover:text-orange-600"
          >
            {t("inbox.issuedAt")} →
          </Link>
        </div>
        <div className="mb-3 flex items-start gap-2">
          <MapPin className="mt-1 h-4 w-4 shrink-0 text-orange-500" />
          <div>
            <div className="text-[15px] font-bold text-ink">{order.destination}</div>
            <div className="text-[12px] text-ink-3">{order.objet}</div>
          </div>
        </div>
        <dl className="mb-4 grid grid-cols-2 gap-x-4 gap-y-2 text-[12px]">
          <div>
            <dt className="text-[10.5px] uppercase tracking-wider text-ink-4">
              {t("inbox.periodLabel")}
            </dt>
            <dd className="font-mono-tabular text-ink-2">
              {formatDate(order.dateDebut, { locale })} → {formatDate(order.dateFin, { locale })}
            </dd>
          </div>
          <div>
            <dt className="text-[10.5px] uppercase tracking-wider text-ink-4">
              {t("inbox.allowanceLabel")}
            </dt>
            <dd className="font-mono-tabular font-bold text-ink">
              {order.montantAvance != null
                ? formatNumber(Number(order.montantAvance), locale)
                : "—"}
            </dd>
          </div>
        </dl>
        <div className="flex gap-2">
          <Button type="button" variant="secondary" onClick={onDecline} className="flex-1">
            <XCircle className="h-4 w-4" />
            {t("inbox.decline")}
          </Button>
          <Button type="button" onClick={onAccept} disabled={accepting} className="flex-1">
            {accepting ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <CheckCircle2 className="h-4 w-4" />
            )}
            {t("inbox.accept")}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

function DeclineDialog({
  order,
  onClose,
  onConfirm,
  loading,
}: {
  order: MissionOrderResponse | null;
  onClose: () => void;
  onConfirm: (reason: string) => void;
  loading: boolean;
}) {
  const t = useTranslations("missions.detail.decline");
  const tCommon = useTranslations("common");
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ reason: string }>({ mode: "onChange" });

  React.useEffect(() => {
    if (!order) reset({ reason: "" });
  }, [order, reset]);

  return (
    <Dialog
      open={!!order}
      onClose={onClose}
      title={
        <span className="flex items-center gap-2 text-danger-600">
          <XCircle className="h-5 w-5" />
          {t("title")}
        </span>
      }
      subtitle={t("subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            variant="danger"
            disabled={!isValid || loading}
            onClick={handleSubmit((v) => onConfirm(v.reason))}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("confirm")}
          </Button>
        </>
      }
    >
      <Field label={t("reasonLabel")}>
        <Textarea
          rows={4}
          placeholder={t("reasonPlaceholder")}
          {...register("reason", { required: true, minLength: 5 })}
        />
      </Field>
    </Dialog>
  );
}
