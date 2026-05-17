import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListLeaveBalances } from "@/server/ksm/modules/leave-balances";

export async function GET(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/leave-balances">,
) {
  const { employeeId } = await ctx.params;
  const url = new URL(request.url);
  const annee = Number(url.searchParams.get("annee") ?? new Date().getUTCFullYear());
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListLeaveBalances(employeeId, annee, kctx);
  });
}
