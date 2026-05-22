import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmRevokeAssignment } from "@/server/ksm/modules/admin";

export async function DELETE(_: Request, ctx: { params: Promise<{ id: string }> }) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    await ksmRevokeAssignment(id, kctx);
    return null as unknown as undefined;
  });
}
