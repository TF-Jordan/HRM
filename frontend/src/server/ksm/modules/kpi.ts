import "server-only";

import { callKsm } from "../client";
import type { CreateRhKpiSnapshotInput, RhKpiSnapshot } from "@/lib/types/hrm/kpi";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

export async function ksmListKpiSnapshots(ctx: KsmCtx): Promise<RhKpiSnapshot[]> {
  return callKsm<RhKpiSnapshot[]>(`/api/v1/hrm/kpi`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { orgId: ctx.organizationId },
  });
}

export async function ksmGetKpiSnapshot(id: string, ctx: KsmCtx): Promise<RhKpiSnapshot> {
  return callKsm<RhKpiSnapshot>(`/api/v1/hrm/kpi/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateKpiSnapshot(
  input: CreateRhKpiSnapshotInput & { organizationId: string },
  ctx: KsmCtx,
): Promise<RhKpiSnapshot> {
  return callKsm<RhKpiSnapshot>(`/api/v1/hrm/kpi`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
