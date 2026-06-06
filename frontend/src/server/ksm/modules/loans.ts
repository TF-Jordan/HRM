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
  dateDebut: string | null;
  nbEcheances: number;
  motif: string | null;
  approvedBy: string | null;
};

export type LoanRepaymentResponse = {
  id: string;
  loanId: string;
  runId: string | null;
  period: string | null;
  montant: number | string;
  soldeApres: number | string;
  recordedAt: string | null;
};

/** Self-service: list the calling user's own loans. */
export async function listMyLoans(session: AppSession) {
  return callKsm<LoanAdvanceResponse[]>(
    "/api/v1/hrm/loan-advances/mine",
    { method: "GET" },
    { session },
  );
}

/** Self-service: real repayment history (actual payroll deductions) of one of the caller's loans. */
export async function listMyLoanRepayments(loanId: string, session: AppSession) {
  return callKsm<LoanRepaymentResponse[]>(
    `/api/v1/hrm/loan-advances/mine/${loanId}/repayments`,
    { method: "GET" },
    { session },
  );
}

/** Self-service: request a new loan. */
export async function requestMyLoan(
  body: { montant: number; nbEcheances: number; motif: string },
  session: AppSession,
) {
  return callKsm<LoanAdvanceResponse>(
    "/api/v1/hrm/loan-advances/mine",
    { method: "POST", body: JSON.stringify(body) },
    { session },
  );
}

/** Admin: list loans for an employee. */
export async function listLoansByEmployee(
  employeeId: string,
  session: AppSession,
) {
  return callKsm<LoanAdvanceResponse[]>(
    `/api/v1/hrm/loan-advances/employee/${employeeId}`,
    { method: "GET" },
    { session },
  );
}

/** Admin: get a single loan. */
export async function getLoanAdvance(
  loanAdvanceId: string,
  session: AppSession,
) {
  return callKsm<LoanAdvanceResponse>(
    `/api/v1/hrm/loan-advances/${loanAdvanceId}`,
    { method: "GET" },
    { session },
  );
}

/** Admin: list all loans for the organization. */
export async function listAllLoans(session: AppSession) {
  return callKsm<LoanAdvanceResponse[]>(
    "/api/v1/hrm/loan-advances",
    { method: "GET" },
    { session },
  );
}

/** Admin: approve a loan. */
export async function approveLoan(loanAdvanceId: string, session: AppSession) {
  return callKsm<LoanAdvanceResponse>(
    `/api/v1/hrm/loan-advances/${loanAdvanceId}/approve`,
    { method: "PUT" },
    { session },
  );
}

/** Admin: reject a loan. */
export async function rejectLoan(
  loanAdvanceId: string,
  motif: string,
  session: AppSession,
) {
  return callKsm<LoanAdvanceResponse>(
    `/api/v1/hrm/loan-advances/${loanAdvanceId}/reject`,
    { method: "PUT", body: JSON.stringify({ motif }) },
    { session },
  );
}
