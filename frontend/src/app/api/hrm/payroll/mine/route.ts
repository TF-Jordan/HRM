import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const data = await payrollApi.listMyPayslips(session);
    return Response.json({ ok: true, data });
  });
}
