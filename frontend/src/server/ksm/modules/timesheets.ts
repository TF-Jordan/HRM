import "server-only";

import { callKsm } from "../client";
import type { CreateTimesheetInput, Timesheet } from "@/lib/types/hrm/timesheet";

type KsmCtx = { tenantId: string; organizationId: string; bearer: string };

export async function ksmListEmployeeTimesheets(
  employeeId: string,
  periode: string | null,
  ctx: KsmCtx,
): Promise<Timesheet[]> {
  return callKsm<Timesheet[]>(`/api/v1/hrm/timesheets/employee/${employeeId}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { periode: periode ?? undefined },
  });
}

export async function ksmCreateTimesheet(
  input: CreateTimesheetInput,
  ctx: KsmCtx,
): Promise<Timesheet> {
  return callKsm<Timesheet>(`/api/v1/hrm/timesheets`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmSubmitTimesheet(timesheetId: string, ctx: KsmCtx): Promise<Timesheet> {
  return callKsm<Timesheet>(`/api/v1/hrm/timesheets/${timesheetId}/submit`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
