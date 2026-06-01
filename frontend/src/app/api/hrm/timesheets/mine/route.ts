import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as timesheetsApi from "@/server/ksm/modules/timesheets";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * Timesheets of the currently authenticated employee for a given period.
 * Period defaults to the current month when not provided.
 */
export async function GET(request: NextRequest) {
  const periode =
    request.nextUrl.searchParams.get("periode") ??
    `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, "0")}`;
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, periode, timesheets: [] } });
    }
    const timesheets = await timesheetsApi.listByEmployee(employee.id, periode, session);
    return Response.json({ ok: true, data: { employee, periode, timesheets } });
  });
}
