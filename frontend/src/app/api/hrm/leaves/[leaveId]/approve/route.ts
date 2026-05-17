import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmApproveLeave } from "@/server/ksm/modules/leaves";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/leaves/[leaveId]/approve">) {
  const { leaveId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmApproveLeave(leaveId, kctx);
  });
}
