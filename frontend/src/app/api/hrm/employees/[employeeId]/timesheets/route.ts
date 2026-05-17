import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListEmployeeTimesheets } from "@/server/ksm/modules/timesheets";

export async function GET(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/timesheets">,
) {
  const { employeeId } = await ctx.params;
  const url = new URL(request.url);
  const periode = url.searchParams.get("periode");
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListEmployeeTimesheets(employeeId, periode, kctx);
  });
}
