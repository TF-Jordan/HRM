import "server-only";

import { callKsm } from "../client";
import type { LeaveRequest, SubmitLeaveInput } from "@/lib/types/hrm/leave-request";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

export async function ksmListEmployeeLeaves(employeeId: string, ctx: KsmCtx): Promise<LeaveRequest[]> {
  return callKsm<LeaveRequest[]>(`/api/v1/hrm/leaves/employee/${employeeId}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListPendingLeaves(ctx: KsmCtx): Promise<LeaveRequest[]> {
  return callKsm<LeaveRequest[]>(`/api/v1/hrm/leaves/pending`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
    query: { organizationId: ctx.organizationId, agencyId: ctx.agencyId ?? undefined },
  });
}

export async function ksmSubmitLeave(input: SubmitLeaveInput, ctx: KsmCtx): Promise<LeaveRequest> {
  return callKsm<LeaveRequest>(`/api/v1/hrm/leaves`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
  });
}

export async function ksmCancelLeave(leaveId: string, ctx: KsmCtx): Promise<LeaveRequest> {
  return callKsm<LeaveRequest>(`/api/v1/hrm/leaves/${leaveId}/cancel`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmApproveLeave(leaveId: string, ctx: KsmCtx): Promise<LeaveRequest> {
  return callKsm<LeaveRequest>(`/api/v1/hrm/leaves/${leaveId}/approve`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmRejectLeave(
  leaveId: string,
  commentaire: string,
  ctx: KsmCtx,
): Promise<LeaveRequest> {
  return callKsm<LeaveRequest>(`/api/v1/hrm/leaves/${leaveId}/reject`, {
    method: "PUT",
    body: { commentaire },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
