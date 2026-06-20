import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";
import type { EmployeeResponse, PaymentChannel } from "@/server/ksm/modules/employees";

/**
 * Payroll-owned employee referential (standalone payroll mode — tenants using payroll
 * without hrm-core). Base path /api/v1/payroll/employees.
 */

export type PayrollDataSource = "HRM" | "LOCAL";

export type PayrollEmployeeResponse = {
  id: string;
  organizationId: string;
  agencyId: string | null;
  actorId: string | null;
  matricule: string;
  displayName: string;
  email: string | null;
  socialSecurityNo: string | null;
  categorie: number;
  echelon: string | null;
  departmentCode: string | null;
  hireDate: string;
  departureDate: string | null;
  maritalStatus: string;
  dependentChildren: number;
  baseSalary: number | string;
  benefitsInKind: number | string;
  position: string | null;
  paymentChannel: string;
  accountRef: string | null;
  active: boolean;
};

export type UpsertPayrollEmployeeRequest = {
  organizationId: string;
  agencyId?: string | null;
  matricule: string;
  displayName: string;
  email?: string | null;
  socialSecurityNo?: string | null;
  categorie?: number | null;
  echelon?: string | null;
  departmentCode?: string | null;
  hireDate: string;
  maritalStatus?: string | null;
  dependentChildren?: number | null;
  baseSalary: number;
  benefitsInKind?: number | null;
  position?: string | null;
  paymentChannel?: string | null;
  accountRef?: string | null;
};

export type CsvImportReport = {
  total: number;
  created: number;
  updated: number;
  errors: { line: number; matricule: string; message: string }[];
};

/**
 * Project a payroll-owned employee onto the HRM {@link EmployeeResponse} shape the payroll UI
 * (monthly variables, run entries, garnishments…) already consumes — so standalone-payroll
 * tenants reuse the exact same screens as HRM tenants.
 */
export function toEmployeeResponse(p: PayrollEmployeeResponse): EmployeeResponse {
  const channel = p.paymentChannel as PaymentChannel;
  return {
    id: p.id,
    organizationId: p.organizationId,
    agencyId: p.agencyId,
    actorId: p.actorId ?? p.id,
    managerId: null,
    matricule: p.matricule,
    numCnps: p.socialSecurityNo,
    categorie: p.categorie,
    echelon: p.echelon,
    dateEmbauche: p.hireDate,
    status: p.active ? "ACTIVE" : "TERMINATED",
    departmentCode: p.departmentCode,
    modePaiement: channel,
    compteBancaire: channel === "BANK_TRANSFER" ? p.accountRef : null,
    numMobileMoney:
      channel === "MTN_MOBILE_MONEY" || channel === "ORANGE_MONEY" ? p.accountRef : null,
    operateurMm:
      channel === "MTN_MOBILE_MONEY" ? "MTN" : channel === "ORANGE_MONEY" ? "ORANGE" : null,
    actorDisplayName: p.displayName,
  };
}

function requireOrgId(session: AppSession, organizationId?: string): string {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  return orgId;
}

export function listPayrollEmployees(session: AppSession, organizationId?: string) {
  const orgId = requireOrgId(session, organizationId);
  return callKsm<PayrollEmployeeResponse[]>(
    `/api/v1/payroll/employees?organizationId=${orgId}`,
    {},
    { session },
  );
}

export function getPayrollEmployee(id: string, session: AppSession) {
  return callKsm<PayrollEmployeeResponse>(`/api/v1/payroll/employees/${id}`, {}, { session });
}

export function createPayrollEmployee(body: UpsertPayrollEmployeeRequest, session: AppSession) {
  return callKsm<PayrollEmployeeResponse>(
    `/api/v1/payroll/employees`,
    { method: "POST", body },
    { session },
  );
}

export function updatePayrollEmployee(
  id: string,
  body: UpsertPayrollEmployeeRequest,
  session: AppSession,
) {
  return callKsm<PayrollEmployeeResponse>(
    `/api/v1/payroll/employees/${id}`,
    { method: "PUT", body },
    { session },
  );
}

export function deactivatePayrollEmployee(
  id: string,
  departureDate: string | null,
  session: AppSession,
) {
  return callKsm<PayrollEmployeeResponse>(
    `/api/v1/payroll/employees/${id}/deactivate`,
    { method: "PUT", body: { departureDate } },
    { session },
  );
}

export function importPayrollEmployeesCsv(csv: string, session: AppSession, organizationId?: string) {
  const orgId = requireOrgId(session, organizationId);
  return callKsm<CsvImportReport>(
    `/api/v1/payroll/employees/import?organizationId=${orgId}`,
    { method: "POST", body: { csv } },
    { session },
  );
}

export type CsvColumnSpec = {
  header: string;
  required: boolean;
  example: string;
  acceptedValues: string[];
};

export type CsvTemplateResponse = {
  csv: string;
  columns: CsvColumnSpec[];
};

export function getPayrollEmployeesCsvTemplate(session: AppSession) {
  return callKsm<CsvTemplateResponse>(`/api/v1/payroll/employees/template`, {}, { session });
}

export function getPayrollDataSource(session: AppSession, organizationId?: string) {
  const orgId = requireOrgId(session, organizationId);
  return callKsm<{ source: PayrollDataSource }>(
    `/api/v1/payroll/employees/data-source?organizationId=${orgId}`,
    {},
    { session },
  );
}

export function setPayrollDataSource(
  source: PayrollDataSource,
  session: AppSession,
  organizationId?: string,
) {
  const orgId = requireOrgId(session, organizationId);
  return callKsm<{ source: PayrollDataSource }>(
    `/api/v1/payroll/employees/data-source?organizationId=${orgId}`,
    { method: "PUT", body: { source } },
    { session },
  );
}
