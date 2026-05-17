import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmApproveLoan } from "@/server/ksm/modules/loans";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/loans/[loanId]/approve">) {
  const { loanId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmApproveLoan(loanId, kctx);
  });
}
