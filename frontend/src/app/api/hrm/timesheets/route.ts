import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateTimesheet } from "@/server/ksm/modules/timesheets";
import { z } from "zod";
import { createTimesheetSchema } from "@/lib/validation/hrm/timesheet.schema";

const bodySchema = z.object({ employeeId: z.uuid() }).and(createTimesheetSchema);

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
