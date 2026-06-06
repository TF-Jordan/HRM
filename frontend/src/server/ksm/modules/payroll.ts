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

export function activatePayElement(id: string, session: AppSession) {
  return callKsm<PayElementResponse>(
    `/api/v1/payroll/pay-elements/${id}/activate`,
    { method: "PUT" },
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

export function activateTaxBracketTable(id: string, session: AppSession) {
  return callKsm<TaxBracketTableResponse>(
    `/api/v1/payroll/tax-brackets/${id}/activate`,
    { method: "PUT" },
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

export function activateLookupTable(id: string, session: AppSession) {
  return callKsm<LookupTableResponse>(
    `/api/v1/payroll/lookup-tables/${id}/activate`,
    { method: "PUT" },
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

/* ===================== Retroactive adjustments (rappels de salaire) ===================== */

export type RetroactiveStatus = "PENDING" | "APPLIED" | "CANCELLED";

export type RetroactiveResponse = {
  id: string;
  employeeId: string;
  originPeriod: string;
  targetPeriod: string;
  reason: string | null;
  currency: string;
  oldGross: number | string | null;
  newGross: number | string | null;
  deltaGross: number | string | null;
  oldNet: number | string | null;
  newNet: number | string | null;
  deltaNet: number | string | null;
  status: RetroactiveStatus;
};

export type CalculateRetroactiveRequest = {
  employeeId: string;
  originPeriod: string;
  newBaseSalary: number;
  targetPeriod: string;
  reason?: string | null;
};

export function listRetroactiveAdjustments(
  session: AppSession,
  opts?: { employeeId?: string; organizationId?: string },
) {
  const params = new URLSearchParams();
  if (opts?.employeeId) {
    params.set("employeeId", opts.employeeId);
  } else {
    const orgId = opts?.organizationId ?? session.workspace?.organizationId;
    if (!orgId) throw new Error("organizationId is required");
    params.set("organizationId", orgId);
  }
  return callKsm<RetroactiveResponse[]>(
    `/api/v1/payroll/retroactive?${params}`,
    {},
    { session },
  );
}

export function calculateRetroactive(body: CalculateRetroactiveRequest, session: AppSession) {
  return callKsm<RetroactiveResponse>(
    `/api/v1/payroll/retroactive`,
    { method: "POST", body },
    { session },
  );
}

export function applyRetroactive(id: string, session: AppSession) {
  return callKsm<RetroactiveResponse>(
    `/api/v1/payroll/retroactive/${id}/apply`,
    { method: "PUT" },
    { session },
  );
}

export function cancelRetroactive(id: string, session: AppSession) {
  return callKsm<RetroactiveResponse>(
    `/api/v1/payroll/retroactive/${id}`,
    { method: "DELETE" },
    { session },
  );
}

/* ===================== Final settlements (soldes de tout compte) ===================== */

export type TerminationReason =
  | "RESIGNATION"
  | "DISMISSAL"
  | "DISMISSAL_GROSS_MISCONDUCT"
  | "END_OF_CONTRACT"
  | "RETIREMENT"
  | "MUTUAL_AGREEMENT"
  | "DEATH";

export type FinalSettlementStatus = "CALCULATED" | "PAID";

export type FinalSettlementResponse = {
  id: string;
  employeeId: string;
  periode: string;
  departureDate: string;
  reason: TerminationReason | string;
  currency: string;
  seniorityYears: number;
  proratedSalary: number | string;
  leaveCompensation: number | string;
  noticeIndemnity: number | string;
  severanceIndemnity: number | string;
  gratification: number | string;
  grossSettlement: number | string;
  loanDeducted: number | string;
  netSettlement: number | string;
  status: FinalSettlementStatus | string;
};

export type CalculateFinalSettlementRequest = {
  employeeId: string;
  departureDate: string;
  reason: TerminationReason;
  unusedLeaveDays: number;
  noticeMonths: number;
  accruedGratification: number;
};

export function calculateFinalSettlement(
  body: CalculateFinalSettlementRequest,
  session: AppSession,
) {
  return callKsm<FinalSettlementResponse>(
    `/api/v1/payroll/final-settlements`,
    { method: "POST", body },
    { session },
  );
}

export function markFinalSettlementPaid(id: string, session: AppSession) {
  return callKsm<FinalSettlementResponse>(
    `/api/v1/payroll/final-settlements/${id}/pay`,
    { method: "PUT" },
    { session },
  );
}

export function listFinalSettlements(
  session: AppSession,
  opts?: { employeeId?: string; organizationId?: string },
) {
  const params = new URLSearchParams();
  if (opts?.employeeId) {
    params.set("employeeId", opts.employeeId);
  } else {
    const orgId = opts?.organizationId ?? session.workspace?.organizationId;
    if (!orgId) throw new Error("organizationId is required");
    params.set("organizationId", orgId);
  }
  return callKsm<FinalSettlementResponse[]>(
    `/api/v1/payroll/final-settlements?${params}`,
    {},
    { session },
  );
}

/* ===================== Sealed payroll documents ===================== */

export type PayrollDocumentType = "PAYSLIP" | "FINAL_SETTLEMENT" | "WORK_CERTIFICATE";

export type PayrollDocumentResponse = {
  id: string;
  employeeId: string;
  type: PayrollDocumentType | string;
  subjectId: string;
  periode: string | null;
  fileId: string;
  fileName: string;
  algorithm: string;
  contentHashHex: string;
  verificationCode: string | null;
  keyId: string | null;
  signedAt: string | null;
};

export type DocumentVerification = {
  valid: boolean;
  verificationCode: string | null;
  contentHashHex: string;
  algorithm: string;
  signedAt: string | null;
};

export function listEmployeeDocuments(session: AppSession, employeeId: string) {
  const params = new URLSearchParams({ employeeId });
  return callKsm<PayrollDocumentResponse[]>(
    `/api/v1/payroll/documents?${params}`,
    {},
    { session },
  );
}

export function generatePayslipDocument(entryId: string, session: AppSession) {
  const params = new URLSearchParams({ entryId });
  return callKsm<PayrollDocumentResponse>(
    `/api/v1/payroll/documents/payslip?${params}`,
    { method: "POST" },
    { session },
  );
}

export function generateFinalSettlementDocument(settlementId: string, session: AppSession) {
  const params = new URLSearchParams({ settlementId });
  return callKsm<PayrollDocumentResponse>(
    `/api/v1/payroll/documents/final-settlement?${params}`,
    { method: "POST" },
    { session },
  );
}

export function generateWorkCertificate(
  employeeId: string,
  position: string | null,
  session: AppSession,
) {
  const params = new URLSearchParams({ employeeId });
  if (position) params.set("position", position);
  return callKsm<PayrollDocumentResponse>(
    `/api/v1/payroll/documents/work-certificate?${params}`,
    { method: "POST" },
    { session },
  );
}

export function verifyDocument(id: string, session: AppSession) {
  return callKsm<DocumentVerification>(
    `/api/v1/payroll/documents/${id}/verify`,
    {},
    { session },
  );
}

/* ===================== Statutory declarations (CNPS / DIPE / IRPP_CAC) ===================== */

export type DeclarationType = "CNPS" | "DIPE" | "IRPP_CAC";

export type DeclarationLineResponse = {
  employeeId: string;
  matricule: string;
  employeeName: string;
  socialSecurityNo: string | null;
  grossBase: number | string;
  employeeContribution: number | string;
  employerContribution: number | string;
};

export type DeclarationResponse = {
  type: DeclarationType | string;
  periode: string;
  employeeCount: number;
  totalGrossBase: number | string;
  totalEmployee: number | string;
  totalEmployer: number | string;
  grandTotal: number | string;
  items: DeclarationLineResponse[];
};

export function generateDeclaration(session: AppSession, type: DeclarationType, runId: string) {
  const params = new URLSearchParams({ type, runId });
  return callKsm<DeclarationResponse>(
    `/api/v1/payroll/declarations?${params}`,
    {},
    { session },
  );
}

/* ===================== Wage garnishments (saisies sur salaire) ===================== */

export type GarnishmentType = "ALIMONY" | "TAX_LEVY" | "CREDITOR";
export type GarnishmentStatus = "ACTIVE" | "SUSPENDED" | "COMPLETED" | "CANCELLED";

export type GarnishmentResponse = {
  id: string;
  employeeId: string;
  type: GarnishmentType | string;
  beneficiary: string;
  reference: string | null;
  totalAmount: number | string;
  remainingBalance: number | string;
  monthlyAmount: number | string;
  status: GarnishmentStatus | string;
};

export type CreateGarnishmentRequest = {
  organizationId: string;
  employeeId: string;
  type: GarnishmentType;
  beneficiary: string;
  reference?: string | null;
  totalAmount: number;
  monthlyAmount: number;
};

export function listGarnishments(session: AppSession, opts?: { employeeId?: string; organizationId?: string }) {
  const params = new URLSearchParams();
  if (opts?.employeeId) {
    params.set("employeeId", opts.employeeId);
  } else {
    const orgId = opts?.organizationId ?? session.workspace?.organizationId;
    if (!orgId) throw new Error("organizationId is required");
    params.set("organizationId", orgId);
  }
  return callKsm<GarnishmentResponse[]>(
    `/api/v1/payroll/garnishments?${params}`,
    {},
    { session },
  );
}

export function createGarnishment(body: CreateGarnishmentRequest, session: AppSession) {
  return callKsm<GarnishmentResponse>(
    `/api/v1/payroll/garnishments`,
    { method: "POST", body },
    { session },
  );
}

export function cancelGarnishment(id: string, session: AppSession) {
  return callKsm<GarnishmentResponse>(
    `/api/v1/payroll/garnishments/${id}`,
    { method: "DELETE" },
    { session },
  );
}

export function suspendGarnishment(id: string, session: AppSession) {
  return callKsm<GarnishmentResponse>(
    `/api/v1/payroll/garnishments/${id}/suspend`,
    { method: "PUT" },
    { session },
  );
}

export function resumeGarnishment(id: string, session: AppSession) {
  return callKsm<GarnishmentResponse>(
    `/api/v1/payroll/garnishments/${id}/resume`,
    { method: "PUT" },
    { session },
  );
}
