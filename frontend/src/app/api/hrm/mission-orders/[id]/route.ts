import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmGetMission } from "@/server/ksm/modules/missions";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/mission-orders/[id]">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmGetMission(id, kctx);
  });
}
