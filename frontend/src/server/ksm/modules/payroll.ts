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

/* ===================== Pay variables (monthly inputs) ===================== */

export type PayVariableResponse = {
  id: string;
  organizationId: string;
  employeeId: string;
  periode: string;
  overtimeHoursDay: number | string;
  overtimeHoursNight: number | string;
  overtimeHoursSundayHoliday: number | string;
  bonuses: number | string;
  unpaidAbsenceDays: number | string;
  advances: number | string;
  workedDaysOverride: number | null;
  locked: boolean;
};

export type CapturePayVariableRequest = {
  organizationId: string;
  employeeId: string;
  period: string;
  overtimeHoursDay: number;
  overtimeHoursNight: number;
  overtimeHoursSundayHoliday: number;
  bonuses: number;
  unpaidAbsenceDays: number;
  advances: number;
  workedDaysOverride: number | null;
};

export function listPayVariables(session: AppSession, period: string, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId, period });
  return callKsm<PayVariableResponse[]>(
    `/api/v1/payroll/variables?${params}`,
    {},
    { session },
  );
}

export function capturePayVariable(body: CapturePayVariableRequest, session: AppSession) {
  return callKsm<PayVariableResponse>(
    `/api/v1/payroll/variables`,
    { method: "POST", body },
    { session },
  );
}

/* ===================== Pay elements (rubric catalogue) ===================== */

export type PayElementCategory = "EARNING" | "DEDUCTION" | "EMPLOYER_CHARGE" | "INFORMATIONAL";
export type CalculationMethod = "RATE" | "BRACKET" | "FLAT" | "LOOKUP_TABLE" | "FORMULA";

export type PayElementResponse = {
  id: string;
  code: string;
  label: string;
  category: PayElementCategory;
  method: CalculationMethod;
  baseReference: string | null;
  rate: number | string | null;
  ceiling: number | string | null;
  floor: number | string | null;
  exemptionThreshold: number | string | null;
  flatAmount: number | string | null;
  bracketTableCode: string | null;
  lookupTableCode: string | null;
  taxable: boolean;
  socialContributable: boolean;
  countryCode: string;
  displayOrder: number;
  active: boolean;
  effectiveFrom: string;
  effectiveTo: string | null;
};

export type CreatePayElementRequest = {
  code: string;
  label: string;
  category: PayElementCategory;
  method: CalculationMethod;
  baseReference?: string | null;
  rate?: number | null;
  ceiling?: number | null;
  floor?: number | null;
  exemptionThreshold?: number | null;
  flatAmount?: number | null;
  bracketTableCode?: string | null;
  lookupTableCode?: string | null;
  taxable: boolean;
  socialContributable: boolean;
  countryCode: string;
  displayOrder: number;
  effectiveFrom: string;
  effectiveTo?: string | null;
};

export function listPayElements(session: AppSession, countryCode: string) {
  const params = new URLSearchParams({ countryCode });
  return callKsm<PayElementResponse[]>(
    `/api/v1/payroll/pay-elements?${params}`,
    {},
    { session },
  );
}

export function createPayElement(body: CreatePayElementRequest, session: AppSession) {
  return callKsm<PayElementResponse>(
    `/api/v1/payroll/pay-elements`,
    { method: "POST", body },
    { session },
  );
}

export function deactivatePayElement(id: string, session: AppSession) {
  return callKsm<PayElementResponse>(
    `/api/v1/payroll/pay-elements/${id}`,
    { method: "DELETE" },
    { session },
  );
}

/* ===================== Tax bracket tables (progressive scales) ===================== */

export type BracketLine = {
  ordre: number;
  lowerBound: number | string;
  upperBound: number | string | null;
  rate: number | string;
};

export type TaxBracketTableResponse = {
  id: string;
  code: string;
  label: string;
  countryCode: string;
  effectiveFrom: string;
  effectiveTo: string | null;
  active: boolean;
  brackets: BracketLine[];
};

export type CreateTaxBracketTableRequest = {
  code: string;
  label: string;
  countryCode: string;
  effectiveFrom: string;
  effectiveTo?: string | null;
  brackets: BracketLine[];
};

export function listTaxBracketTables(session: AppSession, countryCode: string) {
  const params = new URLSearchParams({ countryCode });
  return callKsm<TaxBracketTableResponse[]>(
    `/api/v1/payroll/tax-brackets?${params}`,
    {},
    { session },
  );
}

export function createTaxBracketTable(body: CreateTaxBracketTableRequest, session: AppSession) {
  return callKsm<TaxBracketTableResponse>(
    `/api/v1/payroll/tax-brackets`,
    { method: "POST", body },
    { session },
  );
}

export function deactivateTaxBracketTable(id: string, session: AppSession) {
  return callKsm<TaxBracketTableResponse>(
    `/api/v1/payroll/tax-brackets/${id}`,
    { method: "DELETE" },
    { session },
  );
}

/* ===================== Lookup tables (stepped forfaits: RAV/TDL) ===================== */

export type EntryLine = {
  ordre: number;
  lowerBound: number | string;
  upperBound: number | string | null;
  amount: number | string;
};

export type LookupTableResponse = {
  id: string;
  code: string;
  label: string;
  countryCode: string;
  effectiveFrom: string;
  effectiveTo: string | null;
  active: boolean;
  entries: EntryLine[];
};

export type CreateLookupTableRequest = {
  code: string;
  label: string;
  countryCode: string;
  effectiveFrom: string;
  effectiveTo?: string | null;
  entries: EntryLine[];
};

export function listLookupTables(session: AppSession, countryCode: string) {
  const params = new URLSearchParams({ countryCode });
  return callKsm<LookupTableResponse[]>(
    `/api/v1/payroll/lookup-tables?${params}`,
    {},
    { session },
  );
}

export function createLookupTable(body: CreateLookupTableRequest, session: AppSession) {
  return callKsm<LookupTableResponse>(
    `/api/v1/payroll/lookup-tables`,
    { method: "POST", body },
    { session },
  );
}

export function deactivateLookupTable(id: string, session: AppSession) {
  return callKsm<LookupTableResponse>(
    `/api/v1/payroll/lookup-tables/${id}`,
    { method: "DELETE" },
    { session },
  );
}
