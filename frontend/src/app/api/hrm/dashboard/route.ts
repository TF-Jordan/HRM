import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListEmployees } from "@/server/ksm/modules/employees";

/**
 * Aggregated KPIs for the Admin RH dashboard. Computed on the fly from the
 * employee list (good enough for Phase 2). A dedicated KSM
 * /api/v1/hrm/dashboards/summary endpoint would replace this later.
 */
export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    const employees = await ksmListEmployees(ctx);
    const active = employees.filter((e) => e.status === "ACTIVE").length;
    const onLeave = employees.filter((e) => e.status === "ON_LEAVE").length;
    const suspended = employees.filter((e) => e.status === "SUSPENDED").length;
    const terminated = employees.filter((e) => e.status === "TERMINATED").length;
    return {
      headcount: {
        total: employees.length,
        active,
        onLeave,
        suspended,
        terminated,
      },
    };
  });
}
