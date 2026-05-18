import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmCloseJobOffer, ksmPublishJobOffer } from "@/server/ksm/modules/recruitment";
import { HttpError } from "@/lib/types/api";

export async function PUT(_: Request, ctx: { params: Promise<{ id: string; action: string }> }) {
  const { id, action } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    if (action === "publish") return ksmPublishJobOffer(id, kctx);
    if (action === "close") return ksmCloseJobOffer(id, kctx);
    throw new HttpError({ status: 400, message: `Unknown action: ${action}`, errorCode: "BAD_REQUEST" });
  });
}
