import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmListDependents, ksmAddDependent } from "@/server/ksm/modules/dependents";
import { addDependentSchema } from "@/lib/validation/hrm/dependent.schema";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/employees/[employeeId]/dependents">) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListDependents(employeeId, kctx);
  });
}

export async function POST(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/dependents">,
) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, addDependentSchema);
      const kctx = await getKsmContext();
      return ksmAddDependent(employeeId, body, kctx);
    },
    { status: 201 },
  );
}
