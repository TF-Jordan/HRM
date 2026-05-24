import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateExpense, ksmListExpenses } from "@/server/ksm/modules/expenses";
import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";
import { createExpenseSchema } from "@/lib/validation/hrm/expense.schema";

const createBody = z.object({ employeeId: uuidLike }).and(createExpenseSchema);

export async function GET(request: Request) {
  const url = new URL(request.url);
  const employeeId = url.searchParams.get("employeeId") ?? "";
  return withKsmHandler(async () => {
    if (!employeeId) return [];
    const ctx = await getKsmContext();
    return ksmListExpenses(employeeId, ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createBody);
      const ctx = await getKsmContext();
      return ksmCreateExpense(
        { employeeId: body.employeeId, periode: body.periode, motif: body.motif },
        ctx,
      );
    },
    { status: 201 },
  );
}
