import "server-only";

import { callKsm } from "../client";
import type { CreateMissionOrderInput, MissionOrder } from "@/lib/types/hrm/mission-order";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

export async function ksmListEmployeeMissions(employeeId: string, ctx: KsmCtx): Promise<MissionOrder[]> {
  return callKsm<MissionOrder[]>(`/api/v1/hrm/mission-orders`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { employeeId },
  });
}

export async function ksmGetMission(id: string, ctx: KsmCtx): Promise<MissionOrder> {
  return callKsm<MissionOrder>(`/api/v1/hrm/mission-orders/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateMission(input: CreateMissionOrderInput, ctx: KsmCtx): Promise<MissionOrder> {
  return callKsm<MissionOrder>(`/api/v1/hrm/mission-orders`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
  });
}

async function transition(id: string, action: string, ctx: KsmCtx): Promise<MissionOrder> {
  return callKsm<MissionOrder>(`/api/v1/hrm/mission-orders/${id}/${action}`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export const ksmApproveMission = (id: string, ctx: KsmCtx) => transition(id, "approve", ctx);
export const ksmStartMission = (id: string, ctx: KsmCtx) => transition(id, "start", ctx);
export const ksmCompleteMission = (id: string, ctx: KsmCtx) => transition(id, "complete", ctx);
export const ksmCancelMission = (id: string, ctx: KsmCtx) => transition(id, "cancel", ctx);
