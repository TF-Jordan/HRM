import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmRequestLoan, ksmListEmployeeLoans } from "@/server/ksm/modules/loans";
import { ksmListEmployees } from "@/server/ksm/modules/employees";
import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";
import { requestLoanSchema } from "@/lib/validation/hrm/loan.schema";
import type { LoanAdvanceWithEmployee } from "@/lib/types/hrm/loan-advance";

/** Aggregates all org loans (KSM has no org-wide endpoint), mirrors /loans/pending. */
export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    const employees = await ksmListEmployees(ctx);
    const all = await Promise.all(
      employees.map((e) =>
        ksmListEmployeeLoans(e.id, ctx)
          .then((loans) =>
            loans.map(
              (l): LoanAdvanceWithEmployee => ({
                ...l,
                employeeName: e.actorDisplayName,
                employeeMatricule: e.matricule,
              }),
            ),
          )
          .catch(() => [] as LoanAdvanceWithEmployee[]),
      ),
    );
    return all.flat();
  });
}

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
