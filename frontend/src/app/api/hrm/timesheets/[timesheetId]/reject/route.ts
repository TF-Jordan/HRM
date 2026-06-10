import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as timesheetsApi from "@/server/ksm/modules/timesheets";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ timesheetId: string }> },
) {
  const { timesheetId } = await params;
  return requirePermissionRoute("hrm:timesheet:validate", async (session) => {
    const body = (await request.json()) as { comment: string };
    const data = await timesheetsApi.rejectTimesheet(timesheetId, body.comment, session);
    return Response.json({ ok: true, data });
  });
}
