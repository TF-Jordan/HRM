import "server-only";

import { requirePermissionRoute } from "@/server/handlers";
import * as loansApi from "@/server/ksm/modules/loans";

export async function GET() {
  return requirePermissionRoute("hrm:loan:read", async (session) => {
    const data = await loansApi.listAllLoans(session);
    return Response.json({ ok: true, data });
  });
}
