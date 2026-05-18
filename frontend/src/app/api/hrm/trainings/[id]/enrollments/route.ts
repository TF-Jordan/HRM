import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmEnrollEmployee, ksmListEnrollments } from "@/server/ksm/modules/trainings";
import { enrollEmployeeSchema } from "@/lib/validation/hrm/training.schema";

export async function GET(_: Request, ctx: { params: Promise<{ id: string }> }) {
  const { id } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListEnrollments(id, kctx);
  });
}

export async function POST(request: Request, ctx: { params: Promise<{ id: string }> }) {
  const { id } = await ctx.params;
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, enrollEmployeeSchema);
      const kctx = await getKsmContext();
      return ksmEnrollEmployee(id, { employeeId: body.employeeId }, kctx);
    },
    { status: 201 },
  );
}
