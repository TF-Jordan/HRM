import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import { hasPermission } from "@/server/permissions";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as payrollEmployeesApi from "@/server/ksm/modules/payroll-employees";
import {
  createEmployeeOrchestrated,
  type CreateEmployeeOrchestratedInput,
} from "@/server/orchestration/create-employee";

/**
 * Employee roster for the org. HRM tenants read it from hrm-core; standalone-payroll tenants
 * (no hrm:employee:read) read it from the payroll-local table so every payroll screen — monthly
 * variables, run entries, garnishments… — works the same in both modes. Hence the dual gate.
 */
export async function GET(request: NextRequest) {
  return requirePermissionRoute(["hrm:employee:read", "hrm:payroll:read"], async (session) => {
    const sp = request.nextUrl.searchParams;
    const organizationId = sp.get("organizationId") ?? undefined;
    if (hasPermission(session, "hrm:employee:read")) {
      const data = await employeesApi.listEmployees(session, {
        organizationId,
        agencyId: sp.get("agencyId") ?? undefined,
      });
      return Response.json({ ok: true, data });
    }
    // Standalone payroll: project the payroll-owned employees onto the EmployeeResponse shape
    // the payroll UI already consumes.
    const local = await payrollEmployeesApi.listPayrollEmployees(session, organizationId);
    return Response.json({ ok: true, data: local.map(payrollEmployeesApi.toEmployeeResponse) });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:employee:create", async (session) => {
    const body = (await request.json()) as CreateEmployeeOrchestratedInput;
    const result = await createEmployeeOrchestrated(body, session);
    return Response.json({ ok: true, data: result }, { status: 201 });
  });
}
