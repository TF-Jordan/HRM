import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as missionsApi from "@/server/ksm/modules/missions";

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:mission:create", async (session) => {
    const body = (await request.json()) as missionsApi.CreateMissionOrderRequest;
    const data = await missionsApi.createMissionOrder(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:mission:read", async (session) => {
    const sp = request.nextUrl.searchParams;
    const employeeId = sp.get("employeeId");
    if (employeeId) {
      const data = await missionsApi.listMissionOrdersByEmployee(employeeId, session);
      return Response.json({ ok: true, data });
    }
    const status = sp.get("status") ?? undefined;
    const [orders, employees] = await Promise.all([
      missionsApi.listAllMissionOrders(session, {
        organizationId: sp.get("organizationId") ?? undefined,
        status: status as missionsApi.MissionOrderStatus | undefined,
      }),
      employeesApi.listEmployees(session).catch(() => []),
    ]);
    const byId = new Map(employees.map((e) => [e.id, e]));
    const data = orders.map((o) => {
      const emp = byId.get(o.employeeId);
      return {
        ...o,
        employeeName: emp?.actorDisplayName ?? null,
        employeeMatricule: emp?.matricule ?? null,
        employeeDepartment: emp?.departmentCode ?? null,
      };
    });
    return Response.json({ ok: true, data });
  });
}
