import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListAssignmentsByRole } from "@/server/ksm/modules/admin";

export async function GET(_: Request, ctx: { params: Promise<{ id: string }> }) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListAssignmentsByRole(id, kctx);
  });
}
