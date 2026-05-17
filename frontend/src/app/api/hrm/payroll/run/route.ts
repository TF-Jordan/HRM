import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmRunPayroll } from "@/server/ksm/modules/payroll";
import { runPayrollSchema } from "@/lib/validation/hrm/payroll.schema";

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, runPayrollSchema);
      const ctx = await getKsmContext();
      return ksmRunPayroll({ periode: body.periode, agencyId: body.agencyId ?? null }, ctx);
    },
    { status: 201 },
  );
}
