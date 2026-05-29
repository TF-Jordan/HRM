import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type ExpenseReportStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "APPROVED"
  | "REJECTED"
  | "REIMBURSED";

export type ExpenseReportResponse = {
  id: string;
  employeeId: string;
  periode: string;
  totalMontant: number | string | null;
  motif: string | null;
  status: ExpenseReportStatus;
  missionOrderId: string | null;
};

export type ExpenseLineResponse = {
  id: string;
  expenseReportId: string;
  description: string;
  montant: number | string;
  categorie: string | null;
  justificatifFileId: string | null;
};

export type CreateExpenseReportRequest = {
  employeeId: string;
  periode: string;
  motif?: string | null;
  missionOrderId?: string | null;
};

export type AddExpenseLineRequest = {
  description: string;
  montant: number | string;
  categorie?: string | null;
  justificatifFileId?: string | null;
};

export function createExpenseReport(body: CreateExpenseReportRequest, session: AppSession) {
  return callKsm<ExpenseReportResponse>(
    "/api/v1/hrm/expenses",
    { method: "POST", body },
    { session },
  );
}

export function getExpenseReport(id: string, session: AppSession) {
  return callKsm<ExpenseReportResponse>(`/api/v1/hrm/expenses/${id}`, {}, { session });
}

export function listExpenseReportsByEmployee(employeeId: string, session: AppSession) {
  const params = new URLSearchParams({ employeeId });
  return callKsm<ExpenseReportResponse[]>(`/api/v1/hrm/expenses?${params}`, {}, { session });
}

export function listAllExpenseReports(
  session: AppSession,
  opts: { organizationId?: string; status?: ExpenseReportStatus } = {},
) {
  const orgId = opts.organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  if (opts.status) params.set("status", opts.status);
  return callKsm<ExpenseReportResponse[]>(`/api/v1/hrm/expenses?${params}`, {}, { session });
}

export function addExpenseLine(id: string, body: AddExpenseLineRequest, session: AppSession) {
  return callKsm<ExpenseLineResponse>(
    `/api/v1/hrm/expenses/${id}/lines`,
    { method: "POST", body },
    { session },
  );
}

export function listExpenseLines(id: string, session: AppSession) {
  return callKsm<ExpenseLineResponse[]>(`/api/v1/hrm/expenses/${id}/lines`, {}, { session });
}

export function submitExpenseReport(id: string, session: AppSession) {
  return callKsm<ExpenseReportResponse>(
    `/api/v1/hrm/expenses/${id}/submit`,
    { method: "PUT" },
    { session },
  );
}

export function approveExpenseReport(id: string, session: AppSession) {
  return callKsm<ExpenseReportResponse>(
    `/api/v1/hrm/expenses/${id}/approve`,
    { method: "PUT" },
    { session },
  );
}

export function rejectExpenseReport(id: string, session: AppSession) {
  return callKsm<ExpenseReportResponse>(
    `/api/v1/hrm/expenses/${id}/reject`,
    { method: "PUT" },
    { session },
  );
}

export function reimburseExpenseReport(id: string, session: AppSession) {
  return callKsm<ExpenseReportResponse>(
    `/api/v1/hrm/expenses/${id}/reimburse`,
    { method: "PUT" },
    { session },
  );
}
