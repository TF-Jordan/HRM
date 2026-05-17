import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmSubmitTimesheet } from "@/server/ksm/modules/timesheets";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/timesheets/[id]/submit">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmSubmitTimesheet(id, kctx);
  });
}
