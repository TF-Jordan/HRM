import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmTerminateContract } from "@/server/ksm/modules/contracts";
import { z } from "zod";

const schema = z.object({
  motif: z.string().trim().min(1).max(500),
});

export async function PUT(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/contracts/[contractId]/terminate">,
) {
  const { employeeId, contractId } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, schema);
    const kctx = await getKsmContext();
    return ksmTerminateContract(employeeId, contractId, body.motif, kctx);
  });
}
