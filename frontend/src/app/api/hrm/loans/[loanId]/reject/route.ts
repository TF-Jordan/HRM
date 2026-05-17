import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmRejectLoan } from "@/server/ksm/modules/loans";
import { z } from "zod";

const schema = z.object({ motif: z.string().trim().min(1).max(500) });

export async function PUT(request: Request, ctx: RouteContext<"/api/hrm/loans/[loanId]/reject">) {
  const { loanId } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, schema);
    const kctx = await getKsmContext();
    return ksmRejectLoan(loanId, body.motif, kctx);
  });
}
