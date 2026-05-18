import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { HttpError } from "@/lib/types/api";
import {
  ksmCancelEnrollment,
  ksmCompleteEnrollment,
} from "@/server/ksm/modules/trainings";
import { completeEnrollmentSchema } from "@/lib/validation/hrm/training.schema";

const ACTIONS = ["complete", "cancel"] as const;
type Action = (typeof ACTIONS)[number];

export async function PUT(request: Request, ctx: { params: Promise<{ id: string; action: string }> }) {
  const { id, action } = await ctx.params;
  if (!ACTIONS.includes(action as Action)) {
    return withKsmHandler(async () => {
      throw new HttpError({ status: 400, message: `Unknown action: ${action}`, errorCode: "BAD_REQUEST" });
    });
  }
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    if (action === "complete") {
      const body = await parseBody(request, completeEnrollmentSchema);
      return ksmCompleteEnrollment(
        id,
        { note: body.note ?? null, attestationId: body.attestationId ?? null },
        kctx,
      );
    }
    return ksmCancelEnrollment(id, kctx);
  });
}
