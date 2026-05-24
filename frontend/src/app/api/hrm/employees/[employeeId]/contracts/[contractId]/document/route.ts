import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmAttachContractDocument } from "@/server/ksm/modules/contracts";
import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

const schema = z.object({
  documentFileId: uuidLike,
});

export async function PUT(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/contracts/[contractId]/document">,
) {
  const { employeeId, contractId } = await ctx.params;
  return withKsmHandler(async () => {
    const body = await parseBody(request, schema);
    const kctx = await getKsmContext();
    return ksmAttachContractDocument(employeeId, contractId, body.documentFileId, kctx);
  });
}
