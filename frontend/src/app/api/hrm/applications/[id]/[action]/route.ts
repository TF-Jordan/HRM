import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import {
  ksmHireApp,
  ksmInterviewApp,
  ksmOfferApp,
  ksmRejectApp,
  ksmShortlistApp,
} from "@/server/ksm/modules/recruitment";
import { HttpError } from "@/lib/types/api";

export async function PUT(_: Request, ctx: { params: Promise<{ id: string; action: string }> }) {
  const { id, action } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    switch (action) {
      case "shortlist":
        return ksmShortlistApp(id, kctx);
      case "interview":
        return ksmInterviewApp(id, kctx);
      case "offer":
        return ksmOfferApp(id, kctx);
      case "reject":
        return ksmRejectApp(id, kctx);
      case "hire":
        return ksmHireApp(id, kctx);
      default:
        throw new HttpError({ status: 400, message: `Unknown action: ${action}`, errorCode: "BAD_REQUEST" });
    }
  });
}
