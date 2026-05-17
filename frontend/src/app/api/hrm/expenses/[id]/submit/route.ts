import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmSubmitExpense } from "@/server/ksm/modules/expenses";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/expenses/[id]/submit">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmSubmitExpense(id, kctx);
  });
}
