"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  AlertTriangle,
  CalendarDays,
  ChevronLeft,
  FileText,
  Loader2,
  RefreshCw,
  XCircle,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label, Textarea } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatMoney } from "@/lib/format";
import type { ContractResponse, ContractStatusValue, ContractType } from "@/server/ksm/modules/employees";

/* ------------------------------------------------------------------ */
/*  Types                                                               */
/* ------------------------------------------------------------------ */

type EmployeeProfile = {
  id: string;
  actorDisplayName?: string | null;
  actorFirstName?: string | null;
  actorLastName?: string | null;
  actorPhotoUri?: string | null;
  matricule: string;
  dateEmbauche: string;
  departmentCode?: string | null;
};

type TimelineEvent = {
  type: string;
  date: string;
  title: string;
  detail: string;
};

/* ------------------------------------------------------------------ */
/*  Helpers                                                             */
/* ------------------------------------------------------------------ */

const CONTRACT_TYPE_TONE: Record<ContractType, "info" | "teal" | "violet" | "orange"> = {
  CDI: "info",
  CDD: "teal",
  STAGE: "violet",
  INTERIM: "orange",
};

const STATUS_TONE: Record<ContractStatusValue, "success" | "warning" | "danger" | "gray" | "info"> = {
  ACTIVE: "success",
  TRIAL: "warning",
  EXPIRED: "danger",
  TERMINATED: "danger",
  RENEWED: "info",
};

function shortRef(id: string) {
  return "CT-" + id.slice(0, 8).toUpperCase();
}

function seniorityYears(dateEmbauche: string): number {
  const hire = new Date(dateEmbauche);
  const now = new Date();
  return Math.max(0, Math.floor((now.getTime() - hire.getTime()) / (1000 * 60 * 60 * 24 * 365)));
}

function isExpiringSoon(dateFin: string | null | undefined): boolean {
  if (!dateFin) return false;
  const end = new Date(dateFin);
  const diff = (end.getTime() - Date.now()) / (1000 * 60 * 60 * 24);
  return diff >= 0 && diff <= 90;
}

/* ------------------------------------------------------------------ */
/*  RenewDialog                                                         */
/* ------------------------------------------------------------------ */

function RenewDialog({
  open,
  onClose,
  onConfirm,
  isPending,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: (newDateFin: string) => void;
  isPending: boolean;
}) {
  const t = useTranslations("contracts.detail.renew");
  const [value, setValue] = React.useState("");
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("dialogTitle")}
      size="sm"
      footer={
        <>
          <Button variant="secondary" size="sm" onClick={onClose} disabled={isPending}>
            Annuler
          </Button>
          <Button
            size="sm"
            onClick={() => onConfirm(value)}
            disabled={!value || isPending}
          >
            {isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : t("submit")}
          </Button>
        </>
      }
    >
      <Field label={t("newDateFin")}>
        <Input
          type="date"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          min={new Date().toISOString().slice(0, 10)}
        />
      </Field>
    </Dialog>
  );
}

/* ------------------------------------------------------------------ */
/*  TerminateDialog                                                     */
/* ------------------------------------------------------------------ */

function TerminateDialog({
  open,
  onClose,
  onConfirm,
  isPending,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: (motif: string) => void;
  isPending: boolean;
}) {
  const t = useTranslations("contracts.detail.terminate");
  const [value, setValue] = React.useState("");
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("dialogTitle")}
      size="sm"
      footer={
        <>
          <Button variant="secondary" size="sm" onClick={onClose} disabled={isPending}>
            Annuler
          </Button>
          <Button
            variant="danger"
            size="sm"
            onClick={() => onConfirm(value)}
            disabled={!value.trim() || isPending}
          >
            {isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : t("submit")}
          </Button>
        </>
      }
    >
      <div className="flex flex-col gap-4">
        <p className="flex items-center gap-2 rounded-[12px] bg-danger-50 px-4 py-3 text-[13px] text-danger-600">
          <AlertTriangle className="h-4 w-4 shrink-0" />
          {t("warning")}
        </p>
        <Field label={t("motif")}>
          <Textarea
            value={value}
            onChange={(e) => setValue(e.target.value)}
            placeholder={t("motifPlaceholder")}
          />
        </Field>
      </div>
    </Dialog>
  );
}

/* ------------------------------------------------------------------ */
/*  CharacteristicsCard                                                 */
/* ------------------------------------------------------------------ */

function CharacteristicsCard({
  contract,
  locale,
}: {
  contract: ContractResponse;
  locale: "fr" | "en";
}) {
  const t = useTranslations("contracts");
  const td = useTranslations("contracts.detail.characteristics");
  return (
    <Card>
      <CardHeader>
        <CardTitle>{td("title")}</CardTitle>
      </CardHeader>
      <CardContent>
        <dl className="grid grid-cols-2 gap-x-6 gap-y-4 text-[13px]">
          <div>
            <Label className="text-ink-3">{td("type")}</Label>
            <p className="mt-1 font-medium text-ink">
              {t(`contractType.${contract.type as ContractType}`)}
            </p>
          </div>
          <div>
            <Label className="text-ink-3">{td("status")}</Label>
            <p className="mt-1">
              <Badge tone={STATUS_TONE[contract.status]} showDot>
                {t(`contractStatus.${contract.status as ContractStatusValue}`)}
              </Badge>
            </p>
          </div>
          <div>
            <Label className="text-ink-3">{td("dateDebut")}</Label>
            <p className="mt-1 font-medium text-ink">{formatDate(contract.dateDebut, { locale })}</p>
          </div>
          <div>
            <Label className="text-ink-3">{td("dateFin")}</Label>
            <p className="mt-1 font-medium text-ink">
              {contract.dateFin ? formatDate(contract.dateFin, { locale }) : td("noEnd")}
            </p>
          </div>
          <div>
            <Label className="text-ink-3">{td("periodeEssai")}</Label>
            <p className="mt-1 font-medium text-ink">
              {contract.periodeEssai
                ? td("periodeEssaiDays", { days: contract.periodeEssai })
                : td("noPeriod")}
            </p>
          </div>
        </dl>
      </CardContent>
    </Card>
  );
}

/* ------------------------------------------------------------------ */
/*  SalaryCard                                                          */
/* ------------------------------------------------------------------ */

function SalaryCard({
  contract,
  dateEmbauche,
  locale,
}: {
  contract: ContractResponse;
  dateEmbauche?: string;
  locale: "fr" | "en";
}) {
  const td = useTranslations("contracts.detail.salary");
  const base = Number(contract.salaireBase);
  const benefits = Number(contract.avantagesNature ?? 0);
  const years = dateEmbauche ? seniorityYears(dateEmbauche) : 0;
  const seniority = Math.round(base * 0.08 * Math.min(years, 25));
  const transport = 40_000;
  const gross = base + seniority + transport + benefits;

  function row(label: string, amount: number, className?: string) {
    return (
      <tr className={className}>
        <td className="py-2 text-[13px] text-ink-2">{label}</td>
        <td className="py-2 text-right text-[13px] font-medium text-ink">
          {formatMoney(amount, { locale })} {td("xaf")}
        </td>
      </tr>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{td("title")}</CardTitle>
      </CardHeader>
      <CardContent>
        <table className="w-full">
          <tbody>
            {row(td("base"), base)}
            {row(td("seniority"), seniority)}
            {row(td("transport"), transport)}
            {benefits > 0 && row(td("benefits"), benefits)}
            <tr className="border-t border-line">
              <td className="pt-3 text-[13px] font-semibold text-ink">{td("gross")}</td>
              <td className="pt-3 text-right text-[14px] font-bold text-orange-500">
                {formatMoney(gross, { locale })} {td("xaf")}
              </td>
            </tr>
          </tbody>
        </table>
      </CardContent>
    </Card>
  );
}

/* ------------------------------------------------------------------ */
/*  HistoryCard                                                         */
/* ------------------------------------------------------------------ */

function HistoryCard({
  timeline,
  locale,
  isLoading,
}: {
  timeline: TimelineEvent[];
  locale: "fr" | "en";
  isLoading: boolean;
}) {
  const td = useTranslations("contracts.detail.history");
  const contractEvents = timeline.filter((e) => e.type === "CONTRACT");

  return (
    <Card>
      <CardHeader>
        <CardTitle>{td("title")}</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex justify-center py-6">
            <Loader2 className="h-5 w-5 animate-spin text-orange-500" />
          </div>
        ) : contractEvents.length === 0 ? (
          <p className="py-4 text-center text-[13px] text-ink-3">{td("empty")}</p>
        ) : (
          <ol className="relative ml-3 border-l border-line">
            {contractEvents.map((ev, i) => (
              <li key={i} className="mb-5 ml-5 last:mb-0">
                <span className="absolute -left-[5px] mt-1.5 h-2.5 w-2.5 rounded-full border-2 border-orange-400 bg-white" />
                <p className="text-[12px] text-ink-3">{formatDate(ev.date, { locale })}</p>
                <p className="text-[13px] font-semibold text-ink">{ev.title}</p>
                <p className="text-[12px] text-ink-2">{ev.detail}</p>
              </li>
            ))}
          </ol>
        )}
      </CardContent>
    </Card>
  );
}

/* ------------------------------------------------------------------ */
/*  ActionsCard                                                         */
/* ------------------------------------------------------------------ */

function ActionsCard({
  contract,
  canUpdate,
  onRenew,
  onTerminate,
}: {
  contract: ContractResponse;
  canUpdate: boolean;
  onRenew: () => void;
  onTerminate: () => void;
}) {
  const td = useTranslations("contracts.detail.actions");
  const isActive = contract.status === "ACTIVE" || contract.status === "TRIAL";

  if (!canUpdate) return null;

  return (
    <Card>
      <CardHeader>
        <CardTitle>{td("title")}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex flex-col gap-2">
          {isActive && (
            <button
              onClick={onRenew}
              className="flex w-full items-start gap-3 rounded-[12px] border border-line p-3 text-left transition hover:border-orange-300 hover:bg-orange-50"
            >
              <span className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-[9px] bg-info-50 text-info-600">
                <RefreshCw className="h-4 w-4" />
              </span>
              <span>
                <p className="text-[13px] font-semibold text-ink">{td("renew")}</p>
                <p className="text-[11.5px] text-ink-3">{td("renewHint")}</p>
              </span>
            </button>
          )}
          {isActive && (
            <button
              onClick={onTerminate}
              className="flex w-full items-start gap-3 rounded-[12px] border border-danger-200 p-3 text-left transition hover:bg-danger-50"
            >
              <span className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-[9px] bg-danger-50 text-danger-600">
                <XCircle className="h-4 w-4" />
              </span>
              <span>
                <p className="text-[13px] font-semibold text-danger-600">{td("terminate")}</p>
                <p className="text-[11.5px] text-ink-3">{td("terminateHint")}</p>
              </span>
            </button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

/* ------------------------------------------------------------------ */
/*  DocumentCard                                                        */
/* ------------------------------------------------------------------ */

function DocumentCard({ contract }: { contract: ContractResponse }) {
  const td = useTranslations("contracts.detail.document");
  return (
    <Card>
      <CardHeader>
        <CardTitle>{td("title")}</CardTitle>
      </CardHeader>
      <CardContent>
        {contract.documentFileId ? (
          <div className="flex items-center gap-3">
            <span className="flex h-9 w-9 items-center justify-center rounded-[10px] bg-info-50 text-info-600">
              <FileText className="h-4 w-4" />
            </span>
            <div className="flex-1 min-w-0">
              <p className="truncate text-[13px] font-medium text-ink">{td("attached")}</p>
              <p className="truncate text-[11.5px] text-ink-3">{contract.documentFileId}</p>
            </div>
          </div>
        ) : (
          <p className="text-[13px] text-ink-3">{td("noDoc")}</p>
        )}
      </CardContent>
    </Card>
  );
}

/* ------------------------------------------------------------------ */
/*  MetadataCard                                                        */
/* ------------------------------------------------------------------ */

function MetadataCard({ contract }: { contract: ContractResponse }) {
  const td = useTranslations("contracts.detail.metadata");
  return (
    <Card>
      <CardHeader>
        <CardTitle>{td("title")}</CardTitle>
      </CardHeader>
      <CardContent>
        <dl className="flex flex-col gap-2 text-[12.5px]">
          <div className="flex justify-between gap-2">
            <dt className="text-ink-3">{td("id")}</dt>
            <dd className="truncate font-mono text-[11px] text-ink-2">{contract.id.slice(0, 8)}…</dd>
          </div>
        </dl>
      </CardContent>
    </Card>
  );
}

/* ------------------------------------------------------------------ */
/*  ExpiryAlert                                                         */
/* ------------------------------------------------------------------ */

function ExpiryAlert({
  contract,
  onRenew,
}: {
  contract: ContractResponse;
  onRenew: () => void;
}) {
  const td = useTranslations("contracts.detail.expiry");
  if (!isExpiringSoon(contract.dateFin)) return null;
  return (
    <div className="rounded-[16px] border border-warning-300 bg-warning-50 p-4">
      <div className="flex items-start gap-3">
        <CalendarDays className="mt-0.5 h-4 w-4 shrink-0 text-warning-600" />
        <div className="flex-1">
          <p className="text-[13px] font-semibold text-warning-700">{td("title")}</p>
          <p className="mt-0.5 text-[12px] text-warning-600">{td("body")}</p>
        </div>
      </div>
      <Button variant="secondary" size="sm" className="mt-3 w-full" onClick={onRenew}>
        {td("cta")}
      </Button>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  ContractDetail (main)                                               */
/* ------------------------------------------------------------------ */

export function ContractDetail({
  contractId,
  employeeId,
  fromEmployee = false,
}: {
  contractId: string;
  employeeId: string;
  fromEmployee?: boolean;
}) {
  const t = useTranslations("contracts");
  const td = useTranslations("contracts.detail");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canUpdate = useCan("hrm:contract:update");

  const [showRenew, setShowRenew] = React.useState(false);
  const [showTerminate, setShowTerminate] = React.useState(false);

  const contractQ = useQuery({
    queryKey: ["hrm", "contracts", employeeId, contractId],
    queryFn: () =>
      apiFetch<ContractResponse>(
        `/api/hrm/employees/${employeeId}/contracts/${contractId}`,
      ),
    enabled: !!employeeId && !!contractId,
  });

  const employeeQ = useQuery({
    queryKey: ["hrm", "employees", employeeId, "profile"],
    queryFn: () => apiFetch<EmployeeProfile>(`/api/hrm/employees/${employeeId}/profile`),
    enabled: !!employeeId,
  });

  const timelineQ = useQuery({
    queryKey: ["hrm", "employees", employeeId, "timeline"],
    queryFn: () =>
      apiFetch<TimelineEvent[]>(`/api/hrm/employees/${employeeId}/timeline`),
    enabled: !!employeeId,
  });

  function invalidate() {
    queryClient.invalidateQueries({ queryKey: ["hrm", "contracts", employeeId, contractId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "contracts"] });
  }

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const renewM = useMutation({
    mutationFn: (newDateFin: string) =>
      apiFetch<ContractResponse>(
        `/api/hrm/employees/${employeeId}/contracts/${contractId}/renew`,
        { method: "POST", body: { newDateFin } },
      ),
    onSuccess: () => {
      toast.success(td("renew.success"));
      setShowRenew(false);
      invalidate();
    },
    onError: handleError,
  });

  const terminateM = useMutation({
    mutationFn: (motif: string) =>
      apiFetch<ContractResponse>(
        `/api/hrm/employees/${employeeId}/contracts/${contractId}/terminate`,
        { method: "PUT", body: { motif } },
      ),
    onSuccess: () => {
      toast.success(td("terminate.success"));
      setShowTerminate(false);
      invalidate();
    },
    onError: handleError,
  });

  /* Loading */
  if (contractQ.isLoading || !contractQ.data) {
    return (
      <div className="grid place-items-center py-24">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }

  if (contractQ.isError) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {contractQ.error instanceof BffApiError ? contractQ.error.message : td("notFound")}
      </div>
    );
  }

  const contract = contractQ.data;
  const employee = employeeQ.data;
  const timeline = timelineQ.data ?? [];
  const employeeName = employee?.actorDisplayName ?? employee?.matricule ?? "—";
  const ref = shortRef(contract.id);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/contracts" },
          { label: ref },
        ]}
        title={
          <span className="flex flex-wrap items-center gap-2">
            <span>{ref}</span>
            <Badge tone={CONTRACT_TYPE_TONE[contract.type as ContractType] as "info"} showDot={false}>
              {t(`contractType.${contract.type as ContractType}`)}
            </Badge>
            <Badge tone={STATUS_TONE[contract.status as ContractStatusValue]} showDot>
              {t(`contractStatus.${contract.status as ContractStatusValue}`)}
            </Badge>
          </span>
        }
        subtitle={
          employee && (
            <span className="flex items-center gap-2">
              <Avatar name={employeeName} size="sm" tone="orange" />
              <span>{employeeName}</span>
              {employee.departmentCode && (
                <span className="text-ink-4">· {employee.departmentCode}</span>
              )}
            </span>
          )
        }
        actions={
          <div className="flex items-center gap-2">
            <Link href={fromEmployee ? `/employees/${employeeId}` : "/contracts"}>
              <Button variant="secondary" size="sm">
                <ChevronLeft className="h-4 w-4" />
                {fromEmployee && employee
                  ? (employee.actorDisplayName ?? employee.matricule)
                  : td("back")}
              </Button>
            </Link>
          </div>
        }
      />

      {/* Body */}
      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Left column — 2/3 */}
        <div className="flex flex-col gap-6 lg:col-span-2">
          <CharacteristicsCard contract={contract} locale={locale} />
          <SalaryCard
            contract={contract}
            dateEmbauche={employee?.dateEmbauche}
            locale={locale}
          />
          <HistoryCard
            timeline={timeline}
            locale={locale}
            isLoading={timelineQ.isLoading}
          />
        </div>

        {/* Right sidebar — 1/3 */}
        <div className="flex flex-col gap-4">
          <ExpiryAlert contract={contract} onRenew={() => setShowRenew(true)} />
          <ActionsCard
            contract={contract}
            canUpdate={canUpdate}
            onRenew={() => setShowRenew(true)}
            onTerminate={() => setShowTerminate(true)}
          />
          <DocumentCard contract={contract} />
          <MetadataCard contract={contract} />
        </div>
      </div>

      {/* Dialogs */}
      <RenewDialog
        open={showRenew}
        onClose={() => setShowRenew(false)}
        onConfirm={(d) => renewM.mutate(d)}
        isPending={renewM.isPending}
      />
      <TerminateDialog
        open={showTerminate}
        onClose={() => setShowTerminate(false)}
        onConfirm={(m) => terminateM.mutate(m)}
        isPending={terminateM.isPending}
      />
    </>
  );
}
