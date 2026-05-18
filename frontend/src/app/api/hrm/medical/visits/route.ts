import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateMedicalVisit } from "@/server/ksm/modules/medical";
import { createMedicalVisitSchema } from "@/lib/validation/hrm/medical.schema";

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createMedicalVisitSchema);
      const ctx = await getKsmContext();
      return ksmCreateMedicalVisit(
        {
          employeeId: body.employeeId,
          dateVisite: body.dateVisite,
          medecin: body.medecin,
          resultatAptitude: body.resultatAptitude,
          restrictions: body.restrictions ?? null,
          prochaineEcheance: body.prochaineEcheance ?? null,
          certificatFileId: body.certificatFileId ?? null,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
