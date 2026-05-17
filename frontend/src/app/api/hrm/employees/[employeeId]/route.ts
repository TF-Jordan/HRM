import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmGetEmployee, ksmUpdateEmployee } from "@/server/ksm/modules/employees";
import { updateEmployeeSchema } from "@/lib/validation/hrm/employee.schema";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/employees/[employeeId]">) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmGetEmployee(employeeId, kctx);
  });
}

export async function PUT(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]">,
) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, updateEmployeeSchema);
    const kctx = await getKsmContext();
    return ksmUpdateEmployee(employeeId, body, kctx);
  });
}
