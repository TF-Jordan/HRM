import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmRejectLeave } from "@/server/ksm/modules/leaves";
import { rejectLeaveSchema } from "@/lib/validation/hrm/leave.schema";

export async function PUT(request: Request, ctx: RouteContext<"/api/hrm/leaves/[leaveId]/reject">) {
  const { leaveId } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, rejectLeaveSchema);
    const kctx = await getKsmContext();
    return ksmRejectLeave(leaveId, body.commentaire, kctx);
  });
}
