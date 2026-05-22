import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmDeleteRole, ksmGetRole, ksmUpdateRole } from "@/server/ksm/modules/admin";
import { z } from "zod";

const updateRoleSchema = z.object({
  name: z.string().trim().min(1).max(160).optional(),
  permissions: z.array(z.string().trim().min(1)).optional(),
});

export async function GET(_: Request, ctx: { params: Promise<{ id: string }> }) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmGetRole(id, kctx);
  });
}

export async function PUT(request: Request, ctx: { params: Promise<{ id: string }> }) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, updateRoleSchema);
    const kctx = await getKsmContext();
    return ksmUpdateRole(id, body, kctx);
  });
}

export async function DELETE(_: Request, ctx: { params: Promise<{ id: string }> }) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    await ksmDeleteRole(id, kctx);
    return null as unknown as undefined;
  });
}
