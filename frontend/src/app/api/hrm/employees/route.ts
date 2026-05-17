import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmListEmployees, ksmCreateEmployee } from "@/server/ksm/modules/employees";
import { ksmCreateActor } from "@/server/ksm/modules/actors";
import { createEmployeeSchema } from "@/lib/validation/hrm/employee.schema";

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListEmployees(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const input = await parseBody(request, createEmployeeSchema);
      const ctx = await getKsmContext();

      const actor = await ksmCreateActor(
        {
          organizationId: ctx.organizationId,
          firstName: input.firstName,
          lastName: input.lastName,
          email: input.email ?? null,
          phoneNumber: input.phoneNumber,
        },
        { tenantId: ctx.tenantId, bearer: ctx.bearer },
      );

      return ksmCreateEmployee(
        {
          actorId: actor.id,
          numCnps: input.numCnps,
          categorie: input.categorie,
          echelon: input.echelon,
          dateEmbauche: input.dateEmbauche,
          departmentCode: input.departmentCode,
          modePaiement: input.modePaiement,
          compteBancaire: input.compteBancaire,
          numMobileMoney: input.numMobileMoney,
          operateurMm: input.operateurMm ?? null,
          contractType: input.contractType ?? null,
          contractDateDebut: input.contractDateDebut ?? null,
          contractDateFin: input.contractDateFin ?? null,
          salaireBase: input.salaireBase ?? null,
          avantagesNature: input.avantagesNature ?? null,
          periodeEssai: input.periodeEssai ?? null,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
