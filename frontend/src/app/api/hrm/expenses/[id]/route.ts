import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmGetExpense } from "@/server/ksm/modules/expenses";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/expenses/[id]">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmGetExpense(id, kctx);
  });
}
