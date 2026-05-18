import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { HttpError } from "@/lib/types/api";
import { ksmEngageBudget, ksmRealiseBudget } from "@/server/ksm/modules/training-budgets";
import { montantSchema } from "@/lib/validation/hrm/training.schema";

const ACTIONS = ["engage", "realiser"] as const;
type Action = (typeof ACTIONS)[number];

export async function PUT(request: Request, ctx: { params: Promise<{ id: string; action: string }> }) {
  const { id, action } = await ctx.params;
  if (!ACTIONS.includes(action as Action)) {
    return withKsmHandler(async () => {
      throw new HttpError({ status: 400, message: `Unknown action: ${action}`, errorCode: "BAD_REQUEST" });
    });
  }
  return withKsmHandler(async () => {
    const body = await parseBody(request, montantSchema);
    const kctx = await getKsmContext();
    if (action === "engage") {
      return ksmEngageBudget(id, { montant: body.montant }, kctx);
    }
    return ksmRealiseBudget(id, { montant: body.montant }, kctx);
  });
}
