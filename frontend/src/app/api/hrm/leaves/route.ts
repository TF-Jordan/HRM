import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as leavesApi from "@/server/ksm/modules/leaves";

/**
 * Management console feed: all org leave requests (any status) enriched with the
 * requesting employee's display name / matricule / department so the queue can be
 * read and triaged without per-row round-trips.
 */
export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:leave:approve", async (session) => {
    const sp = request.nextUrl.searchParams;
    const organizationId = sp.get("organizationId") ?? undefined;
    const agencyId = sp.get("agencyId") ?? undefined;

    const [leaves, employees] = await Promise.all([
      leavesApi.listOrganizationLeaves(session, organizationId, agencyId),
      employeesApi.listEmployees(session, { organizationId, agencyId }).catch(() => []),
    ]);

    const byId = new Map(employees.map((e) => [e.id, e]));
    const data = leaves.map((l) => {
      const emp = byId.get(l.employeeId);
      return {
        ...l,
        employeeName: emp?.actorDisplayName ?? null,
        employeeMatricule: emp?.matricule ?? null,
        employeeDepartment: emp?.departmentCode ?? null,
      };
    });
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:leave:create", async (session) => {
    const body = (await request.json()) as leavesApi.SubmitLeaveRequest;
    const data = await leavesApi.submitLeave(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
