import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import { hasPermission } from "@/server/permissions";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as payrollEmployeesApi from "@/server/ksm/modules/payroll-employees";

/**
 * Single employee lookup. HRM tenants read from hrm-core; standalone-payroll tenants fall back
 * to the payroll-local table (same EmployeeResponse shape, so callers like the payslip preview
 * don't care which mode the org is in).
 */
export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute(["hrm:employee:read", "hrm:payroll:read"], async (session) => {
    if (hasPermission(session, "hrm:employee:read")) {
      const data = await employeesApi.getEmployee(employeeId, session);
      return Response.json({ ok: true, data });
    }
    const local = await payrollEmployeesApi.getPayrollEmployee(employeeId, session);
    return Response.json({ ok: true, data: payrollEmployeesApi.toEmployeeResponse(local) });
  });
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:employee:update", async (session) => {
    const body = (await request.json()) as employeesApi.UpdateEmployeeRequest;
    const data = await employeesApi.updateEmployee(employeeId, body, session);
    return Response.json({ ok: true, data });
  });
}
