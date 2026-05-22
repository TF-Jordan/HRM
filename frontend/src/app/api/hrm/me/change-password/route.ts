import { z } from "zod";
import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmChangeMyPassword } from "@/server/ksm/modules/actors";

const schema = z.object({
  oldPassword: z.string().min(1),
  newPassword: z.string().min(10),
});

export async function POST(request: Request) {
  return withKsmHandler(async () => {
    const body = await parseBody(request, schema);
    const ctx = await getKsmContext();
    await ksmChangeMyPassword(body.oldPassword, body.newPassword, ctx);
    return { success: true };
  });
}
