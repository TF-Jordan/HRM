import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import {
  ksmApproveMission,
  ksmCancelMission,
  ksmCompleteMission,
  ksmStartMission,
} from "@/server/ksm/modules/missions";
import { HttpError } from "@/lib/types/api";

export async function PUT(
  _: Request,
  ctx: RouteContext<"/api/hrm/mission-orders/[id]/[action]">,
) {
  const { id, action } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    switch (action) {
      case "approve":
        return ksmApproveMission(id, kctx);
      case "start":
        return ksmStartMission(id, kctx);
      case "complete":
        return ksmCompleteMission(id, kctx);
      case "cancel":
        return ksmCancelMission(id, kctx);
      default:
        throw new HttpError({ status: 400, message: `Unknown action: ${action}`, errorCode: "BAD_REQUEST" });
    }
  });
}
