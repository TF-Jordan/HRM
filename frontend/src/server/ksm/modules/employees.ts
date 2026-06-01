import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

/* ============================== Enums ============================== */

export type EmployeeStatus = "ACTIVE" | "ON_LEAVE" | "SUSPENDED" | "TERMINATED";
export type ContractType = "CDI" | "CDD" | "STAGE" | "INTERIM";
export type ContractStatusValue = "ACTIVE" | "TRIAL" | "EXPIRED" | "TERMINATED" | "RENEWED";
export type PaymentChannel = "BANK_TRANSFER" | "MTN_MOBILE_MONEY" | "ORANGE_MONEY" | "CASH";
export type MobileOperator = "MTN" | "ORANGE";
export type LeaveType = "ANNUAL" | "SICK" | "MATERNITY" | "PATERNITY" | "UNPAID" | "SPECIAL";

/* ============================== DTOs ============================== */

export type EmployeeResponse = {
  id: string;
  organizationId: string;
  agencyId?: string | null;
  actorId: string;
  managerId?: string | null;
  matricule: string;
  numCnps?: string | null;
  categorie: number;
  echelon?: string | null;
  dateEmbauche: string;
  status: EmployeeStatus;
  departmentCode?: string | null;
  modePaiement?: PaymentChannel | null;
  compteBancaire?: string | null;
  numMobileMoney?: string | null;
  operateurMm?: MobileOperator | null;
  actorDisplayName?: string | null;
};

export type ContractResponse = {
  id: string;
  employeeId: string;
  type: ContractType;
  dateDebut: string;
  dateFin?: string | null;
  salaireBase: number | string;
  avantagesNature?: number | string | null;
  periodeEssai?: number | null;
  status: ContractStatusValue;
  motifFin?: string | null;
  documentFileId?: string | null;
};

export type DependentResponse = {
  id: string;
  employeeId: string;
  nom: string;
  prenom: string;
  dateNaissance: string;
  lienParente: string;
  certificatFileId?: string | null;
};

export type LeaveBalanceResponse = {
  id: string;
  employeeId: string;
  type: LeaveType;
  acquis: number | string;
  pris: number | string;
  soldeRestant: number | string;
  annee: number;
};

/* ============================== Requests ============================== */

export type CreateEmployeeRequest = {
  actorId: string;
  numCnps?: string;
  categorie: number;
  echelon?: string;
  dateEmbauche: string;
  departmentCode?: string;
  modePaiement?: PaymentChannel;
  compteBancaire?: string;
  numMobileMoney?: string;
  operateurMm?: MobileOperator;
};

export type UpdateEmployeeRequest = {
  numCnps?: string;
  categorie: number;
  echelon?: string;
  departmentCode?: string;
  modePaiement?: PaymentChannel;
  compteBancaire?: string;
  numMobileMoney?: string;
  operateurMm?: MobileOperator;
  managerId?: string;
};

export type TerminateEmployeeRequest = { terminationDate: string; reason: string };
export type SuspendEmployeeRequest = { reason: string };

export type AddContractRequest = {
  type: ContractType;
  dateDebut: string;
  dateFin?: string;
  salaireBase: number;
  avantagesNature?: number;
  periodeEssai?: number;
  documentFileId?: string;
};

export type TerminateContractRequest = { motif: string };
export type RenewContractRequest = { newDateFin: string };

export type AddDependentRequest = {
  nom: string;
  prenom: string;
  dateNaissance: string;
  lienParente: string;
};

/* ============================== Calls ============================== */

export function listEmployees(
  session: AppSession,
  filters?: { organizationId?: string; agencyId?: string },
) {
  const orgId = filters?.organizationId ?? session.workspace?.organizationId;
  if (!orgId) {
    throw new Error("organizationId is required to list employees");
  }
  const params = new URLSearchParams({ organizationId: orgId });
  if (filters?.agencyId ?? session.workspace?.agencyId) {
    params.set("agencyId", filters?.agencyId ?? session.workspace!.agencyId!);
  }
  return callKsm<EmployeeResponse[]>(`/api/v1/hrm/employees?${params}`, {}, { session });
}

export function getEmployee(employeeId: string, session: AppSession) {
  return callKsm<EmployeeResponse>(`/api/v1/hrm/employees/${employeeId}`, {}, { session });
}

export function createEmployee(body: CreateEmployeeRequest, session: AppSession) {
  return callKsm<EmployeeResponse>(
    "/api/v1/hrm/employees",
    { method: "POST", body },
    { session },
  );
}

export function updateEmployee(
  employeeId: string,
  body: UpdateEmployeeRequest,
  session: AppSession,
) {
  return callKsm<EmployeeResponse>(
    `/api/v1/hrm/employees/${employeeId}`,
    { method: "PUT", body },
    { session },
  );
}

export function terminateEmployee(
  employeeId: string,
  body: TerminateEmployeeRequest,
  session: AppSession,
) {
  return callKsm<EmployeeResponse>(
    `/api/v1/hrm/employees/${employeeId}/terminate`,
    { method: "PUT", body },
    { session },
  );
}

export function suspendEmployee(
  employeeId: string,
  body: SuspendEmployeeRequest,
  session: AppSession,
) {
  return callKsm<EmployeeResponse>(
    `/api/v1/hrm/employees/${employeeId}/suspend`,
    { method: "PUT", body },
    { session },
  );
}

export function reactivateEmployee(employeeId: string, session: AppSession) {
  return callKsm<EmployeeResponse>(
    `/api/v1/hrm/employees/${employeeId}/reactivate`,
    { method: "PUT" },
    { session },
  );
}

export function listContracts(employeeId: string, session: AppSession) {
  return callKsm<ContractResponse[]>(
    `/api/v1/hrm/employees/${employeeId}/contracts`,
    {},
    { session },
  );
}

export function addContract(
  employeeId: string,
  body: AddContractRequest,
  session: AppSession,
) {
  return callKsm<ContractResponse>(
    `/api/v1/hrm/employees/${employeeId}/contracts`,
    { method: "POST", body },
    { session },
  );
}

export function getContract(employeeId: string, contractId: string, session: AppSession) {
  return callKsm<ContractResponse>(
    `/api/v1/hrm/employees/${employeeId}/contracts/${contractId}`,
    {},
    { session },
  );
}

export function terminateContract(
  employeeId: string,
  contractId: string,
  body: TerminateContractRequest,
  session: AppSession,
) {
  return callKsm<ContractResponse>(
    `/api/v1/hrm/employees/${employeeId}/contracts/${contractId}/terminate`,
    { method: "PUT", body },
    { session },
  );
}

export function renewContract(
  employeeId: string,
  contractId: string,
  body: RenewContractRequest,
  session: AppSession,
) {
  return callKsm<ContractResponse>(
    `/api/v1/hrm/employees/${employeeId}/contracts/${contractId}/renew`,
    { method: "POST", body },
    { session },
  );
}

export function listDependents(employeeId: string, session: AppSession) {
  return callKsm<DependentResponse[]>(
    `/api/v1/hrm/employees/${employeeId}/dependents`,
    {},
    { session },
  );
}

export function addDependent(
  employeeId: string,
  body: AddDependentRequest,
  session: AppSession,
) {
  return callKsm<DependentResponse>(
    `/api/v1/hrm/employees/${employeeId}/dependents`,
    { method: "POST", body },
    { session },
  );
}

export function listLeaveBalances(employeeId: string, annee: number, session: AppSession) {
  return callKsm<LeaveBalanceResponse[]>(
    `/api/v1/hrm/employees/${employeeId}/leave-balances?annee=${annee}`,
    {},
    { session },
  );
}

export function checkCnpsAvailability(value: string, session: AppSession) {
  return callKsm<boolean>(
    `/api/v1/hrm/employees/check-cnps?value=${encodeURIComponent(value)}`,
    {},
    { session },
  );
}
