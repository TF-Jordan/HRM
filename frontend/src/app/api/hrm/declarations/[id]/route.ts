import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmGetDeclaration } from "@/server/ksm/modules/declarations";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/declarations/[id]">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmGetDeclaration(id, kctx);
  });
}
