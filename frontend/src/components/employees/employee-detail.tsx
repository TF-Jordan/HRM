"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Banknote,
  Calendar,
  CalendarRange,
  ChevronLeft,
  Loader2,
  Pause,
  Pencil,
  Play,
  Plus,
  StopCircle,
  UserRound,
  Users,
} from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { useCan } from "@/hooks/use-can";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Column, DataTable } from "@/components/ui/data-table";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label, Textarea } from "@/components/ui/input";
import { IconTile } from "@/components/ui/icon-tile";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { employeeStatusTone } from "@/lib/employee-status";
import { formatDate, formatMoney } from "@/lib/format";
import { initials } from "@/lib/utils";
import type {
  ContractResponse,
  DependentResponse,
  EmployeeResponse,
  LeaveBalanceResponse,
} from "@/server/ksm/modules/employees";

type Tab = "overview" | "contracts" | "dependents" | "leaves";

import type {
  EmployeeProfileResponse,
  TimelineEventResponse,
} from "@/server/ksm/modules/employee-profile";
import {
  Briefcase,
  FileText,
  GraduationCap,
  Mail,
  Phone,
  Star,
} from "lucide-react";
import { formatDateLong } from "@/lib/format";
import { EmployeeDocuments } from "@/components/employees/employee-documents";
import { EmployeeSkills } from "@/components/employees/employee-skills";
import { AddContractDialog } from "@/components/contracts/add-contract-dialog";

export function EmployeeDetail({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tDetail = useTranslations("employees.detail");
  const tHero = useTranslations("employees.detail.hero");
  const tStatus = useTranslations("employees.status");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();
  const router = useRouter();
  const [tab, setTab] = React.useState<Tab>("overview");
  const [modal, setModal] = React.useState<null | "suspend" | "terminate" | "reactivate" | "addContract" | "addDependent">(null);

  const canSuspend = useCan("hrm:employee:suspend");
  const canTerminate = useCan("hrm:employee:terminate");
  const canReactivate = useCan("hrm:employee:reactivate");
  const canEdit = useCan("hrm:employee:update");
  const canAddContract = useCan("hrm:contract:create");
  const canAddDependent = useCan("hrm:dependent:create");

  const employeeQuery = useQuery({
    queryKey: ["hrm", "employee", employeeId],
    queryFn: () => apiFetch<EmployeeResponse>(`/api/hrm/employees/${employeeId}`),
  });
  const e = employeeQuery.data;

  // Hero stat tiles — fed by real data from the related collections.
  const heroContracts = useQuery({
    queryKey: ["hrm", "contracts", employeeId],
    queryFn: () => apiFetch<ContractResponse[]>(`/api/hrm/employees/${employeeId}/contracts`),
  });
  const heroDependents = useQuery({
    queryKey: ["hrm", "dependents", employeeId],
    queryFn: () => apiFetch<DependentResponse[]>(`/api/hrm/employees/${employeeId}/dependents`),
  });
  const heroBalances = useQuery({
    queryKey: ["hrm", "leave-balances", employeeId, new Date().getFullYear()],
    queryFn: () =>
      apiFetch<LeaveBalanceResponse[]>(
        `/api/hrm/employees/${employeeId}/leave-balances?annee=${new Date().getFullYear()}`,
      ),
  });

  const activeContract = (heroContracts.data ?? []).find((c) => c.status === "ACTIVE");
  const annualBalance = (heroBalances.data ?? []).find((b) => b.type === "ANNUAL");

  // Performance + trainings tiles (design hero) — gated by permission so roles
  // without review/training read access don't trigger 403s.
  const canReadReviews = useCan("hrm:review:read");
  const canReadTrainings = useCan("hrm:training:read");
  const heroReviews = useQuery({
    queryKey: ["hrm", "employee", "reviews", employeeId],
    enabled: canReadReviews,
    queryFn: () =>
      apiFetch<{ noteGlobale?: number | string | null; periode: string; status: string }[]>(
        `/api/hrm/employees/${employeeId}/reviews`,
      ),
  });
  const heroEnrollments = useQuery({
    queryKey: ["hrm", "employee", "enrollments", employeeId],
    enabled: canReadTrainings,
    queryFn: () =>
      apiFetch<{ status: string }[]>(`/api/hrm/employees/${employeeId}/enrollments`),
  });

  const latestReview = (heroReviews.data ?? [])
    .filter((r) => r.noteGlobale != null)
    .at(-1);
  const completedTrainings = (heroEnrollments.data ?? []).filter(
    (e) => e.status === "COMPLETED",
  ).length;
  const ongoingTrainings = (heroEnrollments.data ?? []).filter(
    (e) => e.status === "ENROLLED",
  ).length;

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "employee", employeeId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "employees"] });
  }, [queryClient, employeeId]);

  function handleApiError(cause: unknown) {
    if (cause instanceof BffApiError) {
      toast.error(cause.message);
    } else {
      toast.error(tErrors("unknown"));
    }
  }

  /* Suspend */
  const suspendMutation = useMutation({
    mutationFn: (reason: string) =>
      apiFetch(`/api/hrm/employees/${employeeId}/suspend`, { method: "POST", body: { reason } }),
    onSuccess: () => {
      toast.success(t("actions.suspend.success"));
      invalidate();
      setModal(null);
    },
    onError: handleApiError,
  });

  /* Terminate */
  const terminateMutation = useMutation({
    mutationFn: (body: { terminationDate: string; reason: string }) =>
      apiFetch(`/api/hrm/employees/${employeeId}/terminate`, { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("actions.terminate.success"));
      invalidate();
      setModal(null);
    },
    onError: handleApiError,
  });

  /* Reactivate */
  const reactivateMutation = useMutation({
    mutationFn: () => apiFetch(`/api/hrm/employees/${employeeId}/reactivate`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("actions.reactivate.success"));
      invalidate();
      setModal(null);
    },
    onError: handleApiError,
  });

  if (employeeQuery.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (employeeQuery.error || !e) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {employeeQuery.error instanceof BffApiError ? employeeQuery.error.message : "Not found"}
      </div>
    );
  }

  const isActive = e.status === "ACTIVE";
  const isSuspended = e.status === "SUSPENDED";
  const isTerminated = e.status === "TERMINATED";

  const avatarTone = (["orange", "blue", "green", "violet", "amber", "teal"] as const)[
    e.id.charCodeAt(0) % 6
  ];

  return (
    <>
      {/* Back breadcrumb */}
      <div className="mb-3.5 flex items-center gap-2">
        <Link href="/employees">
          <Button type="button" variant="ghost" size="sm">
            <ChevronLeft className="h-3.5 w-3.5" />
            {tCommon("actions.back")}
          </Button>
        </Link>
        <span className="text-[12px] text-ink-3">
          {t("list.title")} / {e.actorDisplayName ?? e.matricule}
        </span>
      </div>

      {/* Hero card — warm gradient */}
      <div
        className="relative mb-5 overflow-hidden rounded-[20px] border border-orange-200 p-[26px] shadow-sm-brand"
        style={{ background: "linear-gradient(135deg, #FFFAF2 0%, #FFFFFF 60%)" }}
      >
        <span
          aria-hidden="true"
          className="pointer-events-none absolute -right-16 -top-16 h-60 w-60 rounded-full bg-grad-orange opacity-[0.08] blur-2xl"
        />
        <div className="relative flex flex-wrap items-start gap-4">
          <div className="relative">
            <Avatar
              name={e.actorDisplayName ?? e.matricule}
              initials={initials(e.actorDisplayName ?? e.matricule, 2)}
              size="xl"
              tone={avatarTone}
            />
            {isActive && (
              <span className="absolute bottom-0 right-0 h-4.5 w-4.5 rounded-full border-[3px] border-white bg-success-500" />
            )}
          </div>
          <div className="min-w-0 flex-1">
            <div className="mb-1 flex flex-wrap items-center gap-2">
              <span className="rounded-full bg-orange-50 px-2 py-0.5 font-mono-tabular text-[11px] text-orange-700">
                {e.matricule}
              </span>
              <Badge tone={employeeStatusTone(e.status)}>{tStatus(e.status)}</Badge>
            </div>
            <h1 className="font-display text-[30px] font-extrabold leading-tight tracking-tight text-ink">
              {e.actorDisplayName ?? e.matricule}
            </h1>
            <p className="mt-1 text-[14px] text-ink-2">
              {e.departmentCode ?? "—"}
              {e.echelon ? ` · ${tDetail("identity.echelon")} ${e.echelon}` : ""}
            </p>
            <div className="mt-4 flex flex-wrap gap-4 text-[12.5px] text-ink-3">
              <span className="flex items-center gap-2">
                <Calendar className="h-3.5 w-3.5" />
                {tDetail("identity.hireDate")} · {formatDate(e.dateEmbauche, { locale: "fr" })}
              </span>
              {e.numCnps && (
                <span className="flex items-center gap-2">
                  <UserRound className="h-3.5 w-3.5" />
                  CNPS · {e.numCnps}
                </span>
              )}
              <span className="flex items-center gap-2">
                <Users className="h-3.5 w-3.5" />
                {tDetail("identity.categorie")} {e.categorie}
              </span>
            </div>
          </div>
          <div className="flex flex-col gap-2">
            {canEdit && !isTerminated && (
              <Button
                type="button"
                variant="secondary"
                onClick={() => router.push(`/employees/${employeeId}/edit`)}
              >
                <Pencil className="h-4 w-4" />
                {tDetail("actions.edit")}
              </Button>
            )}
            {canSuspend && isActive && (
              <Button type="button" variant="secondary" onClick={() => setModal("suspend")}>
                <Pause className="h-4 w-4" />
                {tDetail("actions.suspend")}
              </Button>
            )}
            {canReactivate && isSuspended && (
              <Button type="button" variant="secondary" onClick={() => setModal("reactivate")}>
                <Play className="h-4 w-4" />
                {tDetail("actions.reactivate")}
              </Button>
            )}
            {canTerminate && !isTerminated && (
              <Button type="button" variant="danger" onClick={() => setModal("terminate")}>
                <StopCircle className="h-4 w-4" />
                {tDetail("actions.terminate")}
              </Button>
            )}
          </div>
        </div>

        {/* Stat tiles — real data */}
        <div className="relative mt-6 grid grid-cols-2 gap-3.5 md:grid-cols-4">
          <HeroTile
            label={tHero("salary")}
            value={activeContract ? formatMoney(Number(activeContract.salaireBase), { locale: "fr", withCurrency: false }) : "—"}
            unit={activeContract ? "XAF / mois" : tHero("noContract")}
          />
          <HeroTile
            label={tHero("leaveBalance")}
            value={annualBalance ? Number(annualBalance.soldeRestant).toFixed(1) : "—"}
            unit={
              annualBalance
                ? `${tHero("daysUnit")} · ${tHero("acquired")} ${Number(annualBalance.acquis).toFixed(1)}`
                : tHero("daysUnit")
            }
          />
          <HeroTile
            label={tHero("performance")}
            value={latestReview?.noteGlobale != null ? Number(latestReview.noteGlobale).toFixed(1) : "—"}
            unit={latestReview ? `/ 5 · ${latestReview.periode}` : tHero("noReview")}
          />
          <HeroTile
            label={tHero("trainings")}
            value={String(completedTrainings)}
            unit={
              ongoingTrainings > 0
                ? `${tHero("completed")} · ${ongoingTrainings} ${tHero("ongoing")}`
                : tHero("completed")
            }
          />
        </div>
      </div>

      {/* Tabs */}
      <div className="mb-6 flex flex-wrap gap-2 border-b border-line-soft pb-2">
        {(["overview", "contracts", "dependents", "leaves"] as const).map((tabKey) => (
          <button
            key={tabKey}
            type="button"
            onClick={() => {
              if (tabKey === "contracts") {
                if (activeContract) {
                  router.push(`/contracts/${activeContract.id}?employeeId=${employeeId}&from=employee`);
                } else if (canAddContract && !isTerminated) {
                  setModal("addContract");
                }
              } else {
                setTab(tabKey);
              }
            }}
            className={`relative px-3.5 py-2 text-[13.5px] font-medium tracking-tight transition-colors ${
              tabKey !== "contracts" && tab === tabKey
                ? "text-orange-600 after:absolute after:bottom-[-9px] after:left-2 after:right-2 after:h-[3px] after:rounded-full after:bg-grad-orange"
                : "text-ink-3 hover:text-ink"
            }`}
          >
            {tDetail(`tabs.${tabKey}`)}
          </button>
        ))}
      </div>

      {tab === "overview" && <OverviewTab employeeId={employeeId} />}
      {tab === "dependents" && (
        <DependentsTab
          employeeId={employeeId}
          canAdd={canAddDependent}
          onAdd={() => setModal("addDependent")}
        />
      )}
      {tab === "leaves" && <LeavesTab employeeId={employeeId} />}

      {/* Modals */}
      <SuspendModal
        open={modal === "suspend"}
        onClose={() => setModal(null)}
        onConfirm={(reason) => suspendMutation.mutate(reason)}
        loading={suspendMutation.isPending}
      />
      <TerminateModal
        open={modal === "terminate"}
        onClose={() => setModal(null)}
        onConfirm={(body) => terminateMutation.mutate(body)}
        loading={terminateMutation.isPending}
      />
      <ReactivateModal
        open={modal === "reactivate"}
        onClose={() => setModal(null)}
        onConfirm={() => reactivateMutation.mutate()}
        loading={reactivateMutation.isPending}
      />
      <AddContractDialog
        open={modal === "addContract"}
        onClose={() => setModal(null)}
        employeeId={employeeId}
        employeeName={e.actorDisplayName ?? e.matricule}
        onSuccess={() => {
          setModal(null);
          queryClient.invalidateQueries({ queryKey: ["hrm", "contracts", employeeId] });
        }}
      />
      <AddDependentModal
        open={modal === "addDependent"}
        onClose={() => setModal(null)}
        employeeId={employeeId}
        onSuccess={() => {
          setModal(null);
          queryClient.invalidateQueries({ queryKey: ["hrm", "dependents", employeeId] });
        }}
      />
    </>
  );
}

/* ============================== Tabs ============================== */

function IdentityTab({ employee }: { employee: EmployeeResponse }) {
  const t = useTranslations("employees");
  const tIdent = useTranslations("employees.detail.identity");
  const tCommon = useTranslations("common");
  type IconType = typeof UserRound;
  const items: Array<{ label: string; value: string; icon?: IconType }> = [
    { label: tIdent("matricule"), value: employee.matricule, icon: UserRound },
    { label: tIdent("department"), value: employee.departmentCode ?? "—", icon: Users },
    {
      label: tIdent("categorie") + " · " + tIdent("echelon"),
      value: `${employee.categorie} / ${employee.echelon ?? "—"}`,
    },
    { label: tIdent("hireDate"), value: formatDate(employee.dateEmbauche, { locale: "fr" }), icon: Calendar },
    { label: tIdent("numCnps"), value: employee.numCnps ?? "—" },
    {
      label: tIdent("modePaiement"),
      value: employee.modePaiement ? t(`paymentChannel.${employee.modePaiement}`) : "—",
      icon: Banknote,
    },
    { label: tIdent("compteBancaire"), value: employee.compteBancaire ?? "—" },
    {
      label: tIdent("mobileMoney"),
      value: employee.numMobileMoney
        ? `${employee.operateurMm ?? "—"} · ${employee.numMobileMoney}`
        : "—",
    },
    { label: tIdent("actor"), value: employee.actorId.slice(0, 13) + "…" },
  ];
  return (
    <Card>
      <CardContent padding="lg">
        <dl className="grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-3">
          {items.map((item) => (
            <div key={item.label} className="flex items-start gap-3">
              {item.icon && <IconTile icon={item.icon} tone="gray" size="sm" />}
              <div className="flex flex-1 flex-col">
                <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">{item.label}</Label>
                <span className="mt-0.5 break-words text-[14px] font-medium text-ink">{item.value}</span>
              </div>
            </div>
          ))}
        </dl>
        <p className="mt-6 text-[12px] text-ink-4">{tCommon("actions.view")} · UC-02</p>
      </CardContent>
    </Card>
  );
}

function DependentsTab({
  employeeId,
  canAdd,
  onAdd,
}: {
  employeeId: string;
  canAdd: boolean;
  onAdd: () => void;
}) {
  const tDeps = useTranslations("employees.detail.dependents");
  const { data, isLoading, error } = useQuery({
    queryKey: ["hrm", "dependents", employeeId],
    queryFn: () => apiFetch<DependentResponse[]>(`/api/hrm/employees/${employeeId}/dependents`),
  });

  const columns: Column<DependentResponse>[] = [
    {
      key: "name",
      header: tDeps("columns.name"),
      cell: (d) => <span className="font-semibold text-ink">{d.prenom} {d.nom}</span>,
    },
    {
      key: "birthDate",
      header: tDeps("columns.birthDate"),
      cell: (d) => formatDate(d.dateNaissance, { locale: "fr" }),
    },
    {
      key: "relationship",
      header: tDeps("columns.relationship"),
      cell: (d) => <Badge tone="info">{d.lienParente}</Badge>,
    },
  ];

  return (
    <>
      {canAdd && (
        <div className="mb-3 flex justify-end">
          <Button type="button" onClick={onAdd}>
            <Plus className="h-4 w-4" />
            {tDeps("addNew")}
          </Button>
        </div>
      )}
      {isLoading ? (
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      ) : error || !data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {error instanceof BffApiError ? error.message : "Failed"}
        </div>
      ) : (
        <DataTable columns={columns} data={data} rowKey={(d) => d.id} empty={tDeps("noDependents")} />
      )}
    </>
  );
}

function LeavesTab({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tL = useTranslations("employees.detail.leaves");
  const [year] = React.useState(new Date().getFullYear());
  const { data, isLoading, error } = useQuery({
    queryKey: ["hrm", "leave-balances", employeeId, year],
    queryFn: () =>
      apiFetch<LeaveBalanceResponse[]>(
        `/api/hrm/employees/${employeeId}/leave-balances?annee=${year}`,
      ),
  });

  const columns: Column<LeaveBalanceResponse>[] = [
    {
      key: "type",
      header: tL("columns.type"),
      cell: (b) => <span className="font-semibold text-ink">{t(`leaveType.${b.type}`)}</span>,
    },
    {
      key: "acquis",
      header: tL("columns.acquired"),
      cell: (b) => <span className="font-mono-tabular text-ink-2">{Number(b.acquis).toFixed(1)}</span>,
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "pris",
      header: tL("columns.taken"),
      cell: (b) => <span className="font-mono-tabular text-ink-2">{Number(b.pris).toFixed(1)}</span>,
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "soldeRestant",
      header: tL("columns.remaining"),
      cell: (b) => (
        <span className="font-mono-tabular font-bold text-orange-700">
          {Number(b.soldeRestant).toFixed(1)}
        </span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
  ];

  return (
    <>
      <div className="mb-3 flex items-center gap-2">
        <CalendarRange className="h-4 w-4 text-ink-3" />
        <span className="font-mono-tabular text-[12.5px] uppercase tracking-wider text-ink-3">
          {year}
        </span>
      </div>
      {isLoading ? (
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      ) : error || !data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {error instanceof BffApiError ? error.message : "Failed"}
        </div>
      ) : (
        <DataTable columns={columns} data={data} rowKey={(b) => b.id} empty={tL("noBalances")} />
      )}
    </>
  );
}

/* ============================== Modals ============================== */

function SuspendModal({
  open,
  onClose,
  onConfirm,
  loading,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
  loading: boolean;
}) {
  const t = useTranslations("employees.actions.suspend");
  const tCommon = useTranslations("common");
  const { register, handleSubmit, reset, formState: { isValid } } = useForm<{ reason: string }>({
    mode: "onChange",
  });
  React.useEffect(() => {
    if (!open) reset({ reason: "" });
  }, [open, reset]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("title")}
      subtitle={t("subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || loading}
            onClick={handleSubmit((v) => onConfirm(v.reason))}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("confirm")}
          </Button>
        </>
      }
    >
      <Field label={t("reason")}>
        <Textarea rows={3} {...register("reason", { required: true, minLength: 5 })} />
      </Field>
    </Dialog>
  );
}

function TerminateModal({
  open,
  onClose,
  onConfirm,
  loading,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: (body: { terminationDate: string; reason: string }) => void;
  loading: boolean;
}) {
  const t = useTranslations("employees.actions.terminate");
  const tCommon = useTranslations("common");
  const today = new Date().toISOString().slice(0, 10);
  const { register, handleSubmit, reset, formState: { isValid } } = useForm<{
    terminationDate: string;
    reason: string;
  }>({
    mode: "onChange",
    defaultValues: { terminationDate: today, reason: "" },
  });
  React.useEffect(() => {
    if (!open) reset({ terminationDate: today, reason: "" });
  }, [open, reset, today]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={
        <span className="flex items-center gap-2 text-danger-600">
          <StopCircle className="h-5 w-5" />
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
            onClick={handleSubmit((v) => onConfirm(v))}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("confirm")}
          </Button>
        </>
      }
    >
      <div className="flex flex-col gap-4">
        <Field label={t("date")}>
          <Input type="date" {...register("terminationDate", { required: true })} />
        </Field>
        <Field label={t("reason")}>
          <Textarea rows={3} {...register("reason", { required: true, minLength: 5 })} />
        </Field>
      </div>
    </Dialog>
  );
}

function ReactivateModal({
  open,
  onClose,
  onConfirm,
  loading,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  loading: boolean;
}) {
  const t = useTranslations("employees.actions.reactivate");
  const tCommon = useTranslations("common");
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("title")}
      subtitle={t("subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button type="button" onClick={onConfirm} disabled={loading}>
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("confirm")}
          </Button>
        </>
      }
    >
      <p className="text-[13.5px] text-ink-3">{t("subtitle")}</p>
    </Dialog>
  );
}


function AddDependentModal({
  open,
  onClose,
  employeeId,
  onSuccess,
}: {
  open: boolean;
  onClose: () => void;
  employeeId: string;
  onSuccess: () => void;
}) {
  const tAct = useTranslations("employees.actions.addDependent");
  const tCreate = useTranslations("employees.create");
  const tDeps = useTranslations("employees.detail.dependents");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const { register, handleSubmit, reset, formState: { isValid } } = useForm<{
    prenom: string;
    nom: string;
    dateNaissance: string;
    lienParente: string;
  }>({ mode: "onChange" });
  React.useEffect(() => {
    if (!open) reset();
  }, [open, reset]);

  const mutation = useMutation({
    mutationFn: (v: { prenom: string; nom: string; dateNaissance: string; lienParente: string }) =>
      apiFetch(`/api/hrm/employees/${employeeId}/dependents`, { method: "POST", body: v }),
    onSuccess: () => {
      toast.success(tAct("success"));
      onSuccess();
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={tAct("title")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || mutation.isPending}
            onClick={handleSubmit((v) => mutation.mutate(v))}
          >
            {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : tAct("confirm")}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-2 gap-4">
        <Field label={tCreate("fields.firstName")}>
          <Input {...register("prenom", { required: true, minLength: 2 })} />
        </Field>
        <Field label={tCreate("fields.lastName")}>
          <Input {...register("nom", { required: true, minLength: 2 })} />
        </Field>
        <Field label={tCreate("fields.birthDate")}>
          <Input type="date" {...register("dateNaissance", { required: true })} />
        </Field>
        <Field label={tDeps("columns.relationship")}>
          <select
            {...register("lienParente", { required: true })}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400"
          >
            <option value="">—</option>
            <option value="SPOUSE">Conjoint(e)</option>
            <option value="CHILD">Enfant</option>
            <option value="PARENT">Parent</option>
            <option value="OTHER">Autre</option>
          </select>
        </Field>
      </div>
    </Dialog>
  );
}

function HeroTile({
  label,
  value,
  unit,
}: {
  label: string;
  value: React.ReactNode;
  unit?: React.ReactNode;
}) {
  return (
    <div className="rounded-[12px] border border-line bg-white p-3.5">
      <div className="text-[11px] font-semibold uppercase tracking-[0.04em] text-ink-3">
        {label}
      </div>
      <div className="mt-1.5 font-display text-[22px] font-extrabold tracking-tight text-ink font-mono-tabular">
        {value}
      </div>
      {unit && <div className="mt-0.5 text-[11px] text-ink-3">{unit}</div>}
    </div>
  );
}

/* ============================== Overview tab (design-faithful) ============================== */

function OverviewTab({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tOv = useTranslations("employees.detail.overview");
  const tEvt = useTranslations("employees.detail.eventType");

  const profileQ = useQuery({
    queryKey: ["hrm", "employee", "profile", employeeId],
    queryFn: () =>
      apiFetch<EmployeeProfileResponse>(`/api/hrm/employees/${employeeId}/profile`),
  });
  const timelineQ = useQuery({
    queryKey: ["hrm", "employee", "timeline", employeeId],
    queryFn: () =>
      apiFetch<TimelineEventResponse[]>(`/api/hrm/employees/${employeeId}/timeline`),
  });

  if (profileQ.isLoading) {
    return (
      <div className="grid place-items-center py-12">
        <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
      </div>
    );
  }
  const p = profileQ.data;
  if (!p) return null;

  const fields: Array<[string, React.ReactNode]> = [
    [tOv("fields.firstName"), p.actorFirstName ?? "—"],
    [tOv("fields.lastName"), p.actorLastName ?? "—"],
    [
      tOv("fields.birthDate"),
      p.actorBirthDate ? formatDateLong(p.actorBirthDate, "fr") : "—",
    ],
    [tOv("fields.gender"), p.actorGender ?? "—"],
    [tOv("fields.nationality"), p.actorNationality ?? "—"],
    [tOv("fields.phone"), p.actorPhoneNumber ?? "—"],
    [tOv("fields.email"), p.actorEmail ?? "—"],
    [
      tOv("fields.bank"),
      p.compteBancaire ? `**** ${p.compteBancaire.slice(-4)}` : "—",
    ],
    [
      tOv("fields.mobileMoney"),
      p.numMobileMoney ? `${p.operateurMm ?? ""} · **${p.numMobileMoney.slice(-4)}` : "—",
    ],
  ];

  const events = timelineQ.data ?? [];
  const eventIcon = (type: string) => {
    switch (type) {
      case "HIRE":
        return Briefcase;
      case "REVIEW":
        return Star;
      case "CONTRACT":
        return FileText;
      default:
        return GraduationCap;
    }
  };
  const eventTone = (type: string) => {
    switch (type) {
      case "HIRE":
        return "violet";
      case "REVIEW":
        return "success";
      case "CONTRACT":
        return "orange";
      default:
        return "info";
    }
  };

  return (
    <div className="grid gap-4 lg:grid-cols-[2fr_1fr]">
      {/* Left column */}
      <div className="flex flex-col gap-4">
        {/* Personal info */}
        <Card>
          <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
            <div className="text-[16px] font-bold tracking-tight text-ink">
              {tOv("personalInfo")}
            </div>
          </div>
          <CardContent padding="lg">
            <dl className="grid grid-cols-1 gap-x-8 gap-y-4 md:grid-cols-3">
              {fields.map(([label, value]) => (
                <div key={label}>
                  <Label className="text-[11px] uppercase tracking-[0.04em] text-ink-3">
                    {label}
                  </Label>
                  <div className="mt-1 text-[13px] text-ink-2">{value}</div>
                </div>
              ))}
            </dl>
          </CardContent>
        </Card>

        {/* Timeline */}
        <Card>
          <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
            <div className="text-[16px] font-bold tracking-tight text-ink">
              {tOv("timeline")}
            </div>
          </div>
          <CardContent padding="lg">
            {events.length === 0 ? (
              <p className="text-[13px] text-ink-3">{tOv("timelineEmpty")}</p>
            ) : (
              <div className="relative pl-2">
                {events.map((ev, idx) => {
                  const Icon = eventIcon(ev.type);
                  return (
                    <div
                      key={`${ev.type}-${ev.date}-${idx}`}
                      className="relative flex items-start gap-3.5 pb-5"
                    >
                      {idx < events.length - 1 && (
                        <span className="absolute left-[15px] top-[34px] bottom-0 w-px bg-line" />
                      )}
                      <IconTile icon={Icon} tone={eventTone(ev.type)} size="sm" />
                      <div className="flex-1">
                        <span className="font-mono-tabular text-[11px] uppercase tracking-wider text-ink-4">
                          {formatDate(ev.date, { locale: "fr" })} ·{" "}
                          {tEvt(ev.type as "HIRE" | "CONTRACT" | "REVIEW")}
                        </span>
                        <div className="mt-0.5 text-[13.5px] font-semibold text-ink">
                          {ev.title}
                        </div>
                        {ev.detail && (
                          <div className="mt-0.5 text-[12px] text-ink-3">{ev.detail}</div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Right column */}
      <div className="flex flex-col gap-4">
        {/* Hierarchy */}
        <Card>
          <CardContent padding="lg">
            <div className="mb-3 text-[14px] font-bold tracking-tight text-ink">
              {tOv("hierarchy")}
            </div>
            {p.managerDisplayName ? (
              <div className="flex items-center gap-3 rounded-[10px] bg-bg-dim p-3">
                <Avatar name={p.managerDisplayName} size="lg" tone="green" />
                <div className="flex-1 min-w-0">
                  <div className="truncate text-[13.5px] font-semibold text-ink">
                    {p.managerDisplayName}
                  </div>
                  <div className="text-[11px] text-ink-3">{tOv("managerLabel")}</div>
                </div>
              </div>
            ) : (
              <div className="rounded-[10px] border border-line-soft bg-bg-soft px-3 py-2.5 text-[12.5px] text-ink-3">
                {tOv("noManager")}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Quick contact card */}
        {(p.actorEmail || p.actorPhoneNumber) && (
          <Card>
            <CardContent padding="lg">
              <div className="mb-3 text-[14px] font-bold tracking-tight text-ink">
                Contact
              </div>
              <div className="flex flex-col gap-2">
                {p.actorEmail && (
                  <div className="flex items-center gap-2.5 text-[13px] text-ink-2">
                    <Mail className="h-4 w-4 text-ink-3" />
                    <a
                      href={`mailto:${p.actorEmail}`}
                      className="break-all hover:text-orange-600"
                    >
                      {p.actorEmail}
                    </a>
                  </div>
                )}
                {p.actorPhoneNumber && (
                  <div className="flex items-center gap-2.5 text-[13px] text-ink-2">
                    <Phone className="h-4 w-4 text-ink-3" />
                    <a
                      href={`tel:${p.actorPhoneNumber}`}
                      className="font-mono-tabular hover:text-orange-600"
                    >
                      {p.actorPhoneNumber}
                    </a>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Key skills — level bars, design-faithful */}
        <Card>
          <CardContent padding="lg">
            <EmployeeSkills employeeId={employeeId} />
          </CardContent>
        </Card>

        {/* Documents — real upload + list via file-core's document-hub. */}
        <Card>
          <CardContent padding="lg">
            <EmployeeDocuments employeeId={employeeId} />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
