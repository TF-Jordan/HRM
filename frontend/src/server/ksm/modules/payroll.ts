import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type PayrollRunStatus = "CALCULATED" | "VALIDATED" | "PAID";
export type PaymentStatus = "PENDING" | "SCHEDULED" | "SENT" | "PAID" | "FAILED";

export type PayrollRunResponse = {
  id: string;
  periode: string;
  status: PayrollRunStatus | string;
  totalBrut: number | string;
  totalNet: number | string;
  totalCnpsEmploye: number | string;
  totalCnpsEmployeur: number | string;
  totalIrpp: number | string;
  totalCac: number | string;
  totalCfc: number | string;
  nbEmployes: number;
  createdAt: string | null;
  calculatedAt: string | null;
  validatedBy: string | null;
  validatedAt: string | null;
};

export type PayrollEntryResponse = {
  id: string;
  employeeId: string;
  salaireBase: number | string;
  brut: number | string;
  net: number | string;
  cnpsEmploye: number | string;
  cnpsEmployeur: number | string;
  irpp: number | string;
  cac: number | string;
  cfc: number | string;
  primes: number | string;
  retenues: number | string;
  avancesDeduites: number | string;
  paymentStatus: PaymentStatus | string;
  paymentChannel: string | null;
};

export type PayslipLineResponse = {
  id: string;
  libelle: string;
  type: "EARNING" | "DEDUCTION" | "EMPLOYER_CONTRIBUTION" | string;
  base: number | string | null;
  taux: number | string | null;
  montant: number | string;
  ordreAffichage: number;
};

export type RunPayrollRequest = {
  periode: string;
  agencyId?: string | null;
};

export function listPayrollRuns(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  return callKsm<PayrollRunResponse[]>(
    `/api/v1/hrm/payroll/runs?${params}`,
    {},
    { session },
  );
}

export function getPayrollRun(id: string, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/hrm/payroll/runs/${id}`,
    {},
    { session },
  );
}

export function listPayrollEntries(runId: string, session: AppSession) {
  return callKsm<PayrollEntryResponse[]>(
    `/api/v1/hrm/payroll/runs/${runId}/entries`,
    {},
    { session },
  );
}

export function listPayslipLines(entryId: string, session: AppSession) {
  return callKsm<PayslipLineResponse[]>(
    `/api/v1/hrm/payroll/entries/${entryId}/payslip`,
    {},
    { session },
  );
}

export function runPayroll(body: RunPayrollRequest, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/hrm/payroll/run`,
    { method: "POST", body },
    { session },
  );
}

export function validatePayroll(id: string, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/hrm/payroll/runs/${id}/validate`,
    { method: "PUT" },
    { session },
  );
}

export type MyPayslipSummaryResponse = {
  entryId: string;
  runId: string;
  periode: string;
  runStatus: string;
  brut: number | string;
  net: number | string;
  cnpsEmploye: number | string;
  irpp: number | string;
  cac: number | string;
  cfc: number | string;
  paymentStatus: string;
  paymentChannel: string | null;
  paymentDate: string | null;
};

export function listMyPayslips(session: AppSession) {
  return callKsm<MyPayslipSummaryResponse[]>(
    `/api/v1/hrm/payroll/my-entries`,
    {},
    { session },
  );
}

export function getMyPayslipLines(entryId: string, session: AppSession) {
  return callKsm<PayslipLineResponse[]>(
    `/api/v1/hrm/payroll/my-entries/${entryId}/payslip`,
    {},
    { session },
  );
}
