import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmAdminCreateUser, ksmListUsers } from "@/server/ksm/modules/admin";
import { z } from "zod";

const createUserSchema = z.object({
  actorId: z.uuid(),
  username: z.string().trim().min(1).max(160),
  email: z.email(),
  phoneNumber: z.string().trim().max(30).optional().nullable().or(z.literal("").transform(() => null)),
  password: z.string().trim().min(8).optional().nullable().or(z.literal("").transform(() => null)),
  sendWelcomeEmail: z.boolean().optional(),
});

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListUsers(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createUserSchema);
      const ctx = await getKsmContext();
      return ksmAdminCreateUser(
        {
          actorId: body.actorId,
          username: body.username,
          email: body.email,
          phoneNumber: body.phoneNumber ?? null,
          password: body.password ?? null,
          sendWelcomeEmail: body.sendWelcomeEmail ?? true,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
