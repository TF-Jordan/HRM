import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateKpiSnapshot, ksmListKpiSnapshots } from "@/server/ksm/modules/kpi";
import { createKpiSnapshotSchema } from "@/lib/validation/hrm/kpi.schema";

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListKpiSnapshots(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createKpiSnapshotSchema);
      const ctx = await getKsmContext();
      return ksmCreateKpiSnapshot(
        {
          organizationId: ctx.organizationId,
          periode: body.periode,
          effectifTotal: Number(body.effectifTotal),
          effectifActif: Number(body.effectifActif),
          tauxTurnover: body.tauxTurnover,
          tauxAbsenteisme: body.tauxAbsenteisme,
          masseSalariale: body.masseSalariale,
          couvertureCompetences: body.couvertureCompetences,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
