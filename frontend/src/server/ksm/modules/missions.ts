import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type MissionOrderStatus =
  | "DRAFT"
  | "PENDING_ACCEPTANCE"
  | "APPROVED"
  | "DECLINED"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELLED";

export type MissionOrderResponse = {
  id: string;
  employeeId: string;
  destination: string;
  objet: string;
  dateDebut: string;
  dateFin: string;
  montantAvance: number | string | null;
  centreCout: string | null;
  status: MissionOrderStatus;
  parentOrderId: string | null;
  decisionReason: string | null;
  decidedAt: string | null;
};

export type CreateMissionOrderRequest = {
  employeeId: string;
  destination: string;
  objet: string;
  dateDebut: string;
  dateFin: string;
  montantAvance?: number | string | null;
  centreCout?: string | null;
};

export type AmendMissionOrderRequest = {
  destination: string;
  objet: string;
  dateDebut: string;
  dateFin: string;
  montantAvance?: number | string | null;
  centreCout?: string | null;
};

export function createMissionOrder(body: CreateMissionOrderRequest, session: AppSession) {
  return callKsm<MissionOrderResponse>(
    "/api/v1/hrm/mission-orders",
    { method: "POST", body },
    { session },
  );
}

export function getMissionOrder(missionOrderId: string, session: AppSession) {
  return callKsm<MissionOrderResponse>(
    `/api/v1/hrm/mission-orders/${missionOrderId}`,
    {},
    { session },
  );
}

export function listMissionOrdersByEmployee(employeeId: string, session: AppSession) {
  const params = new URLSearchParams({ employeeId });
  return callKsm<MissionOrderResponse[]>(
    `/api/v1/hrm/mission-orders?${params}`,
    {},
    { session },
  );
}

export function listAllMissionOrders(
  session: AppSession,
  opts: { organizationId?: string; status?: MissionOrderStatus } = {},
) {
  const orgId = opts.organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  if (opts.status) params.set("status", opts.status);
  return callKsm<MissionOrderResponse[]>(
    `/api/v1/hrm/mission-orders?${params}`,
    {},
    { session },
  );
}

export function listPendingAcceptance(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  return callKsm<MissionOrderResponse[]>(
    `/api/v1/hrm/mission-orders/pending-acceptance?${params}`,
    {},
    { session },
  );
}

export function listDeclined(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  return callKsm<MissionOrderResponse[]>(
    `/api/v1/hrm/mission-orders/declined?${params}`,
    {},
    { session },
  );
}

export function issueMissionOrder(missionOrderId: string, session: AppSession) {
  return callKsm<MissionOrderResponse>(
    `/api/v1/hrm/mission-orders/${missionOrderId}/issue`,
    { method: "PUT" },
    { session },
  );
}

export function acceptMissionOrder(missionOrderId: string, session: AppSession) {
  return callKsm<MissionOrderResponse>(
    `/api/v1/hrm/mission-orders/${missionOrderId}/accept`,
    { method: "PUT" },
    { session },
  );
}

export function declineMissionOrder(missionOrderId: string, reason: string, session: AppSession) {
  return callKsm<MissionOrderResponse>(
    `/api/v1/hrm/mission-orders/${missionOrderId}/decline`,
    { method: "PUT", body: { reason } },
    { session },
  );
}

export function amendMissionOrder(
  parentMissionOrderId: string,
  body: AmendMissionOrderRequest,
  session: AppSession,
) {
  return callKsm<MissionOrderResponse>(
    `/api/v1/hrm/mission-orders/${parentMissionOrderId}/amend`,
    { method: "POST", body },
    { session },
  );
}

export function startMissionOrder(missionOrderId: string, session: AppSession) {
  return callKsm<MissionOrderResponse>(
    `/api/v1/hrm/mission-orders/${missionOrderId}/start`,
    { method: "PUT" },
    { session },
  );
}

export function completeMissionOrder(missionOrderId: string, session: AppSession) {
  return callKsm<MissionOrderResponse>(
    `/api/v1/hrm/mission-orders/${missionOrderId}/complete`,
    { method: "PUT" },
    { session },
  );
}

export function cancelMissionOrder(missionOrderId: string, session: AppSession) {
  return callKsm<MissionOrderResponse>(
    `/api/v1/hrm/mission-orders/${missionOrderId}/cancel`,
    { method: "PUT" },
    { session },
  );
}
