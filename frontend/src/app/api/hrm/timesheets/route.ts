import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as timesheetsApi from "@/server/ksm/modules/timesheets";

/**
 * Time & attendance console feed: all org timesheets for a period, enriched with the
 * employee's name / matricule / department so the queue can be read and validated
 * without per-row round-trips.
 */
export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:timesheet:read", async (session) => {
    const periode = request.nextUrl.searchParams.get("periode") ?? currentPeriode();
    const [timesheets, employees] = await Promise.all([
      timesheetsApi.listByOrganization(periode, session),
      employeesApi.listEmployees(session).catch(() => []),
    ]);
    const byId = new Map(employees.map((e) => [e.id, e]));
    const data = timesheets.map((ts) => {
      const emp = byId.get(ts.employeeId);
      return {
        ...ts,
        employeeName: emp?.actorDisplayName ?? null,
        employeeMatricule: emp?.matricule ?? null,
        employeeDepartment: emp?.departmentCode ?? null,
      };
    });
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
