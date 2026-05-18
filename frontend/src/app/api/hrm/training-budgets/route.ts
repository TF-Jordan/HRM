import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import {
  ksmCreateTrainingBudget,
  ksmListTrainingBudgets,
} from "@/server/ksm/modules/training-budgets";
import { createTrainingBudgetSchema } from "@/lib/validation/hrm/training.schema";

export async function GET(request: Request) {
  const url = new URL(request.url);
  const anneeStr = url.searchParams.get("annee");
  const annee = anneeStr ? Number(anneeStr) : new Date().getFullYear();
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListTrainingBudgets(annee, ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createTrainingBudgetSchema);
      const ctx = await getKsmContext();
      return ksmCreateTrainingBudget(
        {
          annee: Number(body.annee),
          montantAlloue: body.montantAlloue,
          agencyId: ctx.agencyId,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
