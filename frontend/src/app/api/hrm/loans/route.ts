import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmRequestLoan } from "@/server/ksm/modules/loans";
import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";
import { requestLoanSchema } from "@/lib/validation/hrm/loan.schema";

const bodySchema = z.object({ employeeId: uuidLike }).and(requestLoanSchema);

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, bodySchema);
      const ctx = await getKsmContext();
      return ksmRequestLoan(
        {
          employeeId: body.employeeId,
          montant: body.montant,
          motif: body.motif ?? null,
          nbEcheances: body.nbEcheances,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
