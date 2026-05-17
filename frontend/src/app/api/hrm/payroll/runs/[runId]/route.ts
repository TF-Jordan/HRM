import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmGetPayrollRun } from "@/server/ksm/modules/payroll";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/payroll/runs/[runId]">) {
  const { runId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmGetPayrollRun(runId, kctx);
  });
}
