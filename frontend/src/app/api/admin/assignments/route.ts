import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmAssignRole } from "@/server/ksm/modules/admin";
import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

const assignSchema = z.object({
  userId: uuidLike,
  roleId: uuidLike,
  scopeType: z.enum(["SYSTEM", "TENANT", "ORGANIZATION", "AGENCY"]).optional(),
  scopeId: uuidLike.optional().nullable(),
  scope: z.string().optional(),
});

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, assignSchema);
      const ctx = await getKsmContext();
      return ksmAssignRole(body, ctx);
    },
    { status: 201 },
  );
}
