import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";
import type { LeaveType } from "@/server/ksm/modules/employees";

export type LeaveStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";

export type LeaveResponse = {
  id: string;
  employeeId: string;
  type: LeaveType;
  dateDebut: string;
  dateFin: string;
  nbJours: number | string;
  status: LeaveStatus;
  motif?: string | null;
  valideurPartyId?: string | null;
  valideurDisplayName?: string | null;
  dateValidation?: string | null;
  commentaireValideur?: string | null;
  justificatifFileId?: string | null;
};

/** Leave request joined (BFF-side) with the requesting employee's identity. */
export type EnrichedLeaveResponse = LeaveResponse & {
  employeeName?: string | null;
  employeeMatricule?: string | null;
  employeeDepartment?: string | null;
};

export type SubmitLeaveRequest = {
  employeeId: string;
  type: LeaveType;
  dateDebut: string;
  dateFin: string;
  motif?: string;
  justificatifFileId?: string;
};

export function submitLeave(body: SubmitLeaveRequest, session: AppSession) {
  return callKsm<LeaveResponse>(
    "/api/v1/hrm/leaves",
    { method: "POST", body },
    { session },
  );
}

export function getLeave(leaveRequestId: string, session: AppSession) {
  return callKsm<LeaveResponse>(`/api/v1/hrm/leaves/${leaveRequestId}`, {}, { session });
}

export function listLeavesByEmployee(employeeId: string, session: AppSession) {
  return callKsm<LeaveResponse[]>(
    `/api/v1/hrm/leaves/employee/${employeeId}`,
    {},
    { session },
  );
}

export function listPendingLeaves(session: AppSession, organizationId?: string, agencyId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  if (agencyId ?? session.workspace?.agencyId) {
    params.set("agencyId", (agencyId ?? session.workspace!.agencyId!) as string);
  }
  return callKsm<LeaveResponse[]>(`/api/v1/hrm/leaves/pending?${params}`, {}, { session });
}

/** Every leave request (any status) for the organization — management console & history. */
export function listOrganizationLeaves(
  session: AppSession,
  organizationId?: string,
  agencyId?: string,
) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  if (agencyId ?? session.workspace?.agencyId) {
    params.set("agencyId", (agencyId ?? session.workspace!.agencyId!) as string);
  }
  return callKsm<LeaveResponse[]>(`/api/v1/hrm/leaves?${params}`, {}, { session });
}

export function approveLeave(leaveRequestId: string, session: AppSession) {
  return callKsm<LeaveResponse>(
    `/api/v1/hrm/leaves/${leaveRequestId}/approve`,
    { method: "PUT" },
    { session },
  );
}

export function rejectLeave(leaveRequestId: string, commentaire: string, session: AppSession) {
  return callKsm<LeaveResponse>(
    `/api/v1/hrm/leaves/${leaveRequestId}/reject`,
    { method: "PUT", body: { commentaire } },
    { session },
  );
}

export function cancelLeave(leaveRequestId: string, session: AppSession) {
  return callKsm<LeaveResponse>(
    `/api/v1/hrm/leaves/${leaveRequestId}/cancel`,
    { method: "PUT" },
    { session },
  );
}
