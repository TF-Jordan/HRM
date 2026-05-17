import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmAddExpenseLine, ksmListExpenseLines } from "@/server/ksm/modules/expenses";
import { addExpenseLineSchema } from "@/lib/validation/hrm/expense.schema";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/expenses/[id]/lines">) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListExpenseLines(id, kctx);
  });
}

export async function POST(request: Request, ctx: RouteContext<"/api/hrm/expenses/[id]/lines">) {
  const { id } = await ctx.params;
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, addExpenseLineSchema);
      const kctx = await getKsmContext();
      return ksmAddExpenseLine(
        id,
        {
          categorie: body.categorie,
          description: body.description ?? null,
          montant: body.montant,
          justificatifFileId: null,
        },
        kctx,
      );
    },
    { status: 201 },
  );
}
