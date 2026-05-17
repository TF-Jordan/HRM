import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmRejectExpense } from "@/server/ksm/modules/expenses";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/expenses/[id]/reject">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmRejectExpense(id, kctx);
  });
}
