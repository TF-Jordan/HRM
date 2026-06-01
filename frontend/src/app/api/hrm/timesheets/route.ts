import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as timesheetsApi from "@/server/ksm/modules/timesheets";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:timesheet:read", async (session) => {
    const periode = request.nextUrl.searchParams.get("periode") ?? currentPeriode();
    const data = await timesheetsApi.listByOrganization(periode, session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:timesheet:create", async (session) => {
    const body = (await request.json()) as timesheetsApi.CreateTimesheetRequest;
    const data = await timesheetsApi.createTimesheet(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}

function currentPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}
