import "server-only";

import { requirePermissionRoute } from "@/server/handlers";
import * as api from "@/server/ksm/modules/payroll-employees";

export async function GET() {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const data = await api.getPayrollEmployeesCsvTemplate(session);
    return Response.json({ ok: true, data });
  });
}
