import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmReactivateEmployee } from "@/server/ksm/modules/employees";

export async function PUT(
  _: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/reactivate">,
) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmReactivateEmployee(employeeId, kctx);
  });
}
