import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateMission, ksmListEmployeeMissions } from "@/server/ksm/modules/missions";
import { createMissionSchema } from "@/lib/validation/hrm/mission.schema";

export async function GET(request: Request) {
  const url = new URL(request.url);
  const employeeId = url.searchParams.get("employeeId") ?? "";
  return withKsmHandler(async () => {
    if (!employeeId) return [];
    const ctx = await getKsmContext();
    return ksmListEmployeeMissions(employeeId, ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createMissionSchema);
      const ctx = await getKsmContext();
      return ksmCreateMission(
        {
          employeeId: body.employeeId,
          destination: body.destination,
          objet: body.objet,
          dateDebut: body.dateDebut,
          dateFin: body.dateFin,
          montantAvance: body.montantAvance,
          centreCout: body.centreCout ?? null,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
