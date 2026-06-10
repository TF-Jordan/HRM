import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import { getPayrollOnboardingManifest } from "@/server/ksm/modules/payroll-onboarding";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const data = await getPayrollOnboardingManifest(session);
    return Response.json({ ok: true, data });
  });
}
