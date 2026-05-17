import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmValidateTimesheet } from "@/server/ksm/modules/timesheets-mgr";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/timesheets/[id]/validate">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmValidateTimesheet(id, kctx);
  });
}
