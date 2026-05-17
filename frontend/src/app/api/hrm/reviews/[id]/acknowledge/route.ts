import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmAcknowledgeReview } from "@/server/ksm/modules/reviews";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/reviews/[id]/acknowledge">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmAcknowledgeReview(id, kctx);
  });
}
