import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmCancelLeave } from "@/server/ksm/modules/leaves";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/leaves/[leaveId]/cancel">) {
  const { leaveId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmCancelLeave(leaveId, kctx);
  });
}
