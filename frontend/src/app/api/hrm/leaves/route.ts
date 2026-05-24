import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmSubmitLeave } from "@/server/ksm/modules/leaves";
import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";
import { submitLeaveSchema } from "@/lib/validation/hrm/leave.schema";

const bodySchema = z.object({ employeeId: uuidLike }).and(submitLeaveSchema);

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, bodySchema);
      const ctx = await getKsmContext();
      return ksmSubmitLeave(
        {
          employeeId: body.employeeId,
          type: body.type,
          dateDebut: body.dateDebut,
          dateFin: body.dateFin,
          motif: body.motif ?? null,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
