import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateMedicalCertificate } from "@/server/ksm/modules/medical";
import { createMedicalCertificateSchema } from "@/lib/validation/hrm/medical.schema";

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createMedicalCertificateSchema);
      const ctx = await getKsmContext();
      return ksmCreateMedicalCertificate(
        {
          employeeId: body.employeeId,
          typeCertificat: body.typeCertificat,
          dateEmission: body.dateEmission,
          dateExpiration: body.dateExpiration ?? null,
          statut: body.statut,
          fichierId: body.fichierId ?? null,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
