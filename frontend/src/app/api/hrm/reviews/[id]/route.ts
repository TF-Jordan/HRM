import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmGetReview } from "@/server/ksm/modules/reviews";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/reviews/[id]">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmGetReview(id, kctx);
  });
}
