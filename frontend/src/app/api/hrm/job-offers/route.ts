import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateJobOffer, ksmListJobOffers } from "@/server/ksm/modules/recruitment";
import { createJobOfferSchema } from "@/lib/validation/hrm/recruitment.schema";

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListJobOffers(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createJobOfferSchema);
      const ctx = await getKsmContext();
      return ksmCreateJobOffer(
        {
          poste: body.poste,
          departement: body.departement ?? null,
          localisation: body.localisation ?? null,
          competencesRequises: body.competencesRequises ?? null,
          dateLimite: body.dateLimite ?? null,
          packageSalarial: body.packageSalarial ?? null,
          agencyId: ctx.agencyId,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
