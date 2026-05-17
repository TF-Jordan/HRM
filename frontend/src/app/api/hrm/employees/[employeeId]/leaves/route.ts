import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListEmployeeLeaves } from "@/server/ksm/modules/leaves";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/employees/[employeeId]/leaves">) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListEmployeeLeaves(employeeId, kctx);
  });
}
