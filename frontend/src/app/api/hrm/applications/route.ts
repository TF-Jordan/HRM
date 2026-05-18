import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateApplication } from "@/server/ksm/modules/recruitment";
import { createApplicationSchema } from "@/lib/validation/hrm/recruitment.schema";

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createApplicationSchema);
      const ctx = await getKsmContext();
      return ksmCreateApplication(
        {
          jobOfferId: body.jobOfferId,
          candidatNom: body.candidatNom,
          candidatPrenom: body.candidatPrenom,
          candidatEmail: body.candidatEmail,
          candidatTelephone: body.candidatTelephone ?? null,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
