import "server-only";

import { callKsm } from "../client";
import type { Timesheet } from "@/lib/types/hrm/timesheet";

type KsmCtx = { tenantId: string; organizationId: string; bearer: string };

export async function ksmListOrgTimesheets(
  periode: string | null,
  ctx: KsmCtx,
): Promise<Timesheet[]> {
  return callKsm<Timesheet[]>(`/api/v1/hrm/timesheets`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { organizationId: ctx.organizationId, periode: periode ?? undefined },
  });
}

export async function ksmValidateTimesheet(timesheetId: string, ctx: KsmCtx): Promise<Timesheet> {
  return callKsm<Timesheet>(`/api/v1/hrm/timesheets/${timesheetId}/validate`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
