import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmListContracts, ksmAddContract } from "@/server/ksm/modules/contracts";
import { addContractSchema } from "@/lib/validation/hrm/contract.schema";

export async function GET(_: Request, ctx: RouteContext<"/api/hrm/employees/[employeeId]/contracts">) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(async () => {
    const kctx = await getKsmContext();
    return ksmListContracts(employeeId, kctx);
  });
}

export async function POST(
  request: Request,
  ctx: RouteContext<"/api/hrm/employees/[employeeId]/contracts">,
) {
  const { employeeId } = await ctx.params;
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, addContractSchema);
      const kctx = await getKsmContext();
      return ksmAddContract(
        employeeId,
        {
          type: body.type,
          dateDebut: body.dateDebut,
          dateFin: body.dateFin ?? null,
          salaireBase: body.salaireBase,
          avantagesNature: body.avantagesNature ?? null,
          periodeEssai: body.periodeEssai ?? null,
        },
        kctx,
      );
    },
    { status: 201 },
  );
}
