import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateEmployeeSkill } from "@/server/ksm/modules/skills";
import { createEmployeeSkillSchema } from "@/lib/validation/hrm/skill.schema";

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createEmployeeSkillSchema);
      const ctx = await getKsmContext();
      return ksmCreateEmployeeSkill(
        {
          employeeId: body.employeeId,
          skillId: body.skillId,
          niveauActuel: Number(body.niveauActuel),
          niveauAttendu: Number(body.niveauAttendu),
          dateEvaluation: body.dateEvaluation,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
