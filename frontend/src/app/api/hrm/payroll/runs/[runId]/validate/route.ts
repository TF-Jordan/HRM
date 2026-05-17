import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmValidatePayrollRun } from "@/server/ksm/modules/payroll";

export async function PUT(_: Request, ctx: RouteContext<"/api/hrm/payroll/runs/[runId]/validate">) {
  const { runId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmValidatePayrollRun(runId, kctx);
  });
}
