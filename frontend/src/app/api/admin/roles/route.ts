import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateRole, ksmListRoles } from "@/server/ksm/modules/admin";
import { z } from "zod";

const createRoleSchema = z.object({
  code: z.string().trim().min(1).max(60),
  name: z.string().trim().min(1).max(160),
  scopeType: z.enum(["SYSTEM", "TENANT", "ORGANIZATION", "AGENCY"]).optional(),
  permissions: z.array(z.string().trim().min(1)).min(1),
});

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListRoles(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createRoleSchema);
      const ctx = await getKsmContext();
      return ksmCreateRole(body, ctx);
    },
    { status: 201 },
  );
}
