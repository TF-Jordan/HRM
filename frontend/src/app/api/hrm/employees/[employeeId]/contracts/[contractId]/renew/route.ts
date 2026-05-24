import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmRenewContract } from "@/server/ksm/modules/contracts";
import { z } from "zod";

const schema = z.object({
  newDateFin: z.iso.date(),
});

export async function PUT(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/contracts/[contractId]/renew">,
) {
  const { employeeId, contractId } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, schema);
    const kctx = await getKsmContext();
    return ksmRenewContract(employeeId, contractId, body.newDateFin, kctx);
  });
}
