import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateTimesheet } from "@/server/ksm/modules/timesheets";
import { ksmListOrgTimesheets } from "@/server/ksm/modules/timesheets-mgr";
import { z } from "zod";
import { createTimesheetSchema } from "@/lib/validation/hrm/timesheet.schema";

const bodySchema = z.object({ employeeId: z.uuid() }).and(createTimesheetSchema);

export async function GET(request: Request) {
  const url = new URL(request.url);
  const periode = url.searchParams.get("periode");
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListOrgTimesheets(periode, ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, bodySchema);
      const ctx = await getKsmContext();
      return ksmCreateTimesheet(
        {
          employeeId: body.employeeId,
          periode: body.periode,
          entries: body.entries.map((e) => ({
            date: e.date,
            projet: e.projet ?? null,
            heuresNormales: e.heuresNormales,
            heuresSupplementaires: e.heuresSupplementaires,
            description: e.description ?? null,
          })),
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
