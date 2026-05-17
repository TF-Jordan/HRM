import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmGetPayslipLines } from "@/server/ksm/modules/payroll";

export async function GET(
  _: Request,
  ctx: RouteContext<"/api/hrm/payroll/entries/[entryId]/payslip">,
) {
  const { entryId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmGetPayslipLines(entryId, kctx);
  });
}
