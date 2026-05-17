import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmSuspendEmployee } from "@/server/ksm/modules/employees";
import { suspendEmployeeSchema } from "@/lib/validation/hrm/employee.schema";

export async function PUT(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/suspend">,
) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, suspendEmployeeSchema);
    const kctx = await getKsmContext();
    return ksmSuspendEmployee(employeeId, body.reason, kctx);
  });
}
