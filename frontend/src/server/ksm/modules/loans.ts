import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type LoanAdvanceStatus =
  | "PENDING"
  | "APPROVED"
  | "IN_REPAYMENT"
  | "FULLY_REPAID"
  | "REJECTED";

export type LoanAdvanceResponse = {
  id: string;
  employeeId: string;
  montant: number | string;
  soldeRestant: number | string;
  mensualite: number | string;
  status: LoanAdvanceStatus;
  dateDebut: string;
  nbEcheances: number;
  motif: string | null;
  approvedBy: string | null;
};

export type RequestLoanAdvanceRequest = {
  employeeId: string;
  montant: number | string;
  nbEcheances: number;
  motif?: string | null;
};

export function requestLoanAdvance(body: RequestLoanAdvanceRequest, session: AppSession) {
  return callKsm<LoanAdvanceResponse>(
    "/api/v1/hrm/loan-advances",
    { method: "POST", body },
    { session },
  );
}

export function getLoanAdvance(id: string, session: AppSession) {
  return callKsm<LoanAdvanceResponse>(`/api/v1/hrm/loan-advances/${id}`, {}, { session });
}

export function listLoanAdvancesByEmployee(employeeId: string, session: AppSession) {
  return callKsm<LoanAdvanceResponse[]>(
    `/api/v1/hrm/loan-advances/employee/${employeeId}`,
    {},
    { session },
  );
}

export function listLoanAdvancesByOrganization(
  session: AppSession,
  opts: { status?: LoanAdvanceStatus } = {},
) {
  const params = new URLSearchParams();
  if (opts.status) params.set("status", opts.status);
  const qs = params.toString();
  return callKsm<LoanAdvanceResponse[]>(
    `/api/v1/hrm/loan-advances${qs ? `?${qs}` : ""}`,
    {},
    { session },
  );
}

export function approveLoanAdvance(id: string, session: AppSession) {
  return callKsm<LoanAdvanceResponse>(
    `/api/v1/hrm/loan-advances/${id}/approve`,
    { method: "PUT" },
    { session },
  );
}

export function rejectLoanAdvance(id: string, motif: string, session: AppSession) {
  return callKsm<LoanAdvanceResponse>(
    `/api/v1/hrm/loan-advances/${id}/reject`,
    { method: "PUT", body: { motif } },
    { session },
  );
}
