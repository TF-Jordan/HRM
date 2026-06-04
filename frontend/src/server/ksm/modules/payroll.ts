import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

/**
 * KSM client for the autonomous payroll-core module (base path /api/v1/payroll).
 * Replaces the legacy hrm-payroll endpoints; totals are now jurisdiction-agnostic
 * (gross / deductions / income tax / employer charges / net) and the per-tax detail
 * lives in the payslip lines.
 */

export type PayrollRunStatus =
  | "DRAFT"
  | "VARIABLES_LOCKED"
  | "CALCULATED"
  | "REVIEW"
  | "VALIDATED"
  | "APPROVED"
  | "PAYMENT_INITIATED"
  | "PAID"
  | "CLOSED";

export type PaymentStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";

export type PayrollRunResponse = {
  id: string;
  periode: string;
  runType: string;
  status: PayrollRunStatus | string;
  currency: string;
  totalGross: number | string;
  totalEmployeeDeductions: number | string;
  totalIncomeTax: number | string;
  totalNet: number | string;
  totalEmployerCharges: number | string;
  nbEmployes: number;
  calculatedAt: string | null;
  validatedBy: string | null;
  validatedAt: string | null;
  approvedBy: string | null;
  approvedAt: string | null;
  paidAt: string | null;
  closedAt: string | null;
};

export type PayrollEntryResponse = {
  id: string;
  employeeId: string;
  currency: string;
  salaireBase: number | string;
  brut: number | string;
  totalDeductions: number | string;
  incomeTax: number | string;
  employerCharges: number | string;
  net: number | string;
  paymentStatus: PaymentStatus | string;
  paymentChannel: string | null;
  accountRef: string | null;
};

export type PayslipLineResponse = {
  id: string;
  payElementCode: string | null;
  libelle: string;
  type: "EARNING" | "DEDUCTION" | "EMPLOYER_INFO" | string;
  base: number | string | null;
  taux: number | string | null;
  montant: number | string;
  ordreAffichage: number;
};

export type RunPayrollRequest = {
  period: string;
  agencyId?: string | null;
  runType?: string | null;
};

export function listPayrollRuns(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  return callKsm<PayrollRunResponse[]>(
    `/api/v1/payroll/runs?${params}`,
    {},
    { session },
  );
}

export function getPayrollRun(id: string, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/payroll/runs/${id}`,
    {},
    { session },
  );
}

export function listPayrollEntries(runId: string, session: AppSession) {
  return callKsm<PayrollEntryResponse[]>(
    `/api/v1/payroll/runs/${runId}/entries`,
    {},
    { session },
  );
}

export function listPayslipLines(entryId: string, session: AppSession) {
  return callKsm<PayslipLineResponse[]>(
    `/api/v1/payroll/entries/${entryId}/payslip`,
    {},
    { session },
  );
}

export function runPayroll(body: RunPayrollRequest, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/payroll/runs`,
    { method: "POST", body },
    { session },
  );
}

export function validatePayroll(id: string, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/payroll/runs/${id}/validate`,
    { method: "PUT" },
    { session },
  );
}

export function approvePayroll(id: string, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/payroll/runs/${id}/approve`,
    { method: "PUT" },
    { session },
  );
}

export function initiatePayrollPayment(id: string, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/payroll/runs/${id}/initiate-payment`,
    { method: "PUT" },
    { session },
  );
}

export function closePayroll(id: string, session: AppSession) {
  return callKsm<PayrollRunResponse>(
    `/api/v1/payroll/runs/${id}/close`,
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
  totalDeductions: number | string;
  incomeTax: number | string;
  paymentStatus: string;
  paymentChannel: string | null;
  paymentDate: string | null;
};

export function listMyPayslips(session: AppSession) {
  return callKsm<MyPayslipSummaryResponse[]>(
    `/api/v1/payroll/my-entries`,
    {},
    { session },
  );
}

export function getMyPayslipLines(entryId: string, session: AppSession) {
  return callKsm<PayslipLineResponse[]>(
    `/api/v1/payroll/my-entries/${entryId}/payslip`,
    {},
    { session },
  );
}
