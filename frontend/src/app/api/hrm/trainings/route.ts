import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmListTrainings, ksmPlanTraining } from "@/server/ksm/modules/trainings";
import { planTrainingSchema } from "@/lib/validation/hrm/training.schema";

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListTrainings(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, planTrainingSchema);
      const ctx = await getKsmContext();
      return ksmPlanTraining(
        {
          intitule: body.intitule,
          organisme: body.organisme ?? null,
          dateDebut: body.dateDebut,
          dateFin: body.dateFin,
          cout: body.cout,
          nbPlaces: Number(body.nbPlaces),
          lieu: body.lieu ?? null,
          agencyId: ctx.agencyId,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
