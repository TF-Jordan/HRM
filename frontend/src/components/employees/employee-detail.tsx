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
import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Column, DataTable } from "@/components/ui/data-table";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label, Textarea } from "@/components/ui/input";
import { IconTile } from "@/components/ui/icon-tile";
import { StatusPill } from "@/components/ui/status-pill";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { employeeStatusTone } from "@/lib/employee-status";
import { formatDate, formatMoney } from "@/lib/format";
import { initials } from "@/lib/utils";
import type {
  ContractResponse,
  ContractType,
  DependentResponse,
  EmployeeResponse,
  LeaveBalanceResponse,
} from "@/server/ksm/modules/employees";

type Tab = "identity" | "contracts" | "dependents" | "leaves";

export function EmployeeDetail({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tDetail = useTranslations("employees.detail");
  const tStatus = useTranslations("employees.status");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();
  const [tab, setTab] = React.useState<Tab>("identity");
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

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("list.title"), href: "/employees" },
          { label: e.actorDisplayName ?? e.matricule },
        ]}
        title={
          <span className="flex items-center gap-3">
            <Avatar
              name={e.actorDisplayName ?? e.matricule}
              initials={initials(e.actorDisplayName ?? e.matricule, 2)}
              size="lg"
              tone={
                (["orange", "blue", "green", "violet", "amber", "teal"] as const)[
                  e.id.charCodeAt(0) % 6
                ]
              }
            />
            <span className="flex flex-col">
              <span>{e.actorDisplayName ?? e.matricule}</span>
              <span className="font-mono-tabular text-[13px] font-normal tracking-normal text-ink-3">
                {e.matricule}
              </span>
            </span>
          </span>
        }
        subtitle={
          <span className="flex items-center gap-2">
            <Badge tone={employeeStatusTone(e.status)}>{tStatus(e.status)}</Badge>
            {e.departmentCode && <span className="text-ink-3">· {e.departmentCode}</span>}
          </span>
        }
        actions={
          <>
            <Link href="/employees">
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("list.title")}
              </Button>
            </Link>
            {canEdit && !isTerminated && (
              <Button type="button" variant="secondary">
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
          </>
        }
      />

      {/* Tabs */}
      <div className="mb-6 flex flex-wrap gap-2 border-b border-line-soft pb-2">
        {(["identity", "contracts", "dependents", "leaves"] as const).map((tabKey) => (
          <button
            key={tabKey}
            type="button"
            onClick={() => setTab(tabKey)}
            className={`relative px-3.5 py-2 text-[13.5px] font-medium tracking-tight transition-colors ${
              tab === tabKey
                ? "text-orange-600 after:absolute after:bottom-[-9px] after:left-2 after:right-2 after:h-[3px] after:rounded-full after:bg-grad-orange"
                : "text-ink-3 hover:text-ink"
            }`}
          >
            {tDetail(`tabs.${tabKey}`)}
          </button>
        ))}
      </div>

      {tab === "identity" && <IdentityTab employee={e} />}
      {tab === "contracts" && (
        <ContractsTab
          employeeId={employeeId}
          isTerminated={isTerminated}
          canAdd={canAddContract}
          onAdd={() => setModal("addContract")}
        />
      )}
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
      <AddContractModal
        open={modal === "addContract"}
        onClose={() => setModal(null)}
        employeeId={employeeId}
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

function ContractsTab({
  employeeId,
  isTerminated,
  canAdd,
  onAdd,
}: {
  employeeId: string;
  isTerminated: boolean;
  canAdd: boolean;
  onAdd: () => void;
}) {
  const t = useTranslations("employees");
  const tContracts = useTranslations("employees.detail.contracts");
  const { data, isLoading, error } = useQuery({
    queryKey: ["hrm", "contracts", employeeId],
    queryFn: () => apiFetch<ContractResponse[]>(`/api/hrm/employees/${employeeId}/contracts`),
  });

  const columns: Column<ContractResponse>[] = [
    {
      key: "type",
      header: tContracts("columns.type"),
      cell: (c) => (
        <Badge tone={c.status === "ACTIVE" ? "success" : "gray"}>
          {t(`contractType.${c.type}`)}
        </Badge>
      ),
    },
    {
      key: "period",
      header: tContracts("columns.period"),
      cell: (c) => (
        <span className="font-mono-tabular text-[12.5px] text-ink-2">
          {formatDate(c.dateDebut, { locale: "fr" })}
          {c.dateFin ? ` → ${formatDate(c.dateFin, { locale: "fr" })}` : " → …"}
        </span>
      ),
    },
    {
      key: "salary",
      header: tContracts("columns.salary"),
      cell: (c) => (
        <span className="font-mono-tabular font-semibold text-ink">
          {formatMoney(Number(c.salaireBase), { locale: "fr" })}
        </span>
      ),
    },
    {
      key: "status",
      header: tContracts("columns.status"),
      cell: (c) => (
        <Badge tone={c.status === "ACTIVE" ? "success" : c.status === "EXPIRED" ? "warning" : "gray"}>
          {t(`contractStatus.${c.status}`)}
        </Badge>
      ),
    },
  ];

  return (
    <>
      {canAdd && !isTerminated && (
        <div className="mb-3 flex justify-end">
          <Button type="button" onClick={onAdd}>
            <Plus className="h-4 w-4" />
            {tContracts("addNew")}
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
        <DataTable columns={columns} data={data} rowKey={(c) => c.id} empty={tContracts("noContracts")} />
      )}
    </>
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

function AddContractModal({
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
  const t = useTranslations("employees");
  const tCreate = useTranslations("employees.create");
  const tAct = useTranslations("employees.actions.addContract");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  type ContractFormValues = {
    type: ContractType;
    dateDebut: string;
    dateFin: string;
    salaireBase: string;
    avantagesNature: string;
    periodeEssai: string;
  };
  const { register, handleSubmit, reset, watch, formState: { isValid } } = useForm<ContractFormValues>({
    mode: "onChange",
    defaultValues: {
      type: "CDI",
      dateDebut: new Date().toISOString().slice(0, 10),
      dateFin: "",
      salaireBase: "",
      avantagesNature: "",
      periodeEssai: "",
    },
  });
  const isCdd = watch("type") === "CDD" || watch("type") === "STAGE";
  React.useEffect(() => {
    if (!open) reset();
  }, [open, reset]);

  const mutation = useMutation({
    mutationFn: (v: ContractFormValues) =>
      apiFetch(`/api/hrm/employees/${employeeId}/contracts`, {
        method: "POST",
        body: {
          type: v.type,
          dateDebut: v.dateDebut,
          dateFin: isCdd && v.dateFin ? v.dateFin : undefined,
          salaireBase: Number(v.salaireBase),
          avantagesNature: v.avantagesNature ? Number(v.avantagesNature) : undefined,
          periodeEssai: v.periodeEssai ? Number(v.periodeEssai) : undefined,
        },
      }),
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
      size="lg"
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
        <Field label={tCreate("fields.contractType")}>
          <select
            {...register("type", { required: true })}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400"
          >
            <option value="CDI">{t("contractType.CDI")}</option>
            <option value="CDD">{t("contractType.CDD")}</option>
            <option value="STAGE">{t("contractType.STAGE")}</option>
            <option value="INTERIM">{t("contractType.INTERIM")}</option>
          </select>
        </Field>
        <Field label={tCreate("fields.contractDateDebut")}>
          <Input type="date" {...register("dateDebut", { required: true })} />
        </Field>
        <Field label={tCreate("fields.contractDateFin")}>
          <Input type="date" disabled={!isCdd} {...register("dateFin", { required: isCdd })} />
        </Field>
        <Field label={tCreate("fields.periodeEssai")}>
          <Input type="number" min="0" max="365" {...register("periodeEssai")} />
        </Field>
        <Field label={tCreate("fields.salaireBase")}>
          <Input type="number" min="1" {...register("salaireBase", { required: true, min: 1 })} />
        </Field>
        <Field label={tCreate("fields.avantagesNature")}>
          <Input type="number" min="0" {...register("avantagesNature")} />
        </Field>
      </div>
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
