import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmAddObjective, ksmListObjectives } from "@/server/ksm/modules/reviews";
import { addObjectiveSchema } from "@/lib/validation/hrm/review.schema";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/reviews/[id]/objectives">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListObjectives(id, kctx);
  });
}

export async function POST(request: Request, ctx: RouteContext<"/api/hrm/reviews/[id]/objectives">) {
  const { id } = await ctx.params;
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, addObjectiveSchema);
      const kctx = await getKsmContext();
      return ksmAddObjective(id, body, kctx);
    },
    { status: 201 },
  );
}
