"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  AlertTriangle,
  Banknote,
  CalendarClock,
  CalendarDays,
  ChevronLeft,
  Download,
  FileText,
  Hourglass,
  Loader2,
  RefreshCw,
  Wallet,
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
import { cn } from "@/lib/utils";
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

const STATUS_TONE: Record<ContractStatusValue, "success" | "warning" | "danger" | "gray" | "info"> = {
  ACTIVE: "success",
  TRIAL: "warning",
  EXPIRED: "gray",
  TERMINATED: "danger",
  RENEWED: "info",
};

const DAY = 86_400_000;

function shortRef(id: string) {
  return "CT-" + id.slice(0, 8).toUpperCase();
}

function formatDuration(from: string, to: Date, locale: "fr" | "en"): string {
  const start = new Date(from);
  let months = (to.getFullYear() - start.getFullYear()) * 12 + (to.getMonth() - start.getMonth());
  if (to.getDate() < start.getDate()) months -= 1;
  months = Math.max(0, months);
  const y = Math.floor(months / 12);
  const m = months % 12;
  if (locale === "fr") {
    const yl = y > 0 ? `${y} an${y > 1 ? "s" : ""}` : "";
    const ml = m > 0 ? `${m} mois` : "";
    return [yl, ml].filter(Boolean).join(" ") || "< 1 mois";
  }
  const yl = y > 0 ? `${y} yr` : "";
  const ml = m > 0 ? `${m} mo` : "";
  return [yl, ml].filter(Boolean).join(" ") || "< 1 mo";
}

type Progress = { percent: number; daysRemaining: number; ended: boolean };

function fixedTermProgress(c: ContractResponse): Progress | null {
  if (!c.dateFin) return null;
  const start = new Date(c.dateDebut).getTime();
  const end = new Date(c.dateFin).getTime();
  const now = Date.now();
  if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) return null;
  const percent = Math.min(100, Math.max(0, Math.round(((now - start) / (end - start)) * 100)));
  const daysRemaining = Math.ceil((end - now) / DAY);
  return { percent, daysRemaining, ended: now >= end };
}

function trialProgress(c: ContractResponse): Progress | null {
  if (!c.periodeEssai || c.periodeEssai <= 0) return null;
  if (c.status === "TERMINATED" || c.status === "EXPIRED") return null;
  const start = new Date(c.dateDebut).getTime();
  const end = start + c.periodeEssai * DAY;
  const now = Date.now();
  if (!Number.isFinite(start)) return null;
  const percent = Math.min(100, Math.max(0, Math.round(((now - start) / (end - start)) * 100)));
  const daysRemaining = Math.ceil((end - now) / DAY);
  return { percent, daysRemaining, ended: now >= end };
}

function isExpiringSoon(dateFin: string | null | undefined): boolean {
  if (!dateFin) return false;
  const diff = (new Date(dateFin).getTime() - Date.now()) / DAY;
  return diff >= 0 && diff <= 90;
}

function contractDurationTo(c: ContractResponse): Date {
  if (c.dateFin && new Date(c.dateFin).getTime() < Date.now()) return new Date(c.dateFin);
  return new Date();
}

/* ------------------------------------------------------------------ */
/*  Hero tile + progress bar                                           */
/* ------------------------------------------------------------------ */

function HeroTile({
  icon: Icon,
  label,
  value,
  sub,
}: {
  icon: typeof Wallet;
  label: string;
  value: React.ReactNode;
  sub?: React.ReactNode;
}) {
  return (
    <div className="rounded-[13px] border border-line bg-white/80 p-3.5 backdrop-blur-sm">
      <div className="mb-1.5 flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-[0.04em] text-ink-3">
        <Icon className="h-3.5 w-3.5" />
        {label}
      </div>
      <div className="font-display text-[19px] font-extrabold leading-tight tracking-tight text-ink tabular-nums">
        {value}
      </div>
      {sub && <div className="mt-0.5 text-[11px] text-ink-3">{sub}</div>}
    </div>
  );
}

function ProgressBar({
  label,
  progress,
  tone,
  remainingLabel,
  endedLabel,
}: {
  label: string;
  progress: Progress;
  tone: "orange" | "amber";
  remainingLabel: (days: number) => string;
  endedLabel: string;
}) {
  const barColor = tone === "orange" ? "bg-grad-orange" : "bg-warning-500";
  return (
    <div className="rounded-[13px] border border-line bg-white/80 p-3.5 backdrop-blur-sm">
      <div className="mb-2 flex items-center justify-between">
        <span className="text-[12px] font-semibold text-ink-2">{label}</span>
        <span
          className={cn(
            "font-mono-tabular text-[11.5px] font-bold",
            progress.ended ? "text-ink-3" : tone === "amber" ? "text-warning-700" : "text-orange-600",
          )}
        >
          {progress.ended ? endedLabel : remainingLabel(progress.daysRemaining)}
        </span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-bg-dim">
        <div
          className={cn("h-full rounded-full transition-all", barColor)}
          style={{ width: `${progress.percent}%` }}
        />
      </div>
      <div className="mt-1 text-right font-mono-tabular text-[10.5px] text-ink-4">
        {progress.percent}%
      </div>
    </div>
  );
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
  const tCommon = useTranslations("common");
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
            {tCommon("actions.cancel")}
          </Button>
          <Button size="sm" onClick={() => onConfirm(value)} disabled={!value || isPending}>
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
  const tCommon = useTranslations("common");
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
            {tCommon("actions.cancel")}
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
            <p className="mt-1 font-medium text-ink">{t(`contractType.${contract.type as ContractType}`)}</p>
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
          {contract.motifFin && (
            <div>
              <Label className="text-ink-3">{td("motifFin")}</Label>
              <p className="mt-1 font-medium text-ink">{contract.motifFin}</p>
            </div>
          )}
        </dl>
      </CardContent>
    </Card>
  );
}

/* ------------------------------------------------------------------ */
/*  RemunerationCard — REAL contractual figures only                   */
/* ------------------------------------------------------------------ */

function RemunerationCard({
  contract,
  locale,
}: {
  contract: ContractResponse;
  locale: "fr" | "en";
}) {
  const td = useTranslations("contracts.detail.salary");
  const base = Number(contract.salaireBase) || 0;
  const benefits = Number(contract.avantagesNature ?? 0) || 0;
  const monthly = base + benefits;
  const annual = monthly * 12;

  return (
    <Card>
      <CardHeader>
        <CardTitle>{td("title")}</CardTitle>
      </CardHeader>
      <CardContent>
        <table className="w-full">
          <tbody>
            <tr>
              <td className="py-2 text-[13px] text-ink-2">{td("base")}</td>
              <td className="py-2 text-right font-mono-tabular text-[13px] font-medium text-ink">
                {formatMoney(base, { locale })} {td("xaf")}
              </td>
            </tr>
            {benefits > 0 && (
              <tr>
                <td className="py-2 text-[13px] text-ink-2">{td("benefits")}</td>
                <td className="py-2 text-right font-mono-tabular text-[13px] font-medium text-ink">
                  {formatMoney(benefits, { locale })} {td("xaf")}
                </td>
              </tr>
            )}
            <tr className="border-t border-line">
              <td className="pt-3 text-[13px] font-semibold text-ink">{td("monthlyGross")}</td>
              <td className="pt-3 text-right font-mono-tabular text-[15px] font-bold text-orange-500">
                {formatMoney(monthly, { locale })} {td("xaf")}
              </td>
            </tr>
            <tr>
              <td className="pt-1 text-[12px] text-ink-3">{td("annualGross")}</td>
              <td className="pt-1 text-right font-mono-tabular text-[12.5px] font-semibold text-ink-2">
                {formatMoney(annual, { locale })} {td("xaf")}
              </td>
            </tr>
          </tbody>
        </table>
        <p className="mt-4 flex items-start gap-2 rounded-[10px] bg-info-50 px-3 py-2.5 text-[11.5px] text-info-700">
          <Wallet className="mt-0.5 h-3.5 w-3.5 shrink-0" />
          {td("payrollNote")}
        </p>
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
/*  DocumentCard — real download link                                  */
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
          <a
            href={`/api/files/${contract.documentFileId}`}
            target="_blank"
            rel="noopener noreferrer"
            className="group flex items-center gap-3 rounded-[12px] border border-line p-3 transition hover:border-orange-300 hover:bg-orange-50"
          >
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-[10px] bg-info-50 text-info-600">
              <FileText className="h-5 w-5" />
            </span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-[13px] font-semibold text-ink">{td("attached")}</p>
              <p className="truncate text-[11.5px] text-ink-3">{td("clickToDownload")}</p>
            </div>
            <span className="grid h-8 w-8 shrink-0 place-items-center rounded-[9px] border border-line bg-white text-ink-3 transition group-hover:border-orange-300 group-hover:text-orange-600">
              <Download className="h-4 w-4" />
            </span>
          </a>
        ) : (
          <p className="rounded-[10px] border border-dashed border-line bg-bg-soft px-3 py-4 text-center text-[13px] text-ink-3">
            {td("noDoc")}
          </p>
        )}
      </CardContent>
    </Card>
  );
}

/* ------------------------------------------------------------------ */
/*  ExpiryAlert                                                         */
/* ------------------------------------------------------------------ */

function ExpiryAlert({ contract, onRenew }: { contract: ContractResponse; onRenew: () => void }) {
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
  const th = useTranslations("contracts.detail.hero");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canUpdate = useCan("hrm:contract:update");

  const [showRenew, setShowRenew] = React.useState(false);
  const [showTerminate, setShowTerminate] = React.useState(false);

  const contractQ = useQuery({
    queryKey: ["hrm", "contracts", employeeId, contractId],
    queryFn: () =>
      apiFetch<ContractResponse>(`/api/hrm/employees/${employeeId}/contracts/${contractId}`),
    enabled: !!employeeId && !!contractId,
  });

  const employeeQ = useQuery({
    queryKey: ["hrm", "employees", employeeId, "profile"],
    queryFn: () => apiFetch<EmployeeProfile>(`/api/hrm/employees/${employeeId}/profile`),
    enabled: !!employeeId,
  });

  const timelineQ = useQuery({
    queryKey: ["hrm", "employees", employeeId, "timeline"],
    queryFn: () => apiFetch<TimelineEvent[]>(`/api/hrm/employees/${employeeId}/timeline`),
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
      apiFetch<ContractResponse>(`/api/hrm/employees/${employeeId}/contracts/${contractId}/renew`, {
        method: "POST",
        body: { newDateFin },
      }),
    onSuccess: () => {
      toast.success(td("renew.success"));
      setShowRenew(false);
      invalidate();
    },
    onError: handleError,
  });

  const terminateM = useMutation({
    mutationFn: (motif: string) =>
      apiFetch<ContractResponse>(`/api/hrm/employees/${employeeId}/contracts/${contractId}/terminate`, {
        method: "PUT",
        body: { motif },
      }),
    onSuccess: () => {
      toast.success(td("terminate.success"));
      setShowTerminate(false);
      invalidate();
    },
    onError: handleError,
  });

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
  const base = Number(contract.salaireBase) || 0;
  const isActive = contract.status === "ACTIVE" || contract.status === "TRIAL";

  const ft = fixedTermProgress(contract);
  const trial = trialProgress(contract);
  const durationTo = contractDurationTo(contract);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title"), href: "/contracts" }, { label: ref }]}
        title={t("title")}
        subtitle={td("subtitlePage")}
        actions={
          <Link href={fromEmployee ? `/employees/${employeeId}` : "/contracts"}>
            <Button variant="secondary" size="sm">
              <ChevronLeft className="h-4 w-4" />
              {fromEmployee && employee ? (employee.actorDisplayName ?? employee.matricule) : td("back")}
            </Button>
          </Link>
        }
      />

      {/* Hero card */}
      <div
        className="relative mb-6 overflow-hidden rounded-[20px] border border-orange-200 p-[26px] shadow-sm-brand"
        style={{ background: "linear-gradient(135deg, #FFF8F0 0%, #FFFFFF 58%)" }}
      >
        <span
          aria-hidden="true"
          className="pointer-events-none absolute -right-20 -top-20 h-64 w-64 rounded-full bg-grad-orange opacity-[0.07] blur-2xl"
        />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0 flex-1">
            <div className="mb-2 flex flex-wrap items-center gap-2">
              <span className="rounded-full bg-orange-50 px-2.5 py-0.5 font-mono-tabular text-[12px] font-semibold text-orange-700">
                {ref}
              </span>
              <Badge tone={STATUS_TONE[contract.status]} showDot>
                {t(`contractStatus.${contract.status as ContractStatusValue}`)}
              </Badge>
            </div>
            <h1 className="font-display text-[26px] font-extrabold leading-tight tracking-tight text-ink">
              {t(`contractType.${contract.type as ContractType}`)}
            </h1>
            {employee && (
              <Link
                href={`/employees/${employeeId}`}
                className="mt-2 inline-flex items-center gap-2 rounded-full border border-line bg-white/70 py-1 pl-1 pr-3 transition hover:border-orange-300"
              >
                <Avatar name={employeeName} size="sm" tone="orange" />
                <span className="text-[13px] font-semibold text-ink">{employeeName}</span>
                <span className="font-mono-tabular text-[11px] text-ink-3">{employee.matricule}</span>
                {employee.departmentCode && (
                  <span className="text-[11px] text-ink-4">· {employee.departmentCode}</span>
                )}
              </Link>
            )}
          </div>

          {canUpdate && isActive && (
            <div className="flex shrink-0 flex-col gap-2 sm:flex-row">
              <Button type="button" variant="secondary" onClick={() => setShowRenew(true)}>
                <RefreshCw className="h-4 w-4" />
                {td("actions.renew")}
              </Button>
              <Button type="button" variant="danger" onClick={() => setShowTerminate(true)}>
                <XCircle className="h-4 w-4" />
                {td("actions.terminate")}
              </Button>
            </div>
          )}
        </div>

        {/* Hero tiles */}
        <div className="relative mt-6 grid grid-cols-2 gap-3.5 md:grid-cols-4">
          <HeroTile
            icon={Banknote}
            label={th("salaryLabel")}
            value={formatMoney(base, { locale, withCurrency: false })}
            sub={`XAF · ${th("monthly")}`}
          />
          <HeroTile
            icon={CalendarDays}
            label={th("startLabel")}
            value={formatDate(contract.dateDebut, { locale })}
          />
          <HeroTile
            icon={CalendarClock}
            label={th("endLabel")}
            value={contract.dateFin ? formatDate(contract.dateFin, { locale }) : "∞"}
            sub={contract.dateFin ? undefined : th("noEnd")}
          />
          <HeroTile
            icon={Hourglass}
            label={th("durationLabel")}
            value={formatDuration(contract.dateDebut, durationTo, locale)}
          />
        </div>

        {/* Progress bars */}
        {(ft || trial) && (
          <div className="relative mt-3.5 grid grid-cols-1 gap-3.5 md:grid-cols-2">
            {trial && (
              <ProgressBar
                label={th("trialTitle")}
                progress={trial}
                tone="amber"
                remainingLabel={(d) => th("daysRemaining", { days: d })}
                endedLabel={th("trialEnded")}
              />
            )}
            {ft && (
              <ProgressBar
                label={th("contractTitle")}
                progress={ft}
                tone="orange"
                remainingLabel={(d) => th("daysRemaining", { days: d })}
                endedLabel={th("contractEnded")}
              />
            )}
          </div>
        )}
      </div>

      {/* Body */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Left column — 2/3 */}
        <div className="flex flex-col gap-6 lg:col-span-2">
          <CharacteristicsCard contract={contract} locale={locale} />
          <RemunerationCard contract={contract} locale={locale} />
          <HistoryCard timeline={timeline} locale={locale} isLoading={timelineQ.isLoading} />
        </div>

        {/* Right sidebar — 1/3 */}
        <div className="flex flex-col gap-4">
          <ExpiryAlert contract={contract} onRenew={() => setShowRenew(true)} />
          <DocumentCard contract={contract} />
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
