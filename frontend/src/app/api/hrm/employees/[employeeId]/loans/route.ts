import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListEmployeeLoans } from "@/server/ksm/modules/loans";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/employees/[employeeId]/loans">) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListEmployeeLoans(employeeId, kctx);
  });
}
