import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmSubmitReview } from "@/server/ksm/modules/reviews";
import { submitReviewSchema } from "@/lib/validation/hrm/review.schema";

export async function PUT(request: Request, ctx: RouteContext<"/api/hrm/reviews/[id]/submit">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, submitReviewSchema);
    const kctx = await getKsmContext();
    return ksmSubmitReview(
      id,
      { noteGlobale: body.noteGlobale, commentaires: body.commentaires, planAction: body.planAction ?? null },
      kctx,
    );
  });
}
