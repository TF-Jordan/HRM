import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import {
  ksmAcknowledgeDeclaration,
  ksmGenerateDeclaration,
  ksmSubmitDeclaration,
} from "@/server/ksm/modules/declarations";
import { HttpError } from "@/lib/types/api";

export async function PUT(
  _: Request,
  ctx: RouteContext<"/api/hrm/declarations/[id]/[action]">,
) {
  const { id, action } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    switch (action) {
      case "generate":
        return ksmGenerateDeclaration(id, null, kctx);
      case "submit":
        return ksmSubmitDeclaration(id, kctx);
      case "acknowledge":
        return ksmAcknowledgeDeclaration(id, kctx);
      default:
        throw new HttpError({ status: 400, message: `Unknown action: ${action}`, errorCode: "BAD_REQUEST" });
    }
  });
}
