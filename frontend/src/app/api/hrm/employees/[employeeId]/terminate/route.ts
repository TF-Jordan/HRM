import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmTerminateEmployee } from "@/server/ksm/modules/employees";
import { terminateEmployeeSchema } from "@/lib/validation/hrm/employee.schema";

export async function PUT(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/terminate">,
) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, terminateEmployeeSchema);
    const kctx = await getKsmContext();
    return ksmTerminateEmployee(employeeId, body, kctx);
  });
}
