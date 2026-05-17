import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListPayrollEntries } from "@/server/ksm/modules/payroll";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/payroll/runs/[runId]/entries">) {
  const { runId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListPayrollEntries(runId, kctx);
  });
}
