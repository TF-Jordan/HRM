import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { HttpError } from "@/lib/types/api";
import {
  ksmCancelTraining,
  ksmCompleteTraining,
  ksmStartTraining,
} from "@/server/ksm/modules/trainings";

const ACTIONS = ["start", "complete", "cancel"] as const;
type Action = (typeof ACTIONS)[number];

export async function PUT(_: Request, ctx: { params: Promise<{ id: string; action: string }> }) {
  const { id, action } = await ctx.params;
  if (!ACTIONS.includes(action as Action)) {
    return withKsmHandler(async () => {
      throw new HttpError({ status: 400, message: `Unknown action: ${action}`, errorCode: "BAD_REQUEST" });
    });
  }
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    switch (action as Action) {
      case "start":
        return ksmStartTraining(id, kctx);
      case "complete":
        return ksmCompleteTraining(id, kctx);
      case "cancel":
        return ksmCancelTraining(id, kctx);
    }
  });
}
