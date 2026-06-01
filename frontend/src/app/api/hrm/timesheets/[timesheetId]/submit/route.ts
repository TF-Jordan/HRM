import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as timesheetsApi from "@/server/ksm/modules/timesheets";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ timesheetId: string }> },
) {
  const { timesheetId } = await params;
  return requirePermissionRoute("hrm:timesheet:create", async (session) => {
    const data = await timesheetsApi.submitTimesheet(timesheetId, session);
    return Response.json({ ok: true, data });
  });
}
