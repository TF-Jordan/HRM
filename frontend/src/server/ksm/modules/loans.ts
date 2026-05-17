import "server-only";

import { callKsm } from "../client";
import type { LoanAdvance, RequestLoanInput } from "@/lib/types/hrm/loan-advance";

type KsmCtx = { tenantId: string; organizationId: string; bearer: string };

export async function ksmListEmployeeLoans(employeeId: string, ctx: KsmCtx): Promise<LoanAdvance[]> {
  return callKsm<LoanAdvance[]>(`/api/v1/hrm/loan-advances/employee/${employeeId}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmRequestLoan(input: RequestLoanInput, ctx: KsmCtx): Promise<LoanAdvance> {
  return callKsm<LoanAdvance>(`/api/v1/hrm/loan-advances`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmApproveLoan(loanId: string, ctx: KsmCtx): Promise<LoanAdvance> {
  return callKsm<LoanAdvance>(`/api/v1/hrm/loan-advances/${loanId}/approve`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmRejectLoan(loanId: string, motif: string, ctx: KsmCtx): Promise<LoanAdvance> {
  return callKsm<LoanAdvance>(`/api/v1/hrm/loan-advances/${loanId}/reject`, {
    method: "PUT",
    body: { motif },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
